package com.catail.backend.disclosure.inbound;

import com.catail.backend.disclosure.application.DisclosureErrorCode;
import com.catail.backend.disclosure.application.DisclosureListService;
import com.catail.backend.disclosure.inbound.read.DisclosureItem;
import com.catail.backend.disclosure.inbound.read.DisclosureListResponse;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DisclosureController.class)
@ExtendWith(RestDocumentationExtension.class)
class DisclosureControllerTest {

    private MockMvc mockMvc;

    @Autowired private WebApplicationContext context;

    @MockitoBean private DisclosureListService disclosureListService;
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
    @DisplayName("GET /api/companies/{companyId}/disclosures")
    class GetList {

        @Test
        @DisplayName("정상 조회하면 200과 공시 목록을 반환한다")
        void getList_existing_returns200() throws Exception {
            DisclosureItem item = new DisclosureItem(
                    1L, "OPEN_DART", "주요사항보고서", "삼성전자", LocalDate.of(2024, 1, 1),
                    List.of("유"), "https://dart.fss.or.kr/dsaf001/main.do?rcpNo=20240101000123");
            DisclosureListResponse response = new DisclosureListResponse(
                    List.of(item), "next-cursor", true, LocalDateTime.of(2024, 1, 2, 15, 0), true);
            given(disclosureListService.getList(1L, null, 20)).willReturn(response);

            mockMvc.perform(get("/api/companies/{companyId}/disclosures", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.items[0].reportName").value("주요사항보고서"))
                    .andExpect(jsonPath("$.data.hasNext").value(true))
                    .andDo(document("disclosure/list",
                            responseFields(
                                    fieldWithPath("success").description("성공 여부"),
                                    fieldWithPath("data.items").description("공시 목록"),
                                    fieldWithPath("data.items[].id").description("공시 ID"),
                                    fieldWithPath("data.items[].provider").description("공시 제공처"),
                                    fieldWithPath("data.items[].reportName").description("보고서명"),
                                    fieldWithPath("data.items[].submitterName").description("제출인명"),
                                    fieldWithPath("data.items[].receivedDate").description("접수일자"),
                                    fieldWithPath("data.items[].remarkCodes").description("비고 코드 목록"),
                                    fieldWithPath("data.items[].sourceUrl").description("공시 원문 URL"),
                                    fieldWithPath("data.nextCursor").description("다음 페이지 커서")
                                            .optional().type(JsonFieldType.STRING),
                                    fieldWithPath("data.hasNext").description("다음 페이지 존재 여부"),
                                    fieldWithPath("data.lastSyncedAt").description("마지막 동기화 시각")
                                            .optional().type(JsonFieldType.STRING),
                                    fieldWithPath("data.initialSyncCompleted").description("최초 수집 완료 여부"),
                                    fieldWithPath("error").description("에러 정보 (성공 시 null)")
                                            .optional().type(JsonFieldType.NULL)
                            )
                    ));
        }

        @Test
        @DisplayName("cursor를 전달하면 서비스로 그대로 전달된다")
        void getList_withCursor_passesToService() throws Exception {
            DisclosureListResponse response = new DisclosureListResponse(List.of(), null, false, null, false);
            given(disclosureListService.getList(eq(1L), eq("abc"), eq(20))).willReturn(response);

            mockMvc.perform(get("/api/companies/{companyId}/disclosures", 1L).param("cursor", "abc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.items").isEmpty());
        }

        @Test
        @DisplayName("존재하지 않는 기업이면 404를 반환한다")
        void getList_companyNotFound_returns404() throws Exception {
            given(disclosureListService.getList(999L, null, 20))
                    .willThrow(new BusinessException(DisclosureErrorCode.COMPANY_NOT_FOUND));

            mockMvc.perform(get("/api/companies/{companyId}/disclosures", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("DISCLOSURE_001"));
        }

        @Test
        @DisplayName("size가 100을 초과하면 400을 반환한다")
        void getList_sizeOverLimit_returns400() throws Exception {
            mockMvc.perform(get("/api/companies/{companyId}/disclosures", 1L).param("size", "101"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("size가 0 이하이면 400을 반환한다")
        void getList_sizeZero_returns400() throws Exception {
            mockMvc.perform(get("/api/companies/{companyId}/disclosures", 1L).param("size", "0"))
                    .andExpect(status().isBadRequest());
        }
    }
}
