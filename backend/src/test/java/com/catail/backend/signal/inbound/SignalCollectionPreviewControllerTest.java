package com.catail.backend.signal.inbound;

import com.catail.backend.global.jwt.JwtAuthenticationFilter;
import com.catail.backend.signal.application.SignalNewsCollectionService;
import com.catail.backend.signal.application.SignalSearchQueryGenerationService;
import com.catail.backend.signal.outbound.naver.NaverNewsItem;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 정식 TDD 대상이 아니다 — 검색어 생성(LLM)/네이버 뉴스 수집은 출력을 사람이 직접 눈으로 확인하는
 * 검증용 엔드포인트라 Red 테스트를 두지 않는다. 이 테스트는 Swagger에 노출할 REST Docs 스니펫을
 * 만들기 위한 최소한의 문서화 테스트다.
 */
@WebMvcTest(SignalCollectionPreviewController.class)
@ExtendWith(RestDocumentationExtension.class)
class SignalCollectionPreviewControllerTest {

    private MockMvc mockMvc;

    @Autowired private WebApplicationContext context;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private SignalSearchQueryGenerationService signalSearchQueryGenerationService;
    @MockitoBean private SignalNewsCollectionService signalNewsCollectionService;
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
    }

    @Test
    @DisplayName("카탈리스트 정보로 검색어를 생성해 반환한다")
    void generateSearchQueries_validRequest_returns200() throws Exception {
        given(signalSearchQueryGenerationService.generate(100L, "SUPPLY_CHAIN", "HBM 공급망 관련 동향"))
                .willReturn(List.of("삼성전자 HBM 공급망", "삼성전자 SK하이닉스 HBM 계약"));

        SignalSearchQueriesRequest request = new SignalSearchQueriesRequest(
                100L, "SUPPLY_CHAIN", "HBM 공급망 관련 동향");

        mockMvc.perform(post("/api/internal/signals/search-queries")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("signal/search-queries",
                        requestFields(
                                fieldWithPath("companyId").description("대상 기업 ID"),
                                fieldWithPath("category").description("카탈리스트 카테고리"),
                                fieldWithPath("detail").description("카탈리스트 상세내용")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.queries").description("LLM이 생성한 검색어 목록"),
                                fieldWithPath("error").description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("검색어 목록으로 네이버 뉴스를 병렬 조회해 반환한다")
    void fetchNewsArticles_validRequest_returns200() throws Exception {
        Map<String, List<NaverNewsItem>> resultsByQuery = new LinkedHashMap<>();
        resultsByQuery.put("삼성전자 HBM 공급망", List.of(
                new NaverNewsItem("삼성전자 HBM 공급 확대", "https://origin.example.com/1",
                        "https://n.news.naver.com/1", "요약 내용", "Mon, 10 Aug 2026 09:00:00 +0900")
        ));
        given(signalNewsCollectionService.collect(List.of("삼성전자 HBM 공급망"))).willReturn(resultsByQuery);

        SignalNewsArticlesRequest request = new SignalNewsArticlesRequest(List.of("삼성전자 HBM 공급망"));

        mockMvc.perform(post("/api/internal/signals/news-articles")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("signal/news-articles",
                        requestFields(
                                fieldWithPath("queries").description("네이버 뉴스를 조회할 검색어 목록")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.results").description("검색어별 조회 결과"),
                                fieldWithPath("data.results[].query").description("검색어"),
                                fieldWithPath("data.results[].articles").description("해당 검색어로 조회된 네이버 뉴스 원본 목록"),
                                fieldWithPath("data.results[].articles[].title").description("기사 제목 (HTML 태그 미제거 원본)"),
                                fieldWithPath("data.results[].articles[].originallink").description("언론사 원문 링크"),
                                fieldWithPath("data.results[].articles[].link").description("네이버 뉴스 링크"),
                                fieldWithPath("data.results[].articles[].description").description("기사 요약 (HTML 태그 미제거 원본)"),
                                fieldWithPath("data.results[].articles[].pubDate").description("발행일시 (RFC822 원본 문자열)"),
                                fieldWithPath("error").description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }
}
