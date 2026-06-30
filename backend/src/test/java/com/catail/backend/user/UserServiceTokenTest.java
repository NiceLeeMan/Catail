package com.catail.backend.user;

import com.catail.backend.global.BusinessException;
import com.catail.backend.global.GlobalErrorCode;
import com.catail.backend.global.jwt.JwtProvider;
import com.catail.backend.user.DB.RefreshToken;
import com.catail.backend.user.DB.RefreshTokenRepository;
import com.catail.backend.user.DB.User;
import com.catail.backend.user.DB.UserRepository;
import com.catail.backend.user.DB.UserRole;
import com.catail.backend.user.application.TokenPair;
import com.catail.backend.user.application.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTokenTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtProvider jwtProvider;

    @InjectMocks
    private UserService userService;

    // ──────────────────────────────────────────
    @Nested
    @DisplayName("saveRefreshToken — TC-1.3")
    class SaveRefreshToken {

        @Test
        @DisplayName("userId, jti, expiresAt이 일치하는 RefreshToken 행을 저장한다")
        void saveRefreshToken_savesTokenWithCorrectFields() {
            Long userId = 1L;
            String jti = "test-jti";
            Instant expiresAt = Instant.now().plusSeconds(432_000);

            userService.saveRefreshToken(userId, jti, expiresAt);

            ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepository).save(captor.capture());

            RefreshToken saved = captor.getValue();
            assertThat(saved.getUserId()).isEqualTo(userId);
            assertThat(saved.getJti()).isEqualTo(jti);
            assertThat(saved.getExpiresAt()).isEqualTo(expiresAt);
        }

        @Test
        @DisplayName("저장되는 행의 revoked_at은 null이다")
        void saveRefreshToken_revokedAtIsNull() {
            userService.saveRefreshToken(1L, "jti", Instant.now().plusSeconds(432_000));

            ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepository).save(captor.capture());
            assertThat(captor.getValue().getRevokedAt()).isNull();
        }
    }

    // ──────────────────────────────────────────
    @Nested
    @DisplayName("findRefreshTokenByJti")
    class FindRefreshTokenByJti {

        @Test
        @DisplayName("존재하는 jti로 조회하면 RefreshToken을 반환한다")
        void findRefreshTokenByJti_existingJti_returnsToken() {
            RefreshToken token = RefreshToken.builder()
                    .userId(1L).jti("jti-a").expiresAt(Instant.now().plusSeconds(86400))
                    .build();
            given(refreshTokenRepository.findByJti("jti-a")).willReturn(Optional.of(token));

            RefreshToken result = userService.findRefreshTokenByJti("jti-a");

            assertThat(result).isSameAs(token);
        }

        @Test
        @DisplayName("존재하지 않는 jti로 조회하면 UNAUTHENTICATED 예외가 발생한다")
        void findRefreshTokenByJti_unknownJti_throwsUnauthenticated() {
            given(refreshTokenRepository.findByJti("nonexistent")).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findRefreshTokenByJti("nonexistent"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(GlobalErrorCode.UNAUTHENTICATED);
        }
    }

    // ──────────────────────────────────────────
    @Nested
    @DisplayName("revokeAllUserTokens")
    class RevokeAllUserTokens {

        @Test
        @DisplayName("userId에 해당하는 모든 Refresh Token의 revoked_at을 기록한다")
        void revokeAllUserTokens_callsRepositoryWithUserIdAndNow() {
            userService.revokeAllUserTokens(1L);

            verify(refreshTokenRepository).revokeAllByUserId(eq(1L), any(Instant.class));
        }
    }

    // ──────────────────────────────────────────
    @Nested
    @DisplayName("rotateRefreshToken")
    class RotateRefreshToken {

        @Test
        @DisplayName("기존 jti를 revoke하고 새 jti로 새 행을 저장한다")
        void rotateRefreshToken_revokesOldAndSavesNew() {
            String oldJti = "old-jti";
            String newJti = "new-jti";
            Instant newExpiresAt = Instant.now().plusSeconds(432_000);

            userService.rotateRefreshToken(oldJti, 1L, newJti, newExpiresAt);

            verify(refreshTokenRepository).revokeByJti(eq(oldJti), any(Instant.class));

            ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepository).save(captor.capture());

            RefreshToken saved = captor.getValue();
            assertThat(saved.getJti()).isEqualTo(newJti);
            assertThat(saved.getUserId()).isEqualTo(1L);
            assertThat(saved.getExpiresAt()).isEqualTo(newExpiresAt);
        }

        @Test
        @DisplayName("새 행의 revoked_at은 null이다")
        void rotateRefreshToken_newToken_revokedAtIsNull() {
            userService.rotateRefreshToken("old", 1L, "new", Instant.now().plusSeconds(432_000));

            ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepository).save(captor.capture());
            assertThat(captor.getValue().getRevokedAt()).isNull();
        }
    }

    // ──────────────────────────────────────────
    @Nested
    @DisplayName("refresh — UC-9")
    class Refresh {

        private User mockUser() {
            User user = mock(User.class);
            given(user.getId()).willReturn(1L);
            given(user.getRole()).willReturn(UserRole.USER);
            return user;
        }

        @Test
        @DisplayName("TC-3.1 유효한 토큰 → 새 TokenPair 반환 및 RTR 수행")
        void refresh_validToken_returnsNewTokenPair() {
            RefreshToken stored = RefreshToken.builder()
                    .userId(1L).jti("jti-a").expiresAt(Instant.now().plusSeconds(86400))
                    .build();
            User user = mockUser();
            Instant newExpiry = Instant.now().plusSeconds(432_000);

            given(jwtProvider.validateToken("raw-rt")).willReturn(true);
            given(jwtProvider.extractJti("raw-rt")).willReturn("jti-a");
            given(refreshTokenRepository.findByJti("jti-a")).willReturn(Optional.of(stored));
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(jwtProvider.generateRefreshToken(1L)).willReturn("new-rt");
            given(jwtProvider.extractJti("new-rt")).willReturn("jti-b");
            given(jwtProvider.extractExpiration("new-rt")).willReturn(newExpiry);
            given(jwtProvider.generateAccessToken(1L, "USER")).willReturn("new-at");

            TokenPair result = userService.refresh("raw-rt");

            assertThat(result.accessToken()).isEqualTo("new-at");
            assertThat(result.refreshToken()).isEqualTo("new-rt");
            verify(refreshTokenRepository).revokeByJti(eq("jti-a"), any(Instant.class));
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("JWT 서명 검증 실패 → UNAUTHENTICATED")
        void refresh_invalidJwt_throwsUnauthenticated() {
            given(jwtProvider.validateToken("bad-rt")).willReturn(false);

            assertThatThrownBy(() -> userService.refresh("bad-rt"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(GlobalErrorCode.UNAUTHENTICATED);
        }

        @Test
        @DisplayName("TC-3.4 폐기된 jti → 전체 세션 무효화 후 UNAUTHENTICATED")
        void refresh_revokedToken_revokesAllAndThrows() {
            RefreshToken revoked = RefreshToken.builder()
                    .userId(1L).jti("jti-a").expiresAt(Instant.now().plusSeconds(86400))
                    .build();
            revoked.revoke();

            given(jwtProvider.validateToken("raw-rt")).willReturn(true);
            given(jwtProvider.extractJti("raw-rt")).willReturn("jti-a");
            given(refreshTokenRepository.findByJti("jti-a")).willReturn(Optional.of(revoked));

            assertThatThrownBy(() -> userService.refresh("raw-rt"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(GlobalErrorCode.UNAUTHENTICATED);

            verify(refreshTokenRepository).revokeAllByUserId(eq(1L), any(Instant.class));
        }

        @Test
        @DisplayName("TC-3.5 DB expires_at이 과거 → 전체 세션 무효화 후 UNAUTHENTICATED")
        void refresh_dbExpiredToken_revokesAllAndThrows() {
            RefreshToken dbExpired = RefreshToken.builder()
                    .userId(1L).jti("jti-a").expiresAt(Instant.now().minusSeconds(1))
                    .build();

            given(jwtProvider.validateToken("raw-rt")).willReturn(true);
            given(jwtProvider.extractJti("raw-rt")).willReturn("jti-a");
            given(refreshTokenRepository.findByJti("jti-a")).willReturn(Optional.of(dbExpired));

            assertThatThrownBy(() -> userService.refresh("raw-rt"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(GlobalErrorCode.UNAUTHENTICATED);

            verify(refreshTokenRepository).revokeAllByUserId(eq(1L), any(Instant.class));
        }
    }
}
