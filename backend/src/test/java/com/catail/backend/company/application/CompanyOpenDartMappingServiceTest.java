package com.catail.backend.company.application;

import com.catail.backend.company.db.Company;
import com.catail.backend.company.db.CompanyRepository;
import com.catail.backend.company.domain.Market;
import com.catail.backend.opendart.CorpCodeItem;
import com.catail.backend.opendart.OpenDartCorpCodeClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CompanyOpenDartMappingServiceTest {

    @Mock
    private OpenDartCorpCodeClient openDartCorpCodeClient;

    @Mock
    private CompanyRepository companyRepository;

    private CompanyOpenDartMappingService mappingService;

    @BeforeEach
    void setUp() {
        mappingService = new CompanyOpenDartMappingService(openDartCorpCodeClient, companyRepository);
    }

    @Test
    @DisplayName("stockCode가 일치하는 기업에 corp_code를 매핑해 저장한다")
    void mapKospiCorpCodes_matchingStockCode_assignsCorpCode() {
        Company company = Company.create(Market.KOSPI, "005930", "삼성전자");
        given(companyRepository.findByMarketAndOpenDartCorpCodeIsNull(Market.KOSPI))
                .willReturn(List.of(company));
        given(openDartCorpCodeClient.fetchAll())
                .willReturn(List.of(new CorpCodeItem("00126380", "005930")));

        mappingService.mapKospiCorpCodes();

        assertThat(company.getOpenDartCorpCode()).isEqualTo("00126380");
        verify(companyRepository).save(company);
    }

    @Test
    @DisplayName("일치하는 corp_code가 없으면 매핑하지 않는다")
    void mapKospiCorpCodes_noMatch_doesNotAssign() {
        Company company = Company.create(Market.KOSPI, "005930", "삼성전자");
        given(companyRepository.findByMarketAndOpenDartCorpCodeIsNull(Market.KOSPI))
                .willReturn(List.of(company));
        given(openDartCorpCodeClient.fetchAll())
                .willReturn(List.of(new CorpCodeItem("00000000", "000660")));

        mappingService.mapKospiCorpCodes();

        assertThat(company.getOpenDartCorpCode()).isNull();
        verify(companyRepository, never()).save(company);
    }

    @Test
    @DisplayName("매핑되지 않은 기업이 없으면 corp_code API를 호출하지 않는다")
    void mapKospiCorpCodes_noUnmappedCompanies_skipsFetch() {
        given(companyRepository.findByMarketAndOpenDartCorpCodeIsNull(Market.KOSPI))
                .willReturn(List.of());

        mappingService.mapKospiCorpCodes();

        verify(openDartCorpCodeClient, never()).fetchAll();
    }
}
