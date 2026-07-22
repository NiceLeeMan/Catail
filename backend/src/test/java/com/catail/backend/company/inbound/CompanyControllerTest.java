package com.catail.backend.company.inbound;

import com.catail.backend.company.application.CompanyDetailService;
import com.catail.backend.company.application.CompanyErrorCode;
import com.catail.backend.company.application.CompanyListService;
import com.catail.backend.company.inbound.read.CompanyDetailResponse;
import com.catail.backend.company.inbound.read.CompanyListItem;
import com.catail.backend.company.inbound.read.CompanyListResponse;
import com.catail.backend.global.BusinessException;
import com.catail.backend.global.jwt.JwtAuthenticationFilter;
import jakarta.servlet.FilterChain;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompanyController.class)
@ExtendWith(RestDocumentationExtension.class)
class CompanyControllerTest {

    private MockMvc mockMvc;

    @Autowired private WebApplicationContext context;

    @MockitoBean private CompanyListService companyListService;
    @MockitoBean private CompanyDetailService companyDetailService;
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

    @Nested
    @DisplayName("GET /api/companies")
    class GetList {

        @Test
        @DisplayName("market=KOSPI로 조회하면 200과 기업 목록을 반환한다")
        void getList_kospi_returns200() throws Exception {
            CompanyListItem item = new CompanyListItem(1L, "삼성전자", "005930", "KOSPI", "반도체", null);
            CompanyListResponse response = new CompanyListResponse(List.of(item), 0, 50, 1, 1, false);
            given(companyListService.getList("KOSPI", null, 0)).willReturn(response);

            mockMvc.perform(get("/api/companies").param("market", "KOSPI"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.items[0].id").value(1))
                    .andExpect(jsonPath("$.data.items[0].companyName").value("삼성전자"))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andDo(document("company/list",
                            responseFields(
                                    fieldWithPath("success").description("성공 여부"),
                                    fieldWithPath("data.items").description("기업 목록"),
                                    fieldWithPath("data.items[].id").description("기업 ID"),
                                    fieldWithPath("data.items[].companyName").description("기업명"),
                                    fieldWithPath("data.items[].stockCode").description("종목코드"),
                                    fieldWithPath("data.items[].market").description("상장시장"),
                                    fieldWithPath("data.items[].industryName").description("업종명")
                                            .optional().type(JsonFieldType.STRING),
                                    fieldWithPath("data.items[].logoUrl").description("로고 URL")
                                            .optional().type(JsonFieldType.NULL),
                                    fieldWithPath("data.page").description("현재 페이지"),
                                    fieldWithPath("data.size").description("페이지 크기"),
                                    fieldWithPath("data.totalElements").description("전체 개수"),
                                    fieldWithPath("data.totalPages").description("전체 페이지 수"),
                                    fieldWithPath("data.hasNext").description("다음 페이지 존재 여부"),
                                    fieldWithPath("error").description("에러 정보 (성공 시 null)")
                                            .optional().type(JsonFieldType.NULL)
                            )
                    ));
        }

        @Test
        @DisplayName("keyword를 함께 전달하면 서비스로 그대로 전달된다")
        void getList_withKeyword_passesKeywordToService() throws Exception {
            CompanyListResponse response = new CompanyListResponse(List.of(), 0, 50, 0, 0, false);
            given(companyListService.getList(eq("KOSPI"), eq("삼성"), eq(0))).willReturn(response);

            mockMvc.perform(get("/api/companies").param("market", "KOSPI").param("keyword", "삼성"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.items").isEmpty());
        }

        @Test
        @DisplayName("market이 없으면 400을 반환한다")
        void getList_missingMarket_returns400() throws Exception {
            given(companyListService.getList(isNull(), isNull(), eq(0)))
                    .willThrow(new BusinessException(CompanyErrorCode.UNSUPPORTED_MARKET));

            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("지원하지 않는 market이면 400을 반환한다")
        void getList_unsupportedMarket_returns400() throws Exception {
            given(companyListService.getList("NASDAQ", null, 0))
                    .willThrow(new BusinessException(CompanyErrorCode.UNSUPPORTED_MARKET));

            mockMvc.perform(get("/api/companies").param("market", "NASDAQ"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("COMPANY_001"));
        }

        @Test
        @DisplayName("page가 음수이면 400을 반환한다")
        void getList_negativePage_returns400() throws Exception {
            mockMvc.perform(get("/api/companies").param("market", "KOSPI").param("page", "-1"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/companies/{companyId}")
    class GetDetail {

        @Test
        @DisplayName("존재하는 기업이면 200과 상세정보를 반환한다")
        void getDetail_existing_returns200() throws Exception {
            CompanyDetailResponse response = new CompanyDetailResponse(1L, "KOSPI", "005930", "삼성전자", "반도체", null);
            given(companyDetailService.getDetail(1L)).willReturn(response);

            mockMvc.perform(get("/api/companies/{companyId}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.companyName").value("삼성전자"))
                    .andDo(document("company/detail",
                            responseFields(
                                    fieldWithPath("success").description("성공 여부"),
                                    fieldWithPath("data.id").description("기업 ID"),
                                    fieldWithPath("data.market").description("상장시장"),
                                    fieldWithPath("data.stockCode").description("종목코드"),
                                    fieldWithPath("data.companyName").description("기업명"),
                                    fieldWithPath("data.industryName").description("업종명")
                                            .optional().type(JsonFieldType.STRING),
                                    fieldWithPath("data.logoUrl").description("로고 URL")
                                            .optional().type(JsonFieldType.NULL),
                                    fieldWithPath("error").description("에러 정보 (성공 시 null)")
                                            .optional().type(JsonFieldType.NULL)
                            )
                    ));
        }

        @Test
        @DisplayName("존재하지 않는 기업이면 404를 반환한다")
        void getDetail_notFound_returns404() throws Exception {
            given(companyDetailService.getDetail(999L))
                    .willThrow(new BusinessException(CompanyErrorCode.COMPANY_NOT_FOUND));

            mockMvc.perform(get("/api/companies/{companyId}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("COMPANY_004"));
        }
    }
}
