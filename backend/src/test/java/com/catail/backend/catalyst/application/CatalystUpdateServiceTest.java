package com.catail.backend.catalyst.application;

import com.catail.backend.catalyst.db.Catalyst;
import com.catail.backend.catalyst.db.CatalystRepository;
import com.catail.backend.catalyst.domain.CatalystCategory;
import com.catail.backend.catalyst.domain.CatalystStatus;
import com.catail.backend.catalyst.inbound.CatalystUpdateRequest;
import com.catail.backend.catalyst.inbound.CatalystUpdateResponse;
import com.catail.backend.company.db.Company;
import com.catail.backend.company.db.CompanyRepository;
import com.catail.backend.company.domain.Market;
import com.catail.backend.global.BusinessException;
import com.catail.backend.global.GlobalErrorCode;
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
class CatalystUpdateServiceTest {

    private static final Long CATALYST_ID = 1L;
    private static final Long OWNER_ID = 1L;
    private static final Long COMPANY_ID = 100L;

    @Mock
    private CatalystRepository catalystRepository;

    @Mock
    private CompanyRepository companyRepository;

    private CatalystUpdateService catalystUpdateService;

    private Catalyst catalyst(Long userId, CatalystCategory category, String title) {
        return Catalyst.create(userId, COMPANY_ID, category, "기존 상세내용 10자 이상 작성", title, CatalystStatus.ACTIVE);
    }

    private CatalystUpdateRequest request(String category, String detail) {
        return new CatalystUpdateRequest(category, detail);
    }

    @Test
    @DisplayName("category가 그대로면 title이 유지된다")
    void update_sameCategory_keepsTitle() {
        catalystUpdateService = new CatalystUpdateService(catalystRepository, companyRepository);
        Catalyst catalyst = catalyst(OWNER_ID, CatalystCategory.SUPPLY_CHAIN, "공급망");
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst));
        given(companyRepository.findById(COMPANY_ID))
                .willReturn(Optional.of(Company.create(Market.KOSPI, "005930", "삼성전자")));

        CatalystUpdateResponse response = catalystUpdateService.update(
                OWNER_ID, CATALYST_ID, request("SUPPLY_CHAIN", "변경된 상세내용 10자 이상 작성"));

        assertThat(response.title()).isEqualTo("공급망");
        assertThat(response.detail()).isEqualTo("변경된 상세내용 10자 이상 작성");
    }

    @Test
    @DisplayName("category가 변경되면 title이 재계산된다")
    void update_categoryChanged_recalculatesTitle() {
        catalystUpdateService = new CatalystUpdateService(catalystRepository, companyRepository);
        Catalyst catalyst = catalyst(OWNER_ID, CatalystCategory.SUPPLY_CHAIN, "공급망");
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst));
        given(catalystRepository.countActiveByCategory(OWNER_ID, COMPANY_ID, CatalystCategory.GOVERNANCE)).willReturn(1L);
        given(companyRepository.findById(COMPANY_ID))
                .willReturn(Optional.of(Company.create(Market.KOSPI, "005930", "삼성전자")));

        CatalystUpdateResponse response = catalystUpdateService.update(
                OWNER_ID, CATALYST_ID, request("GOVERNANCE", "변경된 상세내용 10자 이상 작성"));

        assertThat(response.title()).isEqualTo("경영권/지배구조 #2");
        assertThat(response.category()).isEqualTo("GOVERNANCE");
    }

    @Test
    @DisplayName("존재하지 않는 catalystId면 CATALYST_NOT_FOUND 예외가 발생한다")
    void update_notFound_throwsCatalystNotFound() {
        catalystUpdateService = new CatalystUpdateService(catalystRepository, companyRepository);
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> catalystUpdateService.update(
                OWNER_ID, CATALYST_ID, request("SUPPLY_CHAIN", "변경된 상세내용 10자 이상 작성")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(CatalystErrorCode.CATALYST_NOT_FOUND);
    }

    @Test
    @DisplayName("소유자가 아니면 CATALYST_ACCESS_DENIED 예외가 발생한다")
    void update_notOwner_throwsCatalystAccessDenied() {
        catalystUpdateService = new CatalystUpdateService(catalystRepository, companyRepository);
        Catalyst catalyst = catalyst(2L, CatalystCategory.SUPPLY_CHAIN, "공급망");
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst));

        assertThatThrownBy(() -> catalystUpdateService.update(
                OWNER_ID, CATALYST_ID, request("SUPPLY_CHAIN", "변경된 상세내용 10자 이상 작성")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(CatalystErrorCode.CATALYST_ACCESS_DENIED);
    }

    @Test
    @DisplayName("삭제된 카탈리스트면 INVALID_INPUT 예외가 발생한다")
    void update_deletedCatalyst_throwsInvalidInput() {
        catalystUpdateService = new CatalystUpdateService(catalystRepository, companyRepository);
        Catalyst catalyst = catalyst(OWNER_ID, CatalystCategory.SUPPLY_CHAIN, "공급망");
        catalyst.softDelete();
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst));

        assertThatThrownBy(() -> catalystUpdateService.update(
                OWNER_ID, CATALYST_ID, request("SUPPLY_CHAIN", "변경된 상세내용 10자 이상 작성")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(GlobalErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("category가 정의된 6개 값이 아니면 INVALID_INPUT 예외가 발생한다")
    void update_invalidCategory_throwsInvalidInput() {
        catalystUpdateService = new CatalystUpdateService(catalystRepository, companyRepository);
        Catalyst catalyst = catalyst(OWNER_ID, CatalystCategory.SUPPLY_CHAIN, "공급망");
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst));

        assertThatThrownBy(() -> catalystUpdateService.update(
                OWNER_ID, CATALYST_ID, request("UNKNOWN_CATEGORY", "변경된 상세내용 10자 이상 작성")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(GlobalErrorCode.INVALID_INPUT);
    }
}
