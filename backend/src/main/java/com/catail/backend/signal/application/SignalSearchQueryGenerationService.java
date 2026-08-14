package com.catail.backend.signal.application;

import com.catail.backend.catalyst.domain.CatalystCategory;
import com.catail.backend.company.application.CompanyErrorCode;
import com.catail.backend.company.db.Company;
import com.catail.backend.company.db.CompanyRepository;
import com.catail.backend.global.BusinessException;
import com.catail.backend.global.GlobalErrorCode;
import com.catail.backend.signal.outbound.openai.SignalSearchQueryLlmAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SignalSearchQueryGenerationService {

    private final CompanyRepository companyRepository;
    private final SignalSearchQueryLlmAdapter signalSearchQueryLlmAdapter;

    public List<String> generate(Long companyId, String category, String detail) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(CompanyErrorCode.COMPANY_NOT_FOUND));
        CatalystCategory resolvedCategory = resolveCategory(category);

        return signalSearchQueryLlmAdapter.generate(company.getCompanyName(), resolvedCategory.name(), detail);
    }

    private CatalystCategory resolveCategory(String category) {
        try {
            return CatalystCategory.valueOf(category);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
    }
}
