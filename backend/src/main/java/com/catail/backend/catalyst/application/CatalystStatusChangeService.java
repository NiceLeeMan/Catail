package com.catail.backend.catalyst.application;

import com.catail.backend.catalyst.db.Catalyst;
import com.catail.backend.catalyst.db.CatalystRepository;
import com.catail.backend.catalyst.domain.CatalystStatus;
import com.catail.backend.catalyst.inbound.CatalystStatusChangeRequest;
import com.catail.backend.catalyst.inbound.CatalystStatusResponse;
import com.catail.backend.global.BusinessException;
import com.catail.backend.global.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CatalystStatusChangeService {

    private static final Set<CatalystStatus> REQUESTABLE_STATUSES = Set.of(CatalystStatus.ACTIVE, CatalystStatus.PAUSED);

    private final CatalystRepository catalystRepository;

    @Transactional
    public CatalystStatusResponse changeStatus(Long userId, Long catalystId, CatalystStatusChangeRequest request) {
        Catalyst catalyst = findCatalyst(catalystId);
        assertOwner(catalyst, userId);
        assertNotDeleted(catalyst);

        CatalystStatus target = resolveRequestableStatus(request.status());
        assertTransitionAllowed(catalyst.getStatus(), target);

        catalyst.changeStatus(target);
        // TODO(시그널 파이프라인 이슈): ACTIVE 전환 시 트리거 대상 등록 / PAUSED 전환 시 트리거 대상 제외 처리 필요.
        //   실제 트리거 로직은 이 이슈 범위가 아니며, 여기가 그 연결 지점이 될 자리다.

        return CatalystStatusResponse.from(catalyst);
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

    private CatalystStatus resolveRequestableStatus(String rawStatus) {
        CatalystStatus status;
        try {
            status = CatalystStatus.valueOf(rawStatus);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
        if (!REQUESTABLE_STATUSES.contains(status)) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
        return status;
    }

    private void assertTransitionAllowed(CatalystStatus current, CatalystStatus target) {
        if (!current.canTransitionTo(target)) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
    }
}
