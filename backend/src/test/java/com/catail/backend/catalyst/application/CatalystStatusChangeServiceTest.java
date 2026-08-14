package com.catail.backend.catalyst.application;

import com.catail.backend.catalyst.db.Catalyst;
import com.catail.backend.catalyst.db.CatalystRepository;
import com.catail.backend.catalyst.domain.CatalystCategory;
import com.catail.backend.catalyst.domain.CatalystStatus;
import com.catail.backend.catalyst.inbound.CatalystStatusChangeRequest;
import com.catail.backend.catalyst.inbound.CatalystStatusResponse;
import com.catail.backend.global.BusinessException;
import com.catail.backend.global.GlobalErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CatalystStatusChangeServiceTest {

    private static final Long CATALYST_ID = 1L;
    private static final Long OWNER_ID = 1L;

    @Mock
    private CatalystRepository catalystRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private CatalystStatusChangeService catalystStatusChangeService;

    private Catalyst catalyst(Long userId, CatalystStatus status) {
        Catalyst catalyst = Catalyst.create(userId, 100L, CatalystCategory.SUPPLY_CHAIN,
                "테스트용 상세내용 10자 이상 작성", "공급망", status);
        return catalyst;
    }

    @Test
    @DisplayName("INACTIVE에서 ACTIVE로 전환할 수 있다")
    void changeStatus_inactiveToActive_succeeds() {
        catalystStatusChangeService = new CatalystStatusChangeService(catalystRepository, applicationEventPublisher);
        Catalyst catalyst = catalyst(OWNER_ID, CatalystStatus.INACTIVE);
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst));

        CatalystStatusResponse response = catalystStatusChangeService.changeStatus(
                OWNER_ID, CATALYST_ID, new CatalystStatusChangeRequest("ACTIVE"));

        assertThat(response.status()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("ACTIVE에서 PAUSED로 전환할 수 있다")
    void changeStatus_activeToPaused_succeeds() {
        catalystStatusChangeService = new CatalystStatusChangeService(catalystRepository, applicationEventPublisher);
        Catalyst catalyst = catalyst(OWNER_ID, CatalystStatus.ACTIVE);
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst));

        CatalystStatusResponse response = catalystStatusChangeService.changeStatus(
                OWNER_ID, CATALYST_ID, new CatalystStatusChangeRequest("PAUSED"));

        assertThat(response.status()).isEqualTo("PAUSED");
    }

    @Test
    @DisplayName("PAUSED에서 ACTIVE로 전환할 수 있다")
    void changeStatus_pausedToActive_succeeds() {
        catalystStatusChangeService = new CatalystStatusChangeService(catalystRepository, applicationEventPublisher);
        Catalyst catalyst = catalyst(OWNER_ID, CatalystStatus.PAUSED);
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst));

        CatalystStatusResponse response = catalystStatusChangeService.changeStatus(
                OWNER_ID, CATALYST_ID, new CatalystStatusChangeRequest("ACTIVE"));

        assertThat(response.status()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("ACTIVE로 전환되면 CatalystActivatedEvent를 발행한다")
    void changeStatus_toActive_publishesCatalystActivatedEvent() {
        catalystStatusChangeService = new CatalystStatusChangeService(catalystRepository, applicationEventPublisher);
        Catalyst catalyst = catalyst(OWNER_ID, CatalystStatus.PAUSED);
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst));

        catalystStatusChangeService.changeStatus(OWNER_ID, CATALYST_ID, new CatalystStatusChangeRequest("ACTIVE"));

        verify(applicationEventPublisher).publishEvent(any(CatalystActivatedEvent.class));
    }

    @Test
    @DisplayName("PAUSED로 전환되면 이벤트를 발행하지 않는다")
    void changeStatus_toPaused_doesNotPublishEvent() {
        catalystStatusChangeService = new CatalystStatusChangeService(catalystRepository, applicationEventPublisher);
        Catalyst catalyst = catalyst(OWNER_ID, CatalystStatus.ACTIVE);
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst));

        catalystStatusChangeService.changeStatus(OWNER_ID, CATALYST_ID, new CatalystStatusChangeRequest("PAUSED"));

        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    @DisplayName("INACTIVE에서 PAUSED로는 전환할 수 없다")
    void changeStatus_inactiveToPaused_throwsInvalidInput() {
        catalystStatusChangeService = new CatalystStatusChangeService(catalystRepository, applicationEventPublisher);
        Catalyst catalyst = catalyst(OWNER_ID, CatalystStatus.INACTIVE);
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst));

        assertThatThrownBy(() -> catalystStatusChangeService.changeStatus(
                OWNER_ID, CATALYST_ID, new CatalystStatusChangeRequest("PAUSED")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(GlobalErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("존재하지 않는 catalystId면 CATALYST_NOT_FOUND 예외가 발생한다")
    void changeStatus_notFound_throwsCatalystNotFound() {
        catalystStatusChangeService = new CatalystStatusChangeService(catalystRepository, applicationEventPublisher);
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> catalystStatusChangeService.changeStatus(
                OWNER_ID, CATALYST_ID, new CatalystStatusChangeRequest("ACTIVE")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(CatalystErrorCode.CATALYST_NOT_FOUND);
    }

    @Test
    @DisplayName("소유자가 아니면 CATALYST_ACCESS_DENIED 예외가 발생한다")
    void changeStatus_notOwner_throwsCatalystAccessDenied() {
        catalystStatusChangeService = new CatalystStatusChangeService(catalystRepository, applicationEventPublisher);
        Catalyst catalyst = catalyst(2L, CatalystStatus.INACTIVE);
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst));

        assertThatThrownBy(() -> catalystStatusChangeService.changeStatus(
                OWNER_ID, CATALYST_ID, new CatalystStatusChangeRequest("ACTIVE")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(CatalystErrorCode.CATALYST_ACCESS_DENIED);
    }

    @Test
    @DisplayName("삭제된 카탈리스트면 INVALID_INPUT 예외가 발생한다")
    void changeStatus_deletedCatalyst_throwsInvalidInput() {
        catalystStatusChangeService = new CatalystStatusChangeService(catalystRepository, applicationEventPublisher);
        Catalyst catalyst = catalyst(OWNER_ID, CatalystStatus.ACTIVE);
        catalyst.softDelete();
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst));

        assertThatThrownBy(() -> catalystStatusChangeService.changeStatus(
                OWNER_ID, CATALYST_ID, new CatalystStatusChangeRequest("PAUSED")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(GlobalErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("INACTIVE는 요청 가능한 status 값이 아니므로 INVALID_INPUT 예외가 발생한다")
    void changeStatus_requestInactive_throwsInvalidInput() {
        catalystStatusChangeService = new CatalystStatusChangeService(catalystRepository, applicationEventPublisher);
        Catalyst catalyst = catalyst(OWNER_ID, CatalystStatus.ACTIVE);
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst));

        assertThatThrownBy(() -> catalystStatusChangeService.changeStatus(
                OWNER_ID, CATALYST_ID, new CatalystStatusChangeRequest("INACTIVE")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(GlobalErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("status가 정의된 값이 아니면 INVALID_INPUT 예외가 발생한다")
    void changeStatus_invalidStatus_throwsInvalidInput() {
        catalystStatusChangeService = new CatalystStatusChangeService(catalystRepository, applicationEventPublisher);
        Catalyst catalyst = catalyst(OWNER_ID, CatalystStatus.ACTIVE);
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst));

        assertThatThrownBy(() -> catalystStatusChangeService.changeStatus(
                OWNER_ID, CATALYST_ID, new CatalystStatusChangeRequest("UNKNOWN")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(GlobalErrorCode.INVALID_INPUT);
    }
}
