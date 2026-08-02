package com.catail.backend.searchplan.application;

import com.catail.backend.company.db.Company;
import com.catail.backend.company.db.CompanyRepository;
import com.catail.backend.company.domain.Market;
import com.catail.backend.global.BusinessException;
import com.catail.backend.searchplan.domain.Criterion;
import com.catail.backend.searchplan.domain.SearchPlanFormat;
import com.catail.backend.searchplan.outbound.CriterionLlmItem;
import com.catail.backend.searchplan.outbound.SearchPlanLlmPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SearchPlanServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private SearchPlanLlmPort searchPlanLlmPort;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private SearchPlanService searchPlanService;

    private static Map<Criterion, CriterionLlmItem> fullValidLlmResult() {
        Map<Criterion, CriterionLlmItem> result = new EnumMap<>(Criterion.class);
        for (Criterion criterion : Criterion.values()) {
            result.put(criterion, new CriterionLlmItem(criterion + " 관련 근거", List.of(criterion + " 검색어")));
        }
        return result;
    }

    @Test
    @DisplayName("존재하지 않는 companyId를 조회하면 COMPANY_NOT_FOUND 예외가 발생한다")
    void generate_companyNotFound_throwsException() {
        searchPlanService = new SearchPlanService(companyRepository, searchPlanLlmPort, objectMapper);
        given(companyRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> searchPlanService.generate(new SearchPlanRequest(999L, "HBM 사업")))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("KOSPI 기업은 countryCode가 KR로 채워진다")
    void generate_kospiCompany_resolvesCountryCodeKr() {
        searchPlanService = new SearchPlanService(companyRepository, searchPlanLlmPort, objectMapper);
        Company company = Company.create(Market.KOSPI, "005930", "삼성전자");
        given(companyRepository.findById(1L)).willReturn(Optional.of(company));
        given(searchPlanLlmPort.generate(any(), any())).willReturn(fullValidLlmResult());

        SearchPlanFormat format = searchPlanService.generate(new SearchPlanRequest(1L, "HBM 사업"));

        assertThat(format.targetCompany().countryCode()).isEqualTo("KR");
        assertThat(format.targetCompany().englishName()).isEmpty();
    }

    @Test
    @DisplayName("NASDAQ 기업은 예외 없이 countryCode가 빈 문자열로 채워진다")
    void generate_nasdaqCompany_resolvesCountryCodeEmpty() {
        searchPlanService = new SearchPlanService(companyRepository, searchPlanLlmPort, objectMapper);
        Company company = Company.create(Market.NASDAQ, "AAPL", "Apple");
        given(companyRepository.findById(2L)).willReturn(Optional.of(company));
        given(searchPlanLlmPort.generate(any(), any())).willReturn(fullValidLlmResult());

        SearchPlanFormat format = searchPlanService.generate(new SearchPlanRequest(2L, "AI 반도체"));

        assertThat(format.targetCompany().countryCode()).isEmpty();
    }

    @Test
    @DisplayName("LLM 응답에 criterion이 누락되면 LLM_INVALID_RESPONSE 예외가 발생한다")
    void generate_missingCriterion_throwsException() {
        searchPlanService = new SearchPlanService(companyRepository, searchPlanLlmPort, objectMapper);
        Company company = Company.create(Market.KOSPI, "005930", "삼성전자");
        given(companyRepository.findById(1L)).willReturn(Optional.of(company));
        Map<Criterion, CriterionLlmItem> incomplete = fullValidLlmResult();
        incomplete.remove(Criterion.GATE_KEEPER);
        given(searchPlanLlmPort.generate(any(), any())).willReturn(incomplete);

        assertThatThrownBy(() -> searchPlanService.generate(new SearchPlanRequest(1L, "HBM 사업")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(SearchPlanErrorCode.LLM_INVALID_RESPONSE);
    }

    @Test
    @DisplayName("scopeRelevance가 비어있는데 queries가 존재하면 LLM_INVALID_RESPONSE 예외가 발생한다")
    void generate_inconsistentScopeRelevance_throwsException() {
        searchPlanService = new SearchPlanService(companyRepository, searchPlanLlmPort, objectMapper);
        Company company = Company.create(Market.KOSPI, "005930", "삼성전자");
        given(companyRepository.findById(1L)).willReturn(Optional.of(company));
        Map<Criterion, CriterionLlmItem> invalid = fullValidLlmResult();
        invalid.put(Criterion.PARTNER, new CriterionLlmItem("", List.of("검색어")));
        given(searchPlanLlmPort.generate(any(), any())).willReturn(invalid);

        assertThatThrownBy(() -> searchPlanService.generate(new SearchPlanRequest(1L, "HBM 사업")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(SearchPlanErrorCode.LLM_INVALID_RESPONSE);
    }

    @Test
    @DisplayName("정상 응답이면 criteria가 Criterion 선언 순서대로 채워진다")
    void generate_validResponse_ordersCriteriaByDeclarationOrder() {
        searchPlanService = new SearchPlanService(companyRepository, searchPlanLlmPort, objectMapper);
        Company company = Company.create(Market.KOSPI, "005930", "삼성전자");
        given(companyRepository.findById(1L)).willReturn(Optional.of(company));
        given(searchPlanLlmPort.generate(any(), any())).willReturn(fullValidLlmResult());

        SearchPlanFormat format = searchPlanService.generate(new SearchPlanRequest(1L, "HBM 사업"));

        assertThat(format.criteria()).extracting("criterion").containsExactly((Object[]) Criterion.values());
        assertThat(format.analysisScope()).isEqualTo("HBM 사업");
    }
}
