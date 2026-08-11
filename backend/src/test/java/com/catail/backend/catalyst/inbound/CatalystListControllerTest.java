package com.catail.backend.catalyst.inbound;

import com.catail.backend.catalyst.application.CatalystListService;
import com.catail.backend.company.application.CompanyErrorCode;
import com.catail.backend.global.BusinessException;
import com.catail.backend.global.jwt.JwtAuthenticationFilter;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.JsonFieldType.NULL;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CatalystListController.class)
@ExtendWith(RestDocumentationExtension.class)
class CatalystListControllerTest {

    private MockMvc mockMvc;

    @Autowired private WebApplicationContext context;

    @MockitoBean private CatalystListService catalystListService;
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
    @DisplayName("등록된 카탈리스트가 있으면 200과 목록을 반환한다")
    void getList_existingCatalysts_returns200() throws Exception {
        CatalystListItem item = new CatalystListItem(
                1L, "공급망", "SUPPLY_CHAIN", "HBM 공급망 관련 SK하이닉스 계약 동향", "ACTIVE",
                LocalDateTime.of(2026, 8, 11, 12, 0));
        given(catalystListService.getList(1L, 100L)).willReturn(new CatalystListResponse(List.of(item)));

        mockMvc.perform(get("/api/companies/{companyId}/catalysts", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.catalysts[0].title").value("공급망"))
                .andDo(document("catalyst/list",
                        pathParameters(
                                parameterWithName("companyId").description("대상 기업 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.catalysts").description("카탈리스트 목록 (생성일시 내림차순)"),
                                fieldWithPath("data.catalysts[].catalystId").description("카탈리스트 식별자"),
                                fieldWithPath("data.catalysts[].title").description("자동 생성된 제목"),
                                fieldWithPath("data.catalysts[].category").description("관찰 카테고리"),
                                fieldWithPath("data.catalysts[].detail").description("상세내용"),
                                fieldWithPath("data.catalysts[].status").description("모니터링 상태"),
                                fieldWithPath("data.catalysts[].createdAt").description("생성 시각"),
                                fieldWithPath("error").description("에러 정보 (성공 시 null)")
                                        .optional().type(NULL)
                        )
                ));
    }

    @Test
    @DisplayName("등록된 카탈리스트가 없으면 200과 빈 배열을 반환한다")
    void getList_noCatalysts_returnsEmptyArray() throws Exception {
        given(catalystListService.getList(1L, 100L)).willReturn(new CatalystListResponse(List.of()));

        mockMvc.perform(get("/api/companies/{companyId}/catalysts", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.catalysts").isEmpty());
    }

    @Test
    @DisplayName("존재하지 않는 companyId면 404를 반환한다")
    void getList_companyNotFound_returns404() throws Exception {
        given(catalystListService.getList(eq(1L), eq(999L)))
                .willThrow(new BusinessException(CompanyErrorCode.COMPANY_NOT_FOUND));

        mockMvc.perform(get("/api/companies/{companyId}/catalysts", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("COMPANY_004"));
    }
}
