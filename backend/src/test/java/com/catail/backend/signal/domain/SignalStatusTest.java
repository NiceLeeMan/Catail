package com.catail.backend.signal.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SignalStatusTest {

    @Test
    @DisplayName("PENDING → ADOPTED 전이는 허용된다")
    void canTransitionTo_pendingToAdopted_returnsTrue() {
        assertThat(SignalStatus.PENDING.canTransitionTo(SignalStatus.ADOPTED)).isTrue();
    }

    @Test
    @DisplayName("PENDING → EXCLUDED 전이는 허용된다")
    void canTransitionTo_pendingToExcluded_returnsTrue() {
        assertThat(SignalStatus.PENDING.canTransitionTo(SignalStatus.EXCLUDED)).isTrue();
    }

    @Test
    @DisplayName("ADOPTED → EXCLUDED 전이는 허용된다")
    void canTransitionTo_adoptedToExcluded_returnsTrue() {
        assertThat(SignalStatus.ADOPTED.canTransitionTo(SignalStatus.EXCLUDED)).isTrue();
    }

    @Test
    @DisplayName("EXCLUDED → ADOPTED 전이는 허용된다")
    void canTransitionTo_excludedToAdopted_returnsTrue() {
        assertThat(SignalStatus.EXCLUDED.canTransitionTo(SignalStatus.ADOPTED)).isTrue();
    }

    @Test
    @DisplayName("ADOPTED → PENDING 전이는 허용되지 않는다")
    void canTransitionTo_adoptedToPending_returnsFalse() {
        assertThat(SignalStatus.ADOPTED.canTransitionTo(SignalStatus.PENDING)).isFalse();
    }

    @Test
    @DisplayName("EXCLUDED → PENDING 전이는 허용되지 않는다")
    void canTransitionTo_excludedToPending_returnsFalse() {
        assertThat(SignalStatus.EXCLUDED.canTransitionTo(SignalStatus.PENDING)).isFalse();
    }

    @Test
    @DisplayName("동일 상태로의 전이는 허용되지 않는다")
    void canTransitionTo_sameStatus_returnsFalse() {
        assertThat(SignalStatus.PENDING.canTransitionTo(SignalStatus.PENDING)).isFalse();
        assertThat(SignalStatus.ADOPTED.canTransitionTo(SignalStatus.ADOPTED)).isFalse();
        assertThat(SignalStatus.EXCLUDED.canTransitionTo(SignalStatus.EXCLUDED)).isFalse();
    }
}
