package com.catail.backend.signal.application;

import com.catail.backend.catalyst.application.CatalystErrorCode;
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
class CatalystOwnershipValidatorTest {

    private static final Long OWNER_ID = 1L;
    private static final Long CATALYST_ID = 10L;

    @Mock private CatalystRepository catalystRepository;

    private CatalystOwnershipValidator validator() {
        return new CatalystOwnershipValidator(catalystRepository);
    }

    private Catalyst catalyst(Long userId) {
        return Catalyst.create(userId, 100L, CatalystCategory.SUPPLY_CHAIN,
                "테스트용 상세내용 10자 이상 작성", "공급망", CatalystStatus.ACTIVE);
    }

    @Test
    @DisplayName("존재하고 소유자가 맞으면 카탈리스트를 반환한다")
    void validate_ownedAndExisting_returnsCatalyst() {
        Catalyst catalyst = catalyst(OWNER_ID);
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst));

        Catalyst result = validator().validate(OWNER_ID, CATALYST_ID);

        assertThat(result).isSameAs(catalyst);
    }

    @Test
    @DisplayName("존재하지 않는 catalystId면 CATALYST_NOT_FOUND 예외가 발생한다")
    void validate_notFound_throwsCatalystNotFound() {
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> validator().validate(OWNER_ID, CATALYST_ID))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(CatalystErrorCode.CATALYST_NOT_FOUND);
    }

    @Test
    @DisplayName("삭제된 카탈리스트면 CATALYST_NOT_FOUND 예외가 발생한다")
    void validate_deletedCatalyst_throwsCatalystNotFound() {
        Catalyst catalyst = catalyst(OWNER_ID);
        catalyst.softDelete();
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst));

        assertThatThrownBy(() -> validator().validate(OWNER_ID, CATALYST_ID))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(CatalystErrorCode.CATALYST_NOT_FOUND);
    }

    @Test
    @DisplayName("소유자가 아니면 CATALYST_ACCESS_DENIED 예외가 발생한다")
    void validate_notOwner_throwsCatalystAccessDenied() {
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst(2L)));

        assertThatThrownBy(() -> validator().validate(OWNER_ID, CATALYST_ID))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(CatalystErrorCode.CATALYST_ACCESS_DENIED);
    }
}
