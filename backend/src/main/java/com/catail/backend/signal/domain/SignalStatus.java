package com.catail.backend.signal.domain;

import java.util.Map;
import java.util.Set;

public enum SignalStatus {
    PENDING,
    ADOPTED,
    EXCLUDED;

    private static final Map<SignalStatus, Set<SignalStatus>> ALLOWED_TRANSITIONS = Map.of(
            PENDING, Set.of(ADOPTED, EXCLUDED),
            ADOPTED, Set.of(EXCLUDED),
            EXCLUDED, Set.of(ADOPTED)
    );

    public boolean canTransitionTo(SignalStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }
}
