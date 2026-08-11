package com.catail.backend.catalyst.application;

import com.catail.backend.catalyst.db.Catalyst;
import com.catail.backend.catalyst.db.CatalystRepository;
import com.catail.backend.global.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalystDeleteService {

    private final CatalystRepository catalystRepository;

    @Transactional
    public void delete(Long userId, Long catalystId) {
        Catalyst catalyst = findActiveCatalyst(catalystId);
        assertOwner(catalyst, userId);
        catalyst.softDelete();
    }

    private Catalyst findActiveCatalyst(Long catalystId) {
        return catalystRepository.findById(catalystId)
                .filter(catalyst -> catalyst.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(CatalystErrorCode.CATALYST_NOT_FOUND));
    }

    private void assertOwner(Catalyst catalyst, Long userId) {
        if (!catalyst.getUserId().equals(userId)) {
            throw new BusinessException(CatalystErrorCode.CATALYST_ACCESS_DENIED);
        }
    }
}
