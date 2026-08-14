package com.catail.backend.signal.inbound;

import com.catail.backend.global.BusinessException;
import com.catail.backend.global.GlobalErrorCode;
import com.catail.backend.global.jwt.JwtAuthenticationFilter;
import com.catail.backend.signal.application.SignalErrorCode;
import com.catail.backend.signal.application.SignalStatusChangeService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SignalStatusChangeController.class)
@ExtendWith(RestDocumentationExtension.class)
class SignalStatusChangeControllerTest {

    private MockMvc mockMvc;

    @Autowired private WebApplicationContext context;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private SignalStatusChangeService signalStatusChangeService;
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocs) throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocs))
                .build();

        doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("정상 요청이면 200과 변경된 상태를 반환한다")
    void changeStatus_valid_returns200() throws Exception {
        SignalStatusResponse response = new SignalStatusResponse(1L, "ADOPTED", LocalDateTime.of(2026, 8, 14, 10, 0));
        given(signalStatusChangeService.changeStatus(1L, 1L, new SignalStatusChangeRequest("ADOPTED")))
                .willReturn(response);

        mockMvc.perform(patch("/api/signals/{signalId}/status", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new SignalStatusChangeRequest("ADOPTED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ADOPTED"))
                .andDo(document("signal/status-change",
                        pathParameters(
                                parameterWithName("signalId").description("상태를 변경할 시그널 식별자")
                        ),
                        requestFields(
                                fieldWithPath("status").description("전환할 상태 (ADOPTED 또는 EXCLUDED)")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.signalId").description("시그널 식별자"),
                                fieldWithPath("data.status").description("전환된 상태"),
                                fieldWithPath("data.updatedAt").description("상태 변경 시각"),
                                fieldWithPath("error").description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("존재하지 않는 signalId면 404를 반환한다")
    void changeStatus_notFound_returns404() throws Exception {
        given(signalStatusChangeService.changeStatus(1L, 999L, new SignalStatusChangeRequest("ADOPTED")))
                .willThrow(new BusinessException(SignalErrorCode.SIGNAL_NOT_FOUND));

        mockMvc.perform(patch("/api/signals/{signalId}/status", 999L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new SignalStatusChangeRequest("ADOPTED"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("SIGNAL_004"));
    }

    @Test
    @DisplayName("허용되지 않는 전환 경로를 요청하면 400을 반환한다")
    void changeStatus_disallowedTransition_returns400() throws Exception {
        given(signalStatusChangeService.changeStatus(1L, 1L, new SignalStatusChangeRequest("PENDING")))
                .willThrow(new BusinessException(GlobalErrorCode.INVALID_INPUT));

        mockMvc.perform(patch("/api/signals/{signalId}/status", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new SignalStatusChangeRequest("PENDING"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("GLOBAL_003"));
    }

    @Test
    @DisplayName("status가 비어있으면 400을 반환한다")
    void changeStatus_blankStatus_returns400() throws Exception {
        mockMvc.perform(patch("/api/signals/{signalId}/status", 1L)
                        .contentType("application/json")
                        .content("{\"status\": \"\"}"))
                .andExpect(status().isBadRequest());
    }
}
