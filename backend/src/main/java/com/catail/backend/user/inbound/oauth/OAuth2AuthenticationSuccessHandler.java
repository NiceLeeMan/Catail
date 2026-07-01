package com.catail.backend.user.inbound.oauth;

import com.catail.backend.global.jwt.JwtProvider;
import com.catail.backend.user.application.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final UserService userService;

    @Value("${oauth2.redirect-uri}")
    private String redirectUri;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        Long userId = oAuth2User.getUserId();

        String accessToken = jwtProvider.generateAccessToken(userId, "USER");
        String refreshToken = jwtProvider.generateRefreshToken(userId);
        String jti = jwtProvider.extractJti(refreshToken);
        Instant expiresAt = jwtProvider.extractExpiration(refreshToken);

        userService.saveRefreshToken(userId, jti, expiresAt);

        response.addCookie(buildCookie("refresh_token", refreshToken, (int) (refreshTokenExpiry / 1000L), "/api/auth/refresh"));

        getRedirectStrategy().sendRedirect(request, response, redirectUri + "?access_token=" + accessToken);
    }

    private Cookie buildCookie(String name, String value, int maxAge, String path) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 로컬: false, 프로덕션: true
        cookie.setAttribute("SameSite", "Lax");
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        return cookie;
    }
}
