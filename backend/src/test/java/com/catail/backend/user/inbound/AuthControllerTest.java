package com.catail.backend.user.inbound;

import com.catail.backend.global.BusinessException;
import com.catail.backend.global.GlobalErrorCode;
import com.catail.backend.global.jwt.JwtAuthenticationFilter;
import com.catail.backend.user.application.TokenPair;
import com.catail.backend.user.application.UserService;
import com.catail.backend.user.inbound.oauth.CustomOAuth2UserService;
import com.catail.backend.user.inbound.oauth.OAuth2AuthenticationSuccessHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@ExtendWith(RestDocumentationExtension.class)
@TestPropertySource(properties = "jwt.refresh-token-expiry=432000000")
class AuthControllerTest {

    private MockMvc mockMvc;

    @Autowired private WebApplicationContext context;

    @MockitoBean private UserService userService;
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean private CustomOAuth2UserService customOAuth2UserService;
    @MockitoBean private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocs) throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocs))
                .build();

        doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    // ──────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/auth/refresh")
    class Refresh {

        @Test
        @DisplayName("TC-3.1 유효한 Refresh Token → 새 Access Token + Set-Cookie 반환")
        void refresh_validToken_returnsNewTokens() throws Exception {
            given(userService.refresh("valid-rt"))
                    .willReturn(new TokenPair("new-at", "new-rt"));

            mockMvc.perform(post("/api/auth/refresh")
                            .cookie(new Cookie("refresh_token", "valid-rt")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.access_token").value("new-at"))
                    .andExpect(header().exists("Set-Cookie"))
                    .andDo(document("auth/refresh",
                            requestCookies(
                                    cookieWithName("refresh_token").description("현재 유효한 Refresh Token")
                            ),
                            responseFields(
                                    fieldWithPath("success").description("성공 여부"),
                                    fieldWithPath("data").description("응답 데이터"),
                                    fieldWithPath("data.access_token").description("새로 발급된 Access Token"),
                                    fieldWithPath("error").description("에러 정보 (성공 시 null)")
                                            .optional().type(JsonFieldType.NULL)
                            )
                    ));
        }

        @Test
        @DisplayName("TC-3.3~3.5 유효하지 않은 토큰 → 401")
        void refresh_invalidToken_returns401() throws Exception {
            given(userService.refresh("invalid-rt"))
                    .willThrow(new BusinessException(GlobalErrorCode.UNAUTHENTICATED));

            mockMvc.perform(post("/api/auth/refresh")
                            .cookie(new Cookie("refresh_token", "invalid-rt")))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("쿠키 없이 요청 → 401")
        void refresh_noCookie_returns401() throws Exception {
            mockMvc.perform(post("/api/auth/refresh"))
                    .andExpect(status().isUnauthorized());

            verify(userService, never()).refresh(anyString());
        }
    }

    // ──────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/auth/logout")
    class Logout {

        @Test
        @DisplayName("TC-4.1~4.4 정상 로그아웃 → Cookie 삭제 + 200")
        void logout_validToken_returns200() throws Exception {
            mockMvc.perform(post("/api/auth/logout")
                            .cookie(new Cookie("refresh_token", "valid-rt")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(header().exists("Set-Cookie"))
                    .andDo(document("auth/logout",
                            requestCookies(
                                    cookieWithName("refresh_token").description("현재 Refresh Token (만료돼도 허용)")
                            ),
                            responseFields(
                                    fieldWithPath("success").description("성공 여부"),
                                    fieldWithPath("data").description("null")
                                            .optional().type(JsonFieldType.NULL),
                                    fieldWithPath("error").description("null")
                                            .optional().type(JsonFieldType.NULL)
                            )
                    ));

            verify(userService).logout("valid-rt");
        }

        @Test
        @DisplayName("TC-4.5 서명 위조 토큰 → 401")
        void logout_tamperedToken_returns401() throws Exception {
            willThrow(new BusinessException(GlobalErrorCode.UNAUTHENTICATED))
                    .given(userService).logout("tampered-rt");

            mockMvc.perform(post("/api/auth/logout")
                            .cookie(new Cookie("refresh_token", "tampered-rt")))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("쿠키 없이 요청 → 401")
        void logout_noCookie_returns401() throws Exception {
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isUnauthorized());

            verify(userService, never()).logout(anyString());
        }
    }
}
