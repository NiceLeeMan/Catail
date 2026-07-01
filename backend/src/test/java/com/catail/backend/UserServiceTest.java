package com.catail.backend;

import com.catail.backend.global.BusinessException;
import com.catail.backend.global.GlobalErrorCode;
import com.catail.backend.global.jwt.JwtProvider;
import com.catail.backend.user.DB.RefreshTokenRepository;
import com.catail.backend.user.DB.User;
import com.catail.backend.user.DB.UserRepository;
import com.catail.backend.user.DB.UserRole;
import com.catail.backend.user.application.UserErrorCode;
import com.catail.backend.user.application.UserService;
import com.catail.backend.user.outbound.OAuthProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("loginOrRegister")
    class LoginOrRegister {

        @Test
        @DisplayName("신규 유저 로그인 시 회원을 생성하고 닉네임이 null이 아니다")
        void loginOrRegister_newUser_createsUserWithNickname() {
            given(userRepository.findByProviderAndProviderId(OAuthProvider.GOOGLE, "google-123"))
                    .willReturn(Optional.empty());
            given(userRepository.save(any(User.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            User result = userService.loginOrRegister(OAuthProvider.GOOGLE, "google-123", null);

            assertThat(result.getNickname()).isNotNull();
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("기존 정상 회원 로그인 시 저장 없이 기존 회원을 반환한다")
        void loginOrRegister_existingUser_returnsExistingUserWithoutSave() {
            User existingUser = buildUser("HappyCat1234", OAuthProvider.GOOGLE, "google-123", "user@test.com");
            given(userRepository.findByProviderAndProviderId(OAuthProvider.GOOGLE, "google-123"))
                    .willReturn(Optional.of(existingUser));

            User result = userService.loginOrRegister(OAuthProvider.GOOGLE, "google-123", null);

            assertThat(result).isSameAs(existingUser);
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("탈퇴 회원 로그인 시 새 회원을 생성하고 닉네임이 null이 아니다")
        void loginOrRegister_deletedUser_createsNewUser() {
            User deletedUser = mock(User.class);
            given(deletedUser.getDeletedAt()).willReturn(LocalDateTime.now());
            given(userRepository.findByProviderAndProviderId(OAuthProvider.GOOGLE, "google-123"))
                    .willReturn(Optional.of(deletedUser));
            given(userRepository.save(any(User.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            User result = userService.loginOrRegister(OAuthProvider.GOOGLE, "google-123", null);

            assertThat(result.getNickname()).isNotNull();
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("지원하지 않는 provider이면 UNSUPPORTED_PROVIDER 예외가 발생한다")
        void loginOrRegister_nullProvider_throwsUnsupportedProviderException() {
            assertThatThrownBy(() -> userService.loginOrRegister(null, "id-123", null))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(UserErrorCode.UNSUPPORTED_PROVIDER);
        }
    }

    @Nested
    @DisplayName("getProfile")
    class GetProfile {

        @Test
        @DisplayName("존재하는 userId로 조회하면 프로필을 반환한다")
        void getProfile_existingUser_returnsProfile() {
            User user = buildUser("HappyCat1234", OAuthProvider.GOOGLE, "google-123", "user@test.com");
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            User result = userService.getProfile(1L);

            assertThat(result).isSameAs(user);
        }

        @Test
        @DisplayName("존재하지 않는 userId로 조회하면 USER_NOT_FOUND 예외가 발생한다")
        void getProfile_nonExistentUser_throwsUserNotFoundException() {
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getProfile(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(UserErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("logout")
    class Logout {

        @Test
        @DisplayName("서명이 유효한 토큰으로 로그아웃하면 jti의 revoked_at이 기록된다")
        void logout_validSignature_revokesToken() {
            given(jwtProvider.validateSignature("raw-token")).willReturn(true);
            given(jwtProvider.extractJtiIgnoringExpiry("raw-token")).willReturn("test-jti");

            userService.logout("raw-token");

            verify(refreshTokenRepository).revokeByJti(eq("test-jti"), any(Instant.class));
        }

        @Test
        @DisplayName("서명이 위조된 토큰으로 로그아웃하면 UNAUTHENTICATED 예외가 발생한다")
        void logout_invalidSignature_throwsUnauthenticated() {
            given(jwtProvider.validateSignature("tampered")).willReturn(false);

            assertThatThrownBy(() -> userService.logout("tampered"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(GlobalErrorCode.UNAUTHENTICATED);
        }

        @Test
        @DisplayName("만료된 토큰으로 로그아웃해도 jti를 추출하여 정상 처리된다")
        void logout_expiredToken_stillRevokes() {
            given(jwtProvider.validateSignature("expired-token")).willReturn(true);
            given(jwtProvider.extractJtiIgnoringExpiry("expired-token")).willReturn("jti-a");

            assertThatCode(() -> userService.logout("expired-token")).doesNotThrowAnyException();
            verify(refreshTokenRepository).revokeByJti(eq("jti-a"), any(Instant.class));
        }
    }

    @Nested
    @DisplayName("deleteAccount")
    class DeleteAccount {

        @Test
        @DisplayName("존재하는 userId로 탈퇴하면 deleted_at이 기록된다")
        void deleteAccount_existingUser_recordsDeletedAt() {
            User user = mock(User.class);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            userService.deleteAccount(1L);

            verify(user).softDelete();
        }

        @Test
        @DisplayName("이미 탈퇴한 회원은 USER_NOT_FOUND 예외가 발생한다")
        void deleteAccount_alreadyDeletedUser_throwsUserNotFoundException() {
            User deletedUser = mock(User.class);
            given(deletedUser.getDeletedAt()).willReturn(LocalDateTime.now());
            given(userRepository.findById(1L)).willReturn(Optional.of(deletedUser));

            assertThatThrownBy(() -> userService.deleteAccount(1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(UserErrorCode.USER_NOT_FOUND);
        }
    }

    private User buildUser(String nickname, OAuthProvider provider, String providerId, String email) {
        return User.builder()
                .nickname(nickname)
                .provider(provider)
                .providerId(providerId)
                .email(email)
                .role(UserRole.USER)
                .build();
    }
}
