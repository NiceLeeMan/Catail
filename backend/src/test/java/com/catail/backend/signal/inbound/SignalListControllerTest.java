package com.catail.backend.signal.inbound;

import com.catail.backend.catalyst.application.CatalystErrorCode;
import com.catail.backend.global.BusinessException;
import com.catail.backend.global.GlobalErrorCode;
import com.catail.backend.global.jwt.JwtAuthenticationFilter;
import com.catail.backend.signal.application.SignalListService;
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
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SignalListController.class)
@ExtendWith(RestDocumentationExtension.class)
class SignalListControllerTest {

    private MockMvc mockMvc;

    @Autowired private WebApplicationContext context;

    @MockitoBean private SignalListService signalListService;
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
    @DisplayName("정상 조회하면 200과 시그널 목록을 반환한다")
    void getList_existing_returns200() throws Exception {
        SignalListItem item = new SignalListItem(1L, "삼성전자 HBM 공급 확대", "요약 내용", "연합뉴스",
                "https://n.news.naver.com/1", OffsetDateTime.parse("2026-08-10T09:00:00+09:00"), null, "PENDING");
        SignalListResponse response = new SignalListResponse(List.of(item), "next-cursor", true);
        given(signalListService.getList(1L, 10L, "PENDING", null, 7)).willReturn(response);

        mockMvc.perform(get("/api/catalysts/{catalystId}/signals", 10L).param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.signals[0].title").value("삼성전자 HBM 공급 확대"))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andDo(document("signal/list",
                        pathParameters(
                                parameterWithName("catalystId").description("조회 대상 카탈리스트 ID")
                        ),
                        queryParameters(
                                parameterWithName("status").description("조회할 시그널 상태 (PENDING 또는 EXCLUDED)"),
                                parameterWithName("size").description("한 번에 조회할 개수 (기본값 7)").optional(),
                                parameterWithName("cursor").description("이전 응답의 nextCursor 값").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.signals").description("시그널 목록 (생성일시 내림차순)"),
                                fieldWithPath("data.signals[].signalId").description("시그널 식별자"),
                                fieldWithPath("data.signals[].title").description("기사 제목"),
                                fieldWithPath("data.signals[].description").description("기사 요약"),
                                fieldWithPath("data.signals[].press").description("언론사명 (null 가능)")
                                        .optional().type(JsonFieldType.STRING),
                                fieldWithPath("data.signals[].link").description("원문(네이버 뉴스) 링크"),
                                fieldWithPath("data.signals[].pubDate").description("기사 발행일시"),
                                fieldWithPath("data.signals[].relevanceReason")
                                        .description("관련성 판단 이유 (관련성 검사가 스텁이라 현재는 항상 null)")
                                        .optional().type(JsonFieldType.NULL),
                                fieldWithPath("data.signals[].status").description("시그널 상태 (PENDING 또는 EXCLUDED)"),
                                fieldWithPath("data.nextCursor").description("다음 페이지 커서")
                                        .optional().type(JsonFieldType.STRING),
                                fieldWithPath("data.hasNext").description("다음 페이지 존재 여부"),
                                fieldWithPath("error").description("에러 정보 (성공 시 null)")
                                        .optional().type(JsonFieldType.NULL)
                        )
                ));
    }

    @Test
    @DisplayName("조건에 맞는 시그널이 없으면 200과 빈 배열을 반환한다")
    void getList_noSignals_returnsEmptyArray() throws Exception {
        given(signalListService.getList(1L, 10L, "PENDING", null, 7))
                .willReturn(new SignalListResponse(List.of(), null, false));

        mockMvc.perform(get("/api/catalysts/{catalystId}/signals", 10L).param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.signals").isEmpty())
                .andExpect(jsonPath("$.data.nextCursor").doesNotExist());
    }

    @Test
    @DisplayName("cursor를 전달하면 서비스로 그대로 전달된다")
    void getList_withCursor_passesToService() throws Exception {
        given(signalListService.getList(eq(1L), eq(10L), eq("PENDING"), eq("abc"), eq(7)))
                .willReturn(new SignalListResponse(List.of(), null, false));

        mockMvc.perform(get("/api/catalysts/{catalystId}/signals", 10L)
                        .param("status", "PENDING")
                        .param("cursor", "abc"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 catalystId면 404를 반환한다")
    void getList_catalystNotFound_returns404() throws Exception {
        given(signalListService.getList(1L, 999L, "PENDING", null, 7))
                .willThrow(new BusinessException(CatalystErrorCode.CATALYST_NOT_FOUND));

        mockMvc.perform(get("/api/catalysts/{catalystId}/signals", 999L).param("status", "PENDING"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("CATALYST_002"));
    }

    @Test
    @DisplayName("소유자가 아니면 403을 반환한다")
    void getList_notOwner_returns403() throws Exception {
        given(signalListService.getList(1L, 10L, "PENDING", null, 7))
                .willThrow(new BusinessException(CatalystErrorCode.CATALYST_ACCESS_DENIED));

        mockMvc.perform(get("/api/catalysts/{catalystId}/signals", 10L).param("status", "PENDING"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("CATALYST_003"));
    }

    @Test
    @DisplayName("status가 PENDING/EXCLUDED가 아니면 400을 반환한다")
    void getList_invalidStatus_returns400() throws Exception {
        given(signalListService.getList(1L, 10L, "ADOPTED", null, 7))
                .willThrow(new BusinessException(GlobalErrorCode.INVALID_INPUT));

        mockMvc.perform(get("/api/catalysts/{catalystId}/signals", 10L).param("status", "ADOPTED"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("size가 100을 초과하면 400을 반환한다")
    void getList_sizeOverLimit_returns400() throws Exception {
        mockMvc.perform(get("/api/catalysts/{catalystId}/signals", 10L)
                        .param("status", "PENDING").param("size", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("size가 0 이하이면 400을 반환한다")
    void getList_sizeZero_returns400() throws Exception {
        mockMvc.perform(get("/api/catalysts/{catalystId}/signals", 10L)
                        .param("status", "PENDING").param("size", "0"))
                .andExpect(status().isBadRequest());
    }
}
