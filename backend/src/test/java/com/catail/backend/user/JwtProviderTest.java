package com.catail.backend.user;

import com.catail.backend.global.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderTest {

    private static final String SECRET = "test-secret-must-be-32chars-long!!";
    private static final long ACCESS_EXPIRY_MS = 3_600_000L;   // 1시간
    private static final long REFRESH_EXPIRY_MS = 432_000_000L; // 5일

    private JwtProvider jwtProvider;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(SECRET, ACCESS_EXPIRY_MS, REFRESH_EXPIRY_MS);
        secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Nested
    @DisplayName("TC-1.1 Access Token 발급 시 클레임 구성")
    class AccessToken {

        @Test
        @DisplayName("sub 클레임은 userId를 String으로 변환한 값이다")
        void accessToken_sub_isUserId() {
            String token = jwtProvider.generateAccessToken(1L, "USER");
            assertThat(parse(token).getSubject()).isEqualTo("1");
        }

        @Test
        @DisplayName("role 클레임은 USER이다")
        void accessToken_role_isUser() {
            String token = jwtProvider.generateAccessToken(1L, "USER");
            assertThat(parse(token).get("role", String.class)).isEqualTo("USER");
        }

        @Test
        @DisplayName("iat와 exp 클레임이 존재한다")
        void accessToken_iat_exp_exist() {
            String token = jwtProvider.generateAccessToken(1L, "USER");
            Claims claims = parse(token);
            assertThat(claims.getIssuedAt()).isNotNull();
            assertThat(claims.getExpiration()).isNotNull();
        }

        @Test
        @DisplayName("exp - iat는 3600초(1시간)이다")
        void accessToken_expiry_is3600Seconds() {
            Instant before = Instant.now();
            String token = jwtProvider.generateAccessToken(1L, "USER");

            Claims claims = parse(token);
            long iatEpoch = claims.getIssuedAt().toInstant().getEpochSecond();
            long expEpoch = claims.getExpiration().toInstant().getEpochSecond();

            assertThat(expEpoch - iatEpoch).isEqualTo(3600L);
        }
    }

    @Nested
    @DisplayName("TC-1.2 Refresh Token 발급 시 클레임 구성")
    class RefreshToken {

        @Test
        @DisplayName("sub 클레임은 userId를 String으로 변환한 값이다")
        void refreshToken_sub_isUserId() {
            String token = jwtProvider.generateRefreshToken(1L);
            assertThat(parse(token).getSubject()).isEqualTo("1");
        }

        @Test
        @DisplayName("jti 클레임이 UUID 형식으로 존재한다")
        void refreshToken_jti_isUuid() {
            String token = jwtProvider.generateRefreshToken(1L);
            String jti = parse(token).getId();
            assertThat(jti).isNotNull();
            assertThatIsUuid(jti);
        }

        @Test
        @DisplayName("iat와 exp 클레임이 존재한다")
        void refreshToken_iat_exp_exist() {
            String token = jwtProvider.generateRefreshToken(1L);
            Claims claims = parse(token);
            assertThat(claims.getIssuedAt()).isNotNull();
            assertThat(claims.getExpiration()).isNotNull();
        }

        @Test
        @DisplayName("exp - iat는 432000초(5일)이다")
        void refreshToken_expiry_is432000Seconds() {
            String token = jwtProvider.generateRefreshToken(1L);
            Claims claims = parse(token);
            long iatEpoch = claims.getIssuedAt().toInstant().getEpochSecond();
            long expEpoch = claims.getExpiration().toInstant().getEpochSecond();
            assertThat(expEpoch - iatEpoch).isEqualTo(432_000L);
        }

        private void assertThatIsUuid(String value) {
            assertThat(UUID.fromString(value)).isNotNull(); // 형식 불일치 시 IllegalArgumentException
        }
    }

    @Nested
    @DisplayName("validateToken / extractJti / extractExpiration")
    class Helpers {

        @Test
        @DisplayName("유효한 토큰은 validateToken이 true를 반환한다")
        void validateToken_validToken_returnsTrue() {
            String token = jwtProvider.generateAccessToken(1L, "USER");
            assertThat(jwtProvider.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("서명이 위조된 토큰은 validateToken이 false를 반환한다")
        void validateToken_tamperedToken_returnsFalse() {
            String token = jwtProvider.generateAccessToken(1L, "USER");
            String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "invalidsignature";
            assertThat(jwtProvider.validateToken(tampered)).isFalse();
        }

        @Test
        @DisplayName("extractJti는 Refresh Token의 jti UUID를 반환한다")
        void extractJti_returnsJtiFromRefreshToken() {
            String token = jwtProvider.generateRefreshToken(1L);
            String jti = jwtProvider.extractJti(token);
            assertThat(jti).isNotBlank();
            assertThat(UUID.fromString(jti)).isNotNull();
        }

        @Test
        @DisplayName("extractExpiration은 Refresh Token의 만료 Instant를 반환한다")
        void extractExpiration_returnsExpirationInstant() {
            Instant before = Instant.now();
            String token = jwtProvider.generateRefreshToken(1L);
            Instant expiration = jwtProvider.extractExpiration(token);
            assertThat(expiration).isAfter(before.plusSeconds(431_990)); // 5일 근사
        }
    }
}
