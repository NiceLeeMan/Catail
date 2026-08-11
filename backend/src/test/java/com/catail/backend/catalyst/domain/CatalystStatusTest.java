package com.catail.backend.catalyst.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CatalystStatusTest {

    @Test
    @DisplayName("INACTIVE → ACTIVE 전이는 허용된다")
    void canTransitionTo_inactiveToActive_returnsTrue() {
        assertThat(CatalystStatus.INACTIVE.canTransitionTo(CatalystStatus.ACTIVE)).isTrue();
    }

    @Test
    @DisplayName("ACTIVE → PAUSED 전이는 허용된다")
    void canTransitionTo_activeToPaused_returnsTrue() {
        assertThat(CatalystStatus.ACTIVE.canTransitionTo(CatalystStatus.PAUSED)).isTrue();
    }

    @Test
    @DisplayName("PAUSED → ACTIVE 전이는 허용된다")
    void canTransitionTo_pausedToActive_returnsTrue() {
        assertThat(CatalystStatus.PAUSED.canTransitionTo(CatalystStatus.ACTIVE)).isTrue();
    }

    @Test
    @DisplayName("INACTIVE → PAUSED 전이는 허용되지 않는다")
    void canTransitionTo_inactiveToPaused_returnsFalse() {
        assertThat(CatalystStatus.INACTIVE.canTransitionTo(CatalystStatus.PAUSED)).isFalse();
    }

    @Test
    @DisplayName("ACTIVE → INACTIVE 전이는 허용되지 않는다")
    void canTransitionTo_activeToInactive_returnsFalse() {
        assertThat(CatalystStatus.ACTIVE.canTransitionTo(CatalystStatus.INACTIVE)).isFalse();
    }

    @Test
    @DisplayName("PAUSED → INACTIVE 전이는 허용되지 않는다")
    void canTransitionTo_pausedToInactive_returnsFalse() {
        assertThat(CatalystStatus.PAUSED.canTransitionTo(CatalystStatus.INACTIVE)).isFalse();
    }

    @Test
    @DisplayName("동일 상태로의 전이는 허용되지 않는다")
    void canTransitionTo_sameStatus_returnsFalse() {
        assertThat(CatalystStatus.ACTIVE.canTransitionTo(CatalystStatus.ACTIVE)).isFalse();
    }
}
