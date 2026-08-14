package com.catail.backend.signal.application;

import com.catail.backend.catalyst.application.CatalystErrorCode;
import com.catail.backend.catalyst.db.Catalyst;
import com.catail.backend.catalyst.db.CatalystRepository;
import com.catail.backend.global.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 시그널 관련 API들이 공통으로 필요로 하는 "카탈리스트가 존재하고, 삭제되지 않았고,
 * 요청자가 소유자인지" 검증. 소프트 삭제된 카탈리스트는 미존재로 취급한다
 * (데이터명세 6.5 — 삭제되면 일반 조회에서 제외).
 */
@Component
@RequiredArgsConstructor
public class CatalystOwnershipValidator {

    private final CatalystRepository catalystRepository;

    public Catalyst validate(Long userId, Long catalystId) {
        Catalyst catalyst = catalystRepository.findById(catalystId)
                .orElseThrow(() -> new BusinessException(CatalystErrorCode.CATALYST_NOT_FOUND));
        if (catalyst.getDeletedAt() != null) {
            throw new BusinessException(CatalystErrorCode.CATALYST_NOT_FOUND);
        }
        if (!catalyst.getUserId().equals(userId)) {
            throw new BusinessException(CatalystErrorCode.CATALYST_ACCESS_DENIED);
        }
        return catalyst;
    }
}
