package com.catail.backend.signal.application;

import com.catail.backend.global.BusinessException;
import com.catail.backend.global.GlobalErrorCode;
import com.catail.backend.signal.db.Signal;
import com.catail.backend.signal.db.SignalRepository;
import com.catail.backend.signal.domain.SignalStatus;
import com.catail.backend.signal.inbound.SignalStatusChangeRequest;
import com.catail.backend.signal.inbound.SignalStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class SignalStatusChangeService {

    private static final Set<SignalStatus> REQUESTABLE_STATUSES = Set.of(SignalStatus.ADOPTED, SignalStatus.EXCLUDED);

    private final SignalRepository signalRepository;
    private final CatalystOwnershipValidator catalystOwnershipValidator;

    @Transactional
    public SignalStatusResponse changeStatus(Long userId, Long signalId, SignalStatusChangeRequest request) {
        Signal signal = findSignal(signalId);
        catalystOwnershipValidator.validate(userId, signal.getCatalystId());

        SignalStatus target = resolveRequestableStatus(request.status());
        assertTransitionAllowed(signal.getStatus(), target);

        signal.changeStatus(target);

        return SignalStatusResponse.from(signal);
    }

    private Signal findSignal(Long signalId) {
        return signalRepository.findById(signalId)
                .orElseThrow(() -> new BusinessException(SignalErrorCode.SIGNAL_NOT_FOUND));
    }

    private SignalStatus resolveRequestableStatus(String rawStatus) {
        SignalStatus status;
        try {
            status = SignalStatus.valueOf(rawStatus);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
        if (!REQUESTABLE_STATUSES.contains(status)) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
        return status;
    }

    private void assertTransitionAllowed(SignalStatus current, SignalStatus target) {
        if (!current.canTransitionTo(target)) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
    }
}
