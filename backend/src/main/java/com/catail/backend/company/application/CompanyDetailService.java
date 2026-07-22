package com.catail.backend.company.application;

import com.catail.backend.company.db.Company;
import com.catail.backend.company.db.CompanyRepository;
import com.catail.backend.company.inbound.read.CompanyDetailResponse;
import com.catail.backend.global.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyDetailService {

    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public CompanyDetailResponse getDetail(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(CompanyErrorCode.COMPANY_NOT_FOUND));
        return CompanyDetailResponse.from(company);
    }
}
