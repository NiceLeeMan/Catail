package com.catail.backend.searchplan.application;

import com.catail.backend.company.application.CompanyErrorCode;
import com.catail.backend.company.db.Company;
import com.catail.backend.company.db.CompanyRepository;
import com.catail.backend.global.BusinessException;
import com.catail.backend.searchplan.domain.Criterion;
import com.catail.backend.searchplan.domain.CriterionPlan;
import com.catail.backend.searchplan.domain.SearchPlanFormat;
import com.catail.backend.searchplan.domain.TargetCompanyInfo;
import com.catail.backend.searchplan.outbound.CriterionLlmItem;
import com.catail.backend.searchplan.outbound.SearchPlanLlmPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchPlanService {

    private final CompanyRepository companyRepository;
    private final SearchPlanLlmPort searchPlanLlmPort;
    private final ObjectMapper objectMapper;

    public SearchPlanFormat generate(SearchPlanRequest request) {
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new BusinessException(CompanyErrorCode.COMPANY_NOT_FOUND));

        TargetCompanyInfo targetCompany = new TargetCompanyInfo(
                request.companyId(),
                company.getCompanyName(),
                "",
                company.getStockCode(),
                company.getMarket(),
                CountryCodeResolver.resolve(company.getMarket())
        );

        Map<Criterion, CriterionLlmItem> llmResult =
                searchPlanLlmPort.generate(targetCompany, request.analysisScope());

        List<CriterionPlan> criteria = Arrays.stream(Criterion.values())
                .map(criterion -> toCriterionPlan(criterion, llmResult))
                .toList();

        SearchPlanFormat format = new SearchPlanFormat(targetCompany, request.analysisScope(), criteria);
        log.info("탐색 계획 포맷 생성 완료:\n{}",
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(format));
        return format;
    }

    private CriterionPlan toCriterionPlan(Criterion criterion, Map<Criterion, CriterionLlmItem> llmResult) {
        CriterionLlmItem item = llmResult.get(criterion);
        if (item == null || item.queries() == null || item.scopeRelevance() == null) {
            throw new BusinessException(SearchPlanErrorCode.LLM_INVALID_RESPONSE);
        }
        if (item.scopeRelevance().isBlank() && !item.queries().isEmpty()) {
            throw new BusinessException(SearchPlanErrorCode.LLM_INVALID_RESPONSE);
        }
        return new CriterionPlan(criterion, item.scopeRelevance(), item.queries());
    }
}
