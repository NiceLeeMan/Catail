package com.catail.backend.catalyst.application;

import com.catail.backend.catalyst.db.Catalyst;
import com.catail.backend.catalyst.db.CatalystRepository;
import com.catail.backend.catalyst.domain.CatalystCategory;
import com.catail.backend.catalyst.domain.CatalystStatus;
import com.catail.backend.catalyst.inbound.CatalystCreateRequest;
import com.catail.backend.catalyst.inbound.CatalystCreateResponse;
import com.catail.backend.company.application.CompanyErrorCode;
import com.catail.backend.company.db.Company;
import com.catail.backend.company.db.CompanyRepository;
import com.catail.backend.global.BusinessException;
import com.catail.backend.global.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CatalystCreateService {

    private static final int MAX_CATALYSTS_PER_COMPANY = 3;
    private static final Set<CatalystStatus> CREATABLE_STATUSES = Set.of(CatalystStatus.ACTIVE, CatalystStatus.INACTIVE);

    private final CatalystRepository catalystRepository;
    private final CompanyRepository companyRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public CatalystCreateResponse create(Long userId, CatalystCreateRequest request) {
        Company company = vaildateCompany(request.companyId());
        CatalystCategory category = validateCategory(request.category());
        CatalystStatus status = validateCreationStatus(request.status());

        assertWithinLimit(userId, company.getId());
        String title = generateTitle(userId, company.getId(), category);

        Catalyst catalyst = catalystRepository.save(
                Catalyst.create(userId, company.getId(), category, request.detail(), title, status));

        if (catalyst.getStatus() == CatalystStatus.ACTIVE) {
            applicationEventPublisher.publishEvent(new CatalystActivatedEvent(catalyst.getId()));
        }

        return CatalystCreateResponse.from(catalyst, company);
    }

    private Company vaildateCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(CompanyErrorCode.COMPANY_NOT_FOUND));
    }

    private CatalystCategory validateCategory(String rawCategory) {
        try {
            return CatalystCategory.valueOf(rawCategory);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
    }

    private CatalystStatus validateCreationStatus(String rawStatus) {
        CatalystStatus status;
        try {
            status = CatalystStatus.valueOf(rawStatus);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
        if (!CREATABLE_STATUSES.contains(status)) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
        return status;
    }

    private void assertWithinLimit(Long userId, Long companyId) {
        if (catalystRepository.countActive(userId, companyId) >= MAX_CATALYSTS_PER_COMPANY) {
            throw new BusinessException(CatalystErrorCode.CATALYST_LIMIT_EXCEEDED);
        }
    }

    private String generateTitle(Long userId, Long companyId, CatalystCategory category) {
        long sameCategoryCount = catalystRepository.countActiveByCategory(userId, companyId, category);
        return CatalystTitleGenerator.generate(category, sameCategoryCount);
    }
}
