package com.catail.backend.signal.inbound;

import com.catail.backend.catalyst.application.CatalystErrorCode;
import com.catail.backend.global.BusinessException;
import com.catail.backend.global.jwt.JwtAuthenticationFilter;
import com.catail.backend.signal.application.SignalTimelineService;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SignalTimelineController.class)
@ExtendWith(RestDocumentationExtension.class)
class SignalTimelineControllerTest {

    private MockMvc mockMvc;

    @Autowired private WebApplicationContext context;

    @MockitoBean private SignalTimelineService signalTimelineService;
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
    @DisplayName("정상 조회하면 200과 채택된 시그널 목록을 반환한다")
    void getTimeline_existing_returns200() throws Exception {
        SignalListItem item = new SignalListItem(1L, "삼성전자 HBM 공급 확대", "요약 내용", "연합뉴스",
                "https://n.news.naver.com/1", OffsetDateTime.parse("2026-08-10T09:00:00+09:00"), null, "ADOPTED");
        given(signalTimelineService.getTimeline(1L, 10L)).willReturn(new SignalTimelineResponse(List.of(item)));

        mockMvc.perform(get("/api/catalysts/{catalystId}/signals/timeline", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.signals[0].status").value("ADOPTED"))
                .andDo(document("signal/timeline",
                        pathParameters(
                                parameterWithName("catalystId").description("조회 대상 카탈리스트 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.signals").description("채택된 시그널 목록 (발행일시 내림차순)"),
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
                                fieldWithPath("data.signals[].status").description("시그널 상태 (항상 ADOPTED)"),
                                fieldWithPath("error").description("에러 정보 (성공 시 null)")
                                        .optional().type(JsonFieldType.NULL)
                        )
                ));
    }

    @Test
    @DisplayName("채택된 시그널이 없으면 200과 빈 배열을 반환한다")
    void getTimeline_noAdoptedSignals_returnsEmptyArray() throws Exception {
        given(signalTimelineService.getTimeline(1L, 10L)).willReturn(new SignalTimelineResponse(List.of()));

        mockMvc.perform(get("/api/catalysts/{catalystId}/signals/timeline", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.signals").isEmpty());
    }

    @Test
    @DisplayName("존재하지 않는 catalystId면 404를 반환한다")
    void getTimeline_catalystNotFound_returns404() throws Exception {
        given(signalTimelineService.getTimeline(1L, 999L))
                .willThrow(new BusinessException(CatalystErrorCode.CATALYST_NOT_FOUND));

        mockMvc.perform(get("/api/catalysts/{catalystId}/signals/timeline", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("CATALYST_002"));
    }

    @Test
    @DisplayName("소유자가 아니면 403을 반환한다")
    void getTimeline_notOwner_returns403() throws Exception {
        given(signalTimelineService.getTimeline(1L, 10L))
                .willThrow(new BusinessException(CatalystErrorCode.CATALYST_ACCESS_DENIED));

        mockMvc.perform(get("/api/catalysts/{catalystId}/signals/timeline", 10L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("CATALYST_003"));
    }
}
