package com.catail.backend.user.inbound;

import com.catail.backend.global.ApiResponse;
import com.catail.backend.global.BusinessException;
import com.catail.backend.global.GlobalErrorCode;
import com.catail.backend.user.application.TokenPair;
import com.catail.backend.user.application.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refresh(
            HttpServletRequest request, HttpServletResponse response) {

        String rawToken = extractRefreshTokenFromCookie(request)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.UNAUTHENTICATED));

        TokenPair tokens = userService.refresh(rawToken);

        response.addCookie(buildRefreshTokenCookie(tokens.refreshToken()));
        return ResponseEntity.ok(ApiResponse.ok(Map.of("access_token", tokens.accessToken())));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request, HttpServletResponse response) {

        String rawToken = extractRefreshTokenFromCookie(request)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.UNAUTHENTICATED));

        userService.logout(rawToken);

        Cookie expired = new Cookie("refresh_token", "");
        expired.setMaxAge(0);
        expired.setPath("/api/auth");
        expired.setHttpOnly(true);
        response.addCookie(expired);

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    private Optional<String> extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> "refresh_token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    private Cookie buildRefreshTokenCookie(String value) {
        Cookie cookie = new Cookie("refresh_token", value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setAttribute("SameSite", "Lax");
        cookie.setPath("/api/auth");
        cookie.setMaxAge((int) (refreshTokenExpiry / 1000L));
        return cookie;
    }
}
