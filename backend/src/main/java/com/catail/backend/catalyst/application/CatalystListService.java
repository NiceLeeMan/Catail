package com.catail.backend.catalyst.application;

import com.catail.backend.catalyst.db.Catalyst;
import com.catail.backend.catalyst.db.CatalystRepository;
import com.catail.backend.catalyst.inbound.CatalystListResponse;
import com.catail.backend.company.application.CompanyErrorCode;
import com.catail.backend.company.db.CompanyRepository;
import com.catail.backend.global.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalystListService {

    private final CatalystRepository catalystRepository;
    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public CatalystListResponse getList(Long userId, Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new BusinessException(CompanyErrorCode.COMPANY_NOT_FOUND);
        }

        List<Catalyst> catalysts = catalystRepository.findActive(userId, companyId);
        return CatalystListResponse.from(catalysts);
    }
}
