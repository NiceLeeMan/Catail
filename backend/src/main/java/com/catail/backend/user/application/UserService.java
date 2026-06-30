package com.catail.backend.user.application;

import com.catail.backend.global.BusinessException;
import com.catail.backend.global.GlobalErrorCode;
import com.catail.backend.global.jwt.JwtProvider;
import com.catail.backend.user.DB.RefreshToken;
import com.catail.backend.user.DB.RefreshTokenRepository;
import com.catail.backend.user.DB.User;
import com.catail.backend.user.DB.UserRepository;
import com.catail.backend.user.DB.UserRole;
import com.catail.backend.user.outbound.OAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;

    // UC-1: OAuth 로그인/회원가입
    @Transactional
    public User loginOrRegister(OAuthProvider provider, String providerId, String email) {
        if (provider == null) {
            throw new BusinessException(UserErrorCode.UNSUPPORTED_PROVIDER);
        }

        Optional<User> existingUser = userRepository.findByProviderAndProviderId(provider, providerId);

        if (existingUser.isPresent() && existingUser.get().getDeletedAt() == null) {
            return existingUser.get();
        }

        return userRepository.save(
                User.builder()
                        .provider(provider)
                        .providerId(providerId)
                        .email(email)
                        .nickname(NicknameGenerator.generate())
                        .role(UserRole.USER)
                        .build()
        );
    }

    // UC-2: 프로필 조회
    public User getProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    // UC-3: 로그아웃 — 서명 검증 후 해당 기기 토큰만 revoke (만료 여부 무관)
    @Transactional
    public void logout(String rawRefreshToken) {
        if (!jwtProvider.validateSignature(rawRefreshToken)) {
            throw new BusinessException(GlobalErrorCode.UNAUTHENTICATED);
        }
        String jti = jwtProvider.extractJtiIgnoringExpiry(rawRefreshToken);
        refreshTokenRepository.revokeByJti(jti, Instant.now());
    }

    // UC-4: 회원 탈퇴
    @Transactional
    public void deleteAccount(Long userId) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        user.softDelete();
    }

    // UC-5: Refresh Token 저장 (로그인 시)
    @Transactional
    public void saveRefreshToken(Long userId, String jti, Instant expiresAt) {
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .userId(userId)
                        .jti(jti)
                        .expiresAt(expiresAt)
                        .build()
        );
    }

    // UC-6: JTI로 RefreshToken 조회
    @Transactional(readOnly = true)
    public RefreshToken findRefreshTokenByJti(String jti) {
        return refreshTokenRepository.findByJti(jti)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.UNAUTHENTICATED));
    }

    // UC-7: 해당 유저의 모든 토큰 revoke (토큰 탈취 감지 시)
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId, Instant.now());
    }

    // UC-8: RTR — 기존 토큰 revoke + 새 토큰 저장 (단일 트랜잭션)
    @Transactional
    public void rotateRefreshToken(String oldJti, Long userId, String newJti, Instant newExpiresAt) {
        refreshTokenRepository.revokeByJti(oldJti, Instant.now());
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .userId(userId)
                        .jti(newJti)
                        .expiresAt(newExpiresAt)
                        .build()
        );
    }

    // UC-9: Refresh Token 재발급 (RTR) + 새 Access Token 발급
    @Transactional
    public TokenPair refresh(String rawRefreshToken) {
        if (!jwtProvider.validateToken(rawRefreshToken)) {
            throw new BusinessException(GlobalErrorCode.UNAUTHENTICATED);
        }

        String jti = jwtProvider.extractJti(rawRefreshToken);
        RefreshToken storedToken = findRefreshTokenByJti(jti);

        if (!storedToken.isValid()) {
            revokeAllUserTokens(storedToken.getUserId());
            throw new BusinessException(GlobalErrorCode.UNAUTHENTICATED);
        }

        User user = getProfile(storedToken.getUserId());

        String newRawRefreshToken = jwtProvider.generateRefreshToken(user.getId());
        String newJti = jwtProvider.extractJti(newRawRefreshToken);
        Instant newExpiresAt = jwtProvider.extractExpiration(newRawRefreshToken);

        rotateRefreshToken(jti, user.getId(), newJti, newExpiresAt);

        String newAccessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole().name());
        return new TokenPair(newAccessToken, newRawRefreshToken);
    }
}
