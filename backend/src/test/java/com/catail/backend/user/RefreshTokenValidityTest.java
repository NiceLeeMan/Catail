package com.catail.backend.user;

import com.catail.backend.user.DB.RefreshToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RefreshToken 유효성 판단 — revoked_at IS NULL AND expires_at > NOW()")
class RefreshTokenValidityTest {

    @Test
    @DisplayName("TC-2.1 revoked_at=null, expires_at=미래 → 유효")
    void isValid_notRevokedAndNotExpired_returnsTrue() {
        RefreshToken token = RefreshToken.builder()
                .userId(1L)
                .jti("jti-a")
                .expiresAt(Instant.now().plusSeconds(86400))
                .build();

        assertThat(token.isValid()).isTrue();
    }

    @Test
    @DisplayName("TC-2.2 revoked_at이 기록됨 → 무효")
    void isValid_revoked_returnsFalse() {
        RefreshToken token = RefreshToken.builder()
                .userId(1L)
                .jti("jti-b")
                .expiresAt(Instant.now().plusSeconds(86400))
                .build();
        token.revoke();

        assertThat(token.isValid()).isFalse();
    }

    @Test
    @DisplayName("TC-2.3 revoked_at=null이지만 expires_at=과거 → 무효")
    void isValid_expired_returnsFalse() {
        RefreshToken token = RefreshToken.builder()
                .userId(1L)
                .jti("jti-c")
                .expiresAt(Instant.now().minusSeconds(1))
                .build();

        assertThat(token.isValid()).isFalse();
    }

    @Test
    @DisplayName("TC-2.4 revoked_at 기록됨 + expires_at=과거 → 무효")
    void isValid_revokedAndExpired_returnsFalse() {
        RefreshToken token = RefreshToken.builder()
                .userId(1L)
                .jti("jti-d")
                .expiresAt(Instant.now().minusSeconds(1))
                .build();
        token.revoke();

        assertThat(token.isValid()).isFalse();
    }
}
