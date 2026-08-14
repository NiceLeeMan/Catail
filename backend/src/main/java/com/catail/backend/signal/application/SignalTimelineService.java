package com.catail.backend.signal.application;

import com.catail.backend.signal.db.Signal;
import com.catail.backend.signal.db.SignalRepository;
import com.catail.backend.signal.domain.SignalStatus;
import com.catail.backend.signal.inbound.SignalListItem;
import com.catail.backend.signal.inbound.SignalTimelineResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SignalTimelineService {

    private final CatalystOwnershipValidator catalystOwnershipValidator;
    private final SignalRepository signalRepository;

    @Transactional(readOnly = true)
    public SignalTimelineResponse getTimeline(Long userId, Long catalystId) {
        catalystOwnershipValidator.validate(userId, catalystId);

        List<Signal> signals = signalRepository.findTimeline(catalystId, SignalStatus.ADOPTED);
        List<SignalListItem> items = signals.stream().map(SignalListItem::from).toList();
        return new SignalTimelineResponse(items);
    }
}
