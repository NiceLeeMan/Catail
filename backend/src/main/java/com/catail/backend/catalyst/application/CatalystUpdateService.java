package com.catail.backend.catalyst.application;

import com.catail.backend.catalyst.db.Catalyst;
import com.catail.backend.catalyst.db.CatalystRepository;
import com.catail.backend.catalyst.domain.CatalystCategory;
import com.catail.backend.catalyst.inbound.CatalystUpdateRequest;
import com.catail.backend.catalyst.inbound.CatalystUpdateResponse;
import com.catail.backend.company.application.CompanyErrorCode;
import com.catail.backend.company.db.Company;
import com.catail.backend.company.db.CompanyRepository;
import com.catail.backend.global.BusinessException;
import com.catail.backend.global.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalystUpdateService {

    private final CatalystRepository catalystRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public CatalystUpdateResponse update(Long userId, Long catalystId, CatalystUpdateRequest request) {
        Catalyst catalyst = findCatalyst(catalystId);
        assertOwner(catalyst, userId);
        assertNotDeleted(catalyst);

        CatalystCategory category = resolveCategory(request.category());
        String title = resolveTitle(catalyst, category);

        catalyst.update(category, request.detail(), title);

        Company company = companyRepository.findById(catalyst.getCompanyId())
                .orElseThrow(() -> new BusinessException(CompanyErrorCode.COMPANY_NOT_FOUND));
        return CatalystUpdateResponse.from(catalyst, company);
    }

    private Catalyst findCatalyst(Long catalystId) {
        return catalystRepository.findById(catalystId)
                .orElseThrow(() -> new BusinessException(CatalystErrorCode.CATALYST_NOT_FOUND));
    }

    private void assertOwner(Catalyst catalyst, Long userId) {
        if (!catalyst.getUserId().equals(userId)) {
            throw new BusinessException(CatalystErrorCode.CATALYST_ACCESS_DENIED);
        }
    }

    private void assertNotDeleted(Catalyst catalyst) {
        if (catalyst.getDeletedAt() != null) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
    }

    private CatalystCategory resolveCategory(String rawCategory) {
        try {
            return CatalystCategory.valueOf(rawCategory);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
    }

    private String resolveTitle(Catalyst catalyst, CatalystCategory newCategory) {
        if (newCategory == catalyst.getCategory()) {
            return catalyst.getTitle();
        }
        long sameCategoryCount = catalystRepository.countActiveByCategory(
                catalyst.getUserId(), catalyst.getCompanyId(), newCategory);
        return CatalystTitleGenerator.generate(newCategory, sameCategoryCount);
    }
}
