package com.catail.backend.catalyst.domain;

import java.util.Map;
import java.util.Set;

public enum CatalystStatus {
    ACTIVE,
    INACTIVE,
    PAUSED;

    private static final Map<CatalystStatus, Set<CatalystStatus>> ALLOWED_TRANSITIONS = Map.of(
            INACTIVE, Set.of(ACTIVE),
            ACTIVE, Set.of(PAUSED),
            PAUSED, Set.of(ACTIVE)
    );

    public boolean canTransitionTo(CatalystStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }
}
