package com.catail.backend.catalyst.application;

import com.catail.backend.catalyst.db.Catalyst;
import com.catail.backend.catalyst.db.CatalystRepository;
import com.catail.backend.catalyst.domain.CatalystCategory;
import com.catail.backend.catalyst.domain.CatalystStatus;
import com.catail.backend.catalyst.inbound.CatalystListResponse;
import com.catail.backend.company.application.CompanyErrorCode;
import com.catail.backend.company.db.CompanyRepository;
import com.catail.backend.global.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CatalystListServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long COMPANY_ID = 100L;

    @Mock
    private CatalystRepository catalystRepository;

    @Mock
    private CompanyRepository companyRepository;

    private CatalystListService catalystListService;

    @Test
    @DisplayName("등록된 카탈리스트가 있으면 목록을 반환한다")
    void getList_existingCatalysts_returnsList() {
        catalystListService = new CatalystListService(catalystRepository, companyRepository);
        Catalyst catalyst = Catalyst.create(USER_ID, COMPANY_ID, CatalystCategory.SUPPLY_CHAIN,
                "테스트용 상세내용 10자 이상 작성", "공급망", CatalystStatus.ACTIVE);
        given(companyRepository.existsById(COMPANY_ID)).willReturn(true);
        given(catalystRepository.findActive(USER_ID, COMPANY_ID)).willReturn(List.of(catalyst));

        CatalystListResponse response = catalystListService.getList(USER_ID, COMPANY_ID);

        assertThat(response.catalysts()).hasSize(1);
        assertThat(response.catalysts().get(0).title()).isEqualTo("공급망");
    }

    @Test
    @DisplayName("등록된 카탈리스트가 없으면 빈 목록을 반환한다")
    void getList_noCatalysts_returnsEmptyList() {
        catalystListService = new CatalystListService(catalystRepository, companyRepository);
        given(companyRepository.existsById(COMPANY_ID)).willReturn(true);
        given(catalystRepository.findActive(USER_ID, COMPANY_ID)).willReturn(List.of());

        CatalystListResponse response = catalystListService.getList(USER_ID, COMPANY_ID);

        assertThat(response.catalysts()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 companyId면 COMPANY_NOT_FOUND 예외가 발생한다")
    void getList_companyNotFound_throwsCompanyNotFound() {
        catalystListService = new CatalystListService(catalystRepository, companyRepository);
        given(companyRepository.existsById(COMPANY_ID)).willReturn(false);

        assertThatThrownBy(() -> catalystListService.getList(USER_ID, COMPANY_ID))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(CompanyErrorCode.COMPANY_NOT_FOUND);
    }
}
