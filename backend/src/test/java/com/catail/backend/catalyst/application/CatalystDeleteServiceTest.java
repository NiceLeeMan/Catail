package com.catail.backend.catalyst.application;

import com.catail.backend.catalyst.db.Catalyst;
import com.catail.backend.catalyst.db.CatalystRepository;
import com.catail.backend.catalyst.domain.CatalystCategory;
import com.catail.backend.catalyst.domain.CatalystStatus;
import com.catail.backend.global.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CatalystDeleteServiceTest {

    private static final Long CATALYST_ID = 1L;
    private static final Long OWNER_ID = 1L;

    @Mock
    private CatalystRepository catalystRepository;

    private CatalystDeleteService catalystDeleteService;

    private Catalyst catalyst(Long userId) {
        return Catalyst.create(userId, 100L, CatalystCategory.SUPPLY_CHAIN,
                "테스트용 상세내용 10자 이상 작성", "공급망", CatalystStatus.ACTIVE);
    }

    @Test
    @DisplayName("소유자가 삭제를 요청하면 deletedAt이 기록된다")
    void delete_owner_softDeletesCatalyst() {
        catalystDeleteService = new CatalystDeleteService(catalystRepository);
        Catalyst catalyst = catalyst(OWNER_ID);
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst));

        catalystDeleteService.delete(OWNER_ID, CATALYST_ID);

        assertThat(catalyst.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 catalystId면 CATALYST_NOT_FOUND 예외가 발생한다")
    void delete_notFound_throwsCatalystNotFound() {
        catalystDeleteService = new CatalystDeleteService(catalystRepository);
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> catalystDeleteService.delete(OWNER_ID, CATALYST_ID))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(CatalystErrorCode.CATALYST_NOT_FOUND);
    }

    @Test
    @DisplayName("이미 삭제된 catalystId면 CATALYST_NOT_FOUND 예외가 발생한다")
    void delete_alreadyDeleted_throwsCatalystNotFound() {
        catalystDeleteService = new CatalystDeleteService(catalystRepository);
        Catalyst catalyst = catalyst(OWNER_ID);
        catalyst.softDelete();
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst));

        assertThatThrownBy(() -> catalystDeleteService.delete(OWNER_ID, CATALYST_ID))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(CatalystErrorCode.CATALYST_NOT_FOUND);
    }

    @Test
    @DisplayName("소유자가 아니면 CATALYST_ACCESS_DENIED 예외가 발생한다")
    void delete_notOwner_throwsCatalystAccessDenied() {
        catalystDeleteService = new CatalystDeleteService(catalystRepository);
        Catalyst catalyst = catalyst(2L);
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst));

        assertThatThrownBy(() -> catalystDeleteService.delete(OWNER_ID, CATALYST_ID))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(CatalystErrorCode.CATALYST_ACCESS_DENIED);
    }
}
