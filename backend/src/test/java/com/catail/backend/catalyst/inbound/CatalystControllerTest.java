package com.catail.backend.catalyst.inbound;

import com.catail.backend.catalyst.application.CatalystCreateService;
import com.catail.backend.catalyst.application.CatalystDeleteService;
import com.catail.backend.catalyst.application.CatalystErrorCode;
import com.catail.backend.company.application.CompanyErrorCode;
import com.catail.backend.global.BusinessException;
import com.catail.backend.global.jwt.JwtAuthenticationFilter;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CatalystController.class)
@ExtendWith(RestDocumentationExtension.class)
class CatalystControllerTest {

    private MockMvc mockMvc;

    @Autowired private WebApplicationContext context;

    @MockitoBean private CatalystCreateService catalystCreateService;
    @MockitoBean private CatalystDeleteService catalystDeleteService;
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

    private String requestJson(String category, String detail, String status) {
        return """
                {
                  "companyId": 1,
                  "category": "%s",
                  "detail": "%s",
                  "status": "%s"
                }
                """.formatted(category, detail, status);
    }

    @Nested
    @DisplayName("POST /api/catalysts")
    class Create {

        @Test
        @DisplayName("정상 요청이면 201과 생성된 카탈리스트를 반환한다")
        void create_valid_returns201() throws Exception {
            CatalystCreateResponse response = new CatalystCreateResponse(
                    1L, "공급망", "KOSPI", "005930", "SUPPLY_CHAIN",
                    "HBM 공급망 관련 SK하이닉스 및 주요 파운드리向 계약 동향", "ACTIVE", LocalDateTime.of(2026, 8, 11, 12, 0));
            given(catalystCreateService.create(eq(1L), any())).willReturn(response);

            mockMvc.perform(post("/api/catalysts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson("SUPPLY_CHAIN", "HBM 공급망 관련 SK하이닉스 및 주요 파운드리向 계약 동향", "ACTIVE")))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("공급망"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                    .andDo(document("catalyst/create",
                            requestFields(
                                    fieldWithPath("companyId").description("대상 기업 ID"),
                                    fieldWithPath("category").description("관찰 카테고리 (6개 값 중 하나)"),
                                    fieldWithPath("detail").description("상세내용 (10~300자)"),
                                    fieldWithPath("status").description("초기 상태 (ACTIVE 또는 INACTIVE)")
                            ),
                            responseFields(
                                    fieldWithPath("success").description("성공 여부"),
                                    fieldWithPath("data.catalystId").description("카탈리스트 식별자"),
                                    fieldWithPath("data.title").description("자동 생성된 제목"),
                                    fieldWithPath("data.market").description("대상 기업 상장시장"),
                                    fieldWithPath("data.stockCode").description("대상 기업 종목코드"),
                                    fieldWithPath("data.category").description("관찰 카테고리"),
                                    fieldWithPath("data.detail").description("상세내용"),
                                    fieldWithPath("data.status").description("현재 상태"),
                                    fieldWithPath("data.createdAt").description("생성 시각"),
                                    fieldWithPath("error").description("에러 정보 (성공 시 null)")
                                            .optional().type(JsonFieldType.NULL)
                            )
                    ));
        }

        @Test
        @DisplayName("대상 기업이 없으면 404를 반환한다")
        void create_companyNotFound_returns404() throws Exception {
            given(catalystCreateService.create(eq(1L), any()))
                    .willThrow(new BusinessException(CompanyErrorCode.COMPANY_NOT_FOUND));

            mockMvc.perform(post("/api/catalysts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson("SUPPLY_CHAIN", "HBM 공급망 관련 SK하이닉스 계약 동향", "ACTIVE")))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("COMPANY_004"));
        }

        @Test
        @DisplayName("동일 기업 카탈리스트 상한(3개)을 초과하면 400을 반환한다")
        void create_limitExceeded_returns400() throws Exception {
            given(catalystCreateService.create(eq(1L), any()))
                    .willThrow(new BusinessException(CatalystErrorCode.CATALYST_LIMIT_EXCEEDED));

            mockMvc.perform(post("/api/catalysts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson("SUPPLY_CHAIN", "HBM 공급망 관련 SK하이닉스 계약 동향", "ACTIVE")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("CATALYST_001"));
        }

        @Test
        @DisplayName("category가 정의된 값이 아니면 400을 반환한다")
        void create_blankCategory_returns400() throws Exception {
            mockMvc.perform(post("/api/catalysts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson("", "HBM 공급망 관련 SK하이닉스 계약 동향", "ACTIVE")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("detail이 10자 미만이면 400을 반환한다")
        void create_detailTooShort_returns400() throws Exception {
            mockMvc.perform(post("/api/catalysts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson("SUPPLY_CHAIN", "짧음", "ACTIVE")))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/catalysts/{catalystId}")
    class Delete {

        @Test
        @DisplayName("소유자가 요청하면 204를 반환한다")
        void delete_owner_returns204() throws Exception {
            mockMvc.perform(delete("/api/catalysts/{catalystId}", 1L))
                    .andExpect(status().isNoContent())
                    .andDo(document("catalyst/delete",
                            pathParameters(
                                    parameterWithName("catalystId").description("삭제할 카탈리스트 식별자")
                            )
                    ));
        }

        @Test
        @DisplayName("존재하지 않는 catalystId면 404를 반환한다")
        void delete_notFound_returns404() throws Exception {
            willThrow(new BusinessException(CatalystErrorCode.CATALYST_NOT_FOUND))
                    .given(catalystDeleteService).delete(eq(1L), eq(999L));

            mockMvc.perform(delete("/api/catalysts/{catalystId}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("CATALYST_002"));
        }

        @Test
        @DisplayName("소유자가 아니면 403을 반환한다")
        void delete_notOwner_returns403() throws Exception {
            willThrow(new BusinessException(CatalystErrorCode.CATALYST_ACCESS_DENIED))
                    .given(catalystDeleteService).delete(eq(1L), eq(2L));

            mockMvc.perform(delete("/api/catalysts/{catalystId}", 2L))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("CATALYST_003"));
        }
    }
}
