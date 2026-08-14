package com.catail.backend.signal.application;

import com.catail.backend.catalyst.application.CatalystErrorCode;
import com.catail.backend.catalyst.db.Catalyst;
import com.catail.backend.catalyst.domain.CatalystCategory;
import com.catail.backend.catalyst.domain.CatalystStatus;
import com.catail.backend.global.BusinessException;
import com.catail.backend.global.GlobalErrorCode;
import com.catail.backend.signal.db.Signal;
import com.catail.backend.signal.db.SignalRepository;
import com.catail.backend.signal.domain.SignalStatus;
import com.catail.backend.signal.inbound.SignalStatusChangeRequest;
import com.catail.backend.signal.inbound.SignalStatusResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SignalStatusChangeServiceTest {

    private static final Long OWNER_ID = 1L;
    private static final Long SIGNAL_ID = 1L;
    private static final Long CATALYST_ID = 10L;

    @Mock private SignalRepository signalRepository;
    @Mock private CatalystOwnershipValidator catalystOwnershipValidator;

    private SignalStatusChangeService service() {
        return new SignalStatusChangeService(signalRepository, catalystOwnershipValidator);
    }

    private Signal signal(SignalStatus status) {
        Signal signal = Signal.create(CATALYST_ID, "제목", "요약", "https://origin.example.com/1",
                "https://n.news.naver.com/1", OffsetDateTime.now(), "언론사");
        signal.changeStatus(status);
        return signal;
    }

    private Catalyst catalyst() {
        return Catalyst.create(OWNER_ID, 100L, CatalystCategory.SUPPLY_CHAIN,
                "테스트용 상세내용 10자 이상 작성", "공급망", CatalystStatus.ACTIVE);
    }

    @Test
    @DisplayName("PENDING에서 ADOPTED로 전환할 수 있다")
    void changeStatus_pendingToAdopted_succeeds() {
        Signal signal = signal(SignalStatus.PENDING);
        given(signalRepository.findById(SIGNAL_ID)).willReturn(Optional.of(signal));
        given(catalystOwnershipValidator.validate(OWNER_ID, CATALYST_ID)).willReturn(catalyst());

        SignalStatusResponse response = service().changeStatus(
                OWNER_ID, SIGNAL_ID, new SignalStatusChangeRequest("ADOPTED"));

        assertThat(response.status()).isEqualTo("ADOPTED");
    }

    @Test
    @DisplayName("PENDING에서 EXCLUDED로 전환할 수 있다")
    void changeStatus_pendingToExcluded_succeeds() {
        Signal signal = signal(SignalStatus.PENDING);
        given(signalRepository.findById(SIGNAL_ID)).willReturn(Optional.of(signal));
        given(catalystOwnershipValidator.validate(OWNER_ID, CATALYST_ID)).willReturn(catalyst());

        SignalStatusResponse response = service().changeStatus(
                OWNER_ID, SIGNAL_ID, new SignalStatusChangeRequest("EXCLUDED"));

        assertThat(response.status()).isEqualTo("EXCLUDED");
    }

    @Test
    @DisplayName("ADOPTED에서 EXCLUDED로 전환할 수 있다")
    void changeStatus_adoptedToExcluded_succeeds() {
        Signal signal = signal(SignalStatus.ADOPTED);
        given(signalRepository.findById(SIGNAL_ID)).willReturn(Optional.of(signal));
        given(catalystOwnershipValidator.validate(OWNER_ID, CATALYST_ID)).willReturn(catalyst());

        SignalStatusResponse response = service().changeStatus(
                OWNER_ID, SIGNAL_ID, new SignalStatusChangeRequest("EXCLUDED"));

        assertThat(response.status()).isEqualTo("EXCLUDED");
    }

    @Test
    @DisplayName("EXCLUDED에서 ADOPTED로 전환할 수 있다")
    void changeStatus_excludedToAdopted_succeeds() {
        Signal signal = signal(SignalStatus.EXCLUDED);
        given(signalRepository.findById(SIGNAL_ID)).willReturn(Optional.of(signal));
        given(catalystOwnershipValidator.validate(OWNER_ID, CATALYST_ID)).willReturn(catalyst());

        SignalStatusResponse response = service().changeStatus(
                OWNER_ID, SIGNAL_ID, new SignalStatusChangeRequest("ADOPTED"));

        assertThat(response.status()).isEqualTo("ADOPTED");
    }

    @Test
    @DisplayName("PENDING으로 되돌리는 전환은 허용되지 않아 INVALID_INPUT 예외가 발생한다")
    void changeStatus_requestPending_throwsInvalidInput() {
        Signal signal = signal(SignalStatus.ADOPTED);
        given(signalRepository.findById(SIGNAL_ID)).willReturn(Optional.of(signal));
        given(catalystOwnershipValidator.validate(OWNER_ID, CATALYST_ID)).willReturn(catalyst());

        assertThatThrownBy(() -> service().changeStatus(
                OWNER_ID, SIGNAL_ID, new SignalStatusChangeRequest("PENDING")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(GlobalErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("존재하지 않는 signalId면 SIGNAL_NOT_FOUND 예외가 발생한다")
    void changeStatus_notFound_throwsSignalNotFound() {
        given(signalRepository.findById(SIGNAL_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service().changeStatus(
                OWNER_ID, SIGNAL_ID, new SignalStatusChangeRequest("ADOPTED")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(SignalErrorCode.SIGNAL_NOT_FOUND);
    }

    @Test
    @DisplayName("소유자가 아니면 예외가 그대로 전파된다")
    void changeStatus_notOwner_propagatesException() {
        Signal signal = signal(SignalStatus.PENDING);
        given(signalRepository.findById(SIGNAL_ID)).willReturn(Optional.of(signal));
        given(catalystOwnershipValidator.validate(OWNER_ID, CATALYST_ID))
                .willThrow(new BusinessException(CatalystErrorCode.CATALYST_ACCESS_DENIED));

        assertThatThrownBy(() -> service().changeStatus(
                OWNER_ID, SIGNAL_ID, new SignalStatusChangeRequest("ADOPTED")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(CatalystErrorCode.CATALYST_ACCESS_DENIED);
    }

    @Test
    @DisplayName("status가 정의된 값이 아니면 INVALID_INPUT 예외가 발생한다")
    void changeStatus_invalidStatus_throwsInvalidInput() {
        Signal signal = signal(SignalStatus.PENDING);
        given(signalRepository.findById(SIGNAL_ID)).willReturn(Optional.of(signal));
        given(catalystOwnershipValidator.validate(OWNER_ID, CATALYST_ID)).willReturn(catalyst());

        assertThatThrownBy(() -> service().changeStatus(
                OWNER_ID, SIGNAL_ID, new SignalStatusChangeRequest("UNKNOWN")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(GlobalErrorCode.INVALID_INPUT);
    }
}
