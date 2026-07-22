package com.catail.backend.company.application;

import com.catail.backend.company.db.Company;
import com.catail.backend.company.db.CompanyRepository;
import com.catail.backend.company.domain.Market;
import com.catail.backend.company.inbound.read.CompanyDetailResponse;
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
class CompanyDetailServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    private CompanyDetailService companyDetailService;

    @Test
    @DisplayName("존재하는 companyId를 조회하면 기업 상세정보를 반환한다")
    void getDetail_existingCompany_returnsDetail() {
        companyDetailService = new CompanyDetailService(companyRepository);
        Company company = Company.create(Market.KOSPI, "005930", "삼성전자");
        given(companyRepository.findById(1L)).willReturn(Optional.of(company));

        CompanyDetailResponse response = companyDetailService.getDetail(1L);

        assertThat(response.companyName()).isEqualTo("삼성전자");
        assertThat(response.stockCode()).isEqualTo("005930");
        assertThat(response.market()).isEqualTo("KOSPI");
    }

    @Test
    @DisplayName("존재하지 않는 companyId를 조회하면 COMPANY_NOT_FOUND 예외가 발생한다")
    void getDetail_notFound_throwsCompanyNotFound() {
        companyDetailService = new CompanyDetailService(companyRepository);
        given(companyRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> companyDetailService.getDetail(999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(CompanyErrorCode.COMPANY_NOT_FOUND);
    }
}
