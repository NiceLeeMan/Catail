package com.catail.backend.catalyst.inbound;

import com.catail.backend.catalyst.application.CatalystBasicInfo;
import com.catail.backend.catalyst.inbound.create.CatalystCreateResponse;
import com.catail.backend.catalyst.inbound.delete.CatalystDeleteResponse;
import com.catail.backend.catalyst.inbound.read.CatalystInfoResponse;
import com.catail.backend.catalyst.inbound.read.CatalystListItemResponse;
import com.catail.backend.catalyst.application.CatalystMonitoringOperation;
import com.catail.backend.catalyst.application.CatalystService;
import com.catail.backend.catalyst.inbound.update.CatalystStatusResponse;
import com.catail.backend.catalyst.inbound.update.CatalystUpdateResponse;
import com.catail.backend.catalyst.inbound.update.ChangeCatalystStatusRequest;
import com.catail.backend.catalyst.inbound.create.CreateCatalystRequest;
import com.catail.backend.catalyst.inbound.update.UpdateCatalystBasicInfoRequest;
import com.catail.backend.global.BusinessException;
import com.catail.backend.global.GlobalErrorCode;
import com.catail.backend.global.PageResponse;
import com.catail.backend.catalyst.application.CatalystErrorCode;
import com.catail.backend.global.jwt.JwtAuthenticationFilter;
import com.catail.backend.global.web.CurrentUserIdArgumentResolver;
import jakarta.servlet.FilterChain;
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
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CatalystController.class)
@ExtendWith(RestDocumentationExtension.class)
class CatalystControllerTest {

    private MockMvc mockMvc;

    @Autowired private WebApplicationContext context;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private CatalystService catalystService;
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean private CurrentUserIdArgumentResolver currentUserIdArgumentResolver;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocs) throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocs))
                .build();

        doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        given(currentUserIdArgumentResolver.supportsParameter(any())).willReturn(true);
        given(currentUserIdArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(1L);
    }

    @Nested
    @DisplayName("POST /api/catalysts")
    class Create {

        @Test
        @DisplayName("유효한 요청이면 201과 생성된 카탈리스트를 반환한다")
        void create_validRequest_returns201() throws Exception {
            CreateCatalystRequest request = new CreateCatalystRequest(
                    "제목", "a".repeat(50), List.of(1L), "ACTIVE");
            CatalystCreateResponse response = new CatalystCreateResponse(
                    1L, "제목", "a".repeat(50), "ACTIVE", List.of("IT"), LocalDateTime.now());
            given(catalystService.create(1L, "제목", "a".repeat(50), List.of(1L), "ACTIVE"))
                    .willReturn(response);

            mockMvc.perform(post("/api/catalysts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andDo(document("catalyst/create",
                            responseFields(
                                    fieldWithPath("success").description("성공 여부"),
                                    fieldWithPath("data.id").description("카탈리스트 ID"),
                                    fieldWithPath("data.title").description("제목"),
                                    fieldWithPath("data.content").description("본문"),
                                    fieldWithPath("data.status").description("상태"),
                                    fieldWithPath("data.industries").description("업종 목록"),
                                    fieldWithPath("data.createdAt").description("생성일시"),
                                    fieldWithPath("error").description("에러 정보 (성공 시 null)")
                                            .optional().type(JsonFieldType.NULL)
                            )
                    ));
        }

        @Test
        @DisplayName("title이 비어있으면 400을 반환한다")
        void create_blankTitle_returns400() throws Exception {
            CreateCatalystRequest request = new CreateCatalystRequest("", "a".repeat(50), List.of(1L), "ACTIVE");

            mockMvc.perform(post("/api/catalysts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("industryIds가 비어있으면 400을 반환한다")
        void create_emptyIndustryIds_returns400() throws Exception {
            CreateCatalystRequest request = new CreateCatalystRequest("제목", "a".repeat(50), List.of(), "ACTIVE");

            mockMvc.perform(post("/api/catalysts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/catalysts")
    class GetList {

        @Test
        @DisplayName("기본 page=0으로 목록을 조회하면 200과 페이지 정보를 반환한다")
        void getList_defaultPage_returns200() throws Exception {
            CatalystListItemResponse item = new CatalystListItemResponse(
                    1L, "제목", "ACTIVE", List.of("IT"), 0, LocalDateTime.now());
            PageResponse<CatalystListItemResponse> page =
                    new PageResponse<>(List.of(item), 0, 20, 1, 1, false);
            given(catalystService.getList(1L, 0)).willReturn(page);

            mockMvc.perform(get("/api/catalysts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.items[0].id").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andDo(document("catalyst/list",
                            responseFields(
                                    fieldWithPath("success").description("성공 여부"),
                                    fieldWithPath("data.items").description("카탈리스트 목록"),
                                    fieldWithPath("data.items[].id").description("카탈리스트 ID"),
                                    fieldWithPath("data.items[].title").description("제목"),
                                    fieldWithPath("data.items[].status").description("상태"),
                                    fieldWithPath("data.items[].industryTags").description("업종 태그 목록"),
                                    fieldWithPath("data.items[].pendingSignalCount").description("대기중인 시그널 수"),
                                    fieldWithPath("data.items[].createdAt").description("생성일시"),
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
    }

    @Nested
    @DisplayName("GET /api/catalysts/{id}")
    class GetDetail {

        @Test
        @DisplayName("존재하는 카탈리스트를 조회하면 200을 반환한다")
        void getDetail_existing_returns200() throws Exception {
            CatalystBasicInfo basicInfo = new CatalystBasicInfo(
                    "제목", "a".repeat(50), List.of("IT"), LocalDateTime.now(), LocalDateTime.now());
            CatalystMonitoringOperation monitoringOperation = new CatalystMonitoringOperation(
                    "ACTIVE", List.of("검색어"), 4, null, null);
            CatalystInfoResponse response = new CatalystInfoResponse(basicInfo, monitoringOperation);
            given(catalystService.getDetail(1L, 1L)).willReturn(response);

            mockMvc.perform(get("/api/catalysts/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.basicInfo.title").value("제목"))
                    .andDo(document("catalyst/detail",
                            responseFields(
                                    fieldWithPath("success").description("성공 여부"),
                                    fieldWithPath("data.basicInfo.title").description("제목"),
                                    fieldWithPath("data.basicInfo.content").description("본문"),
                                    fieldWithPath("data.basicInfo.industries").description("업종 목록"),
                                    fieldWithPath("data.basicInfo.createdAt").description("생성일시"),
                                    fieldWithPath("data.basicInfo.updatedAt").description("수정일시"),
                                    fieldWithPath("data.monitoringOperation.status").description("모니터링 상태"),
                                    fieldWithPath("data.monitoringOperation.searchConditions").description("모니터링 검색조건 목록"),
                                    fieldWithPath("data.monitoringOperation.searchIntervalHours").description("탐색 주기(시간)"),
                                    fieldWithPath("data.monitoringOperation.lastSearchedAt").description("마지막 탐색 시각")
                                            .optional().type(JsonFieldType.NULL),
                                    fieldWithPath("data.monitoringOperation.activatedAt").description("모니터링 시작 시각")
                                            .optional().type(JsonFieldType.NULL),
                                    fieldWithPath("error").description("에러 정보 (성공 시 null)")
                                            .optional().type(JsonFieldType.NULL)
                            )
                    ));
        }

        @Test
        @DisplayName("존재하지 않는 카탈리스트를 조회하면 404를 반환한다")
        void getDetail_notFound_returns404() throws Exception {
            given(catalystService.getDetail(999L, 1L))
                    .willThrow(new BusinessException(CatalystErrorCode.NOT_FOUND));

            mockMvc.perform(get("/api/catalysts/{id}", 999L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/catalysts/{id}")
    class Delete {

        @Test
        @DisplayName("본인 소유 카탈리스트를 삭제하면 200과 status/deletedAt을 반환한다")
        void delete_owned_returns200() throws Exception {
            CatalystDeleteResponse response = new CatalystDeleteResponse("ENDED", LocalDateTime.now());
            given(catalystService.delete(1L, 1L)).willReturn(response);

            mockMvc.perform(delete("/api/catalysts/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("ENDED"))
                    .andDo(document("catalyst/delete",
                            responseFields(
                                    fieldWithPath("success").description("성공 여부"),
                                    fieldWithPath("data.status").description("삭제 후 상태(ENDED)"),
                                    fieldWithPath("data.deletedAt").description("삭제 시각"),
                                    fieldWithPath("error").description("에러 정보 (성공 시 null)")
                                            .optional().type(JsonFieldType.NULL)
                            )
                    ));
        }

        @Test
        @DisplayName("존재하지 않는 카탈리스트를 삭제하면 404를 반환한다")
        void delete_notFound_returns404() throws Exception {
            org.mockito.BDDMockito.willThrow(new BusinessException(CatalystErrorCode.NOT_FOUND))
                    .given(catalystService).delete(999L, 1L);

            mockMvc.perform(delete("/api/catalysts/{id}", 999L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/catalysts/{id}")
    class UpdateBasicInfo {

        @Test
        @DisplayName("유효한 요청이면 200과 수정된 기본정보를 반환한다")
        void updateBasicInfo_validRequest_returns200() throws Exception {
            UpdateCatalystBasicInfoRequest request = new UpdateCatalystBasicInfoRequest(
                    "새 제목", "a".repeat(50), List.of(1L));
            CatalystUpdateResponse response = new CatalystUpdateResponse(
                    "새 제목", "a".repeat(50), List.of("IT"), LocalDateTime.now());
            given(catalystService.updateBasicInfo(1L, 1L, "새 제목", "a".repeat(50), List.of(1L)))
                    .willReturn(response);

            mockMvc.perform(put("/api/catalysts/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("새 제목"))
                    .andDo(document("catalyst/update-basic-info",
                            responseFields(
                                    fieldWithPath("success").description("성공 여부"),
                                    fieldWithPath("data.title").description("수정된 제목"),
                                    fieldWithPath("data.content").description("수정된 본문"),
                                    fieldWithPath("data.industries").description("수정된 업종 목록"),
                                    fieldWithPath("data.updatedAt").description("수정일시"),
                                    fieldWithPath("error").description("에러 정보 (성공 시 null)")
                                            .optional().type(JsonFieldType.NULL)
                            )
                    ));
        }

        @Test
        @DisplayName("title이 비어있으면 400을 반환한다")
        void updateBasicInfo_blankTitle_returns400() throws Exception {
            UpdateCatalystBasicInfoRequest request = new UpdateCatalystBasicInfoRequest(
                    "", "a".repeat(50), List.of(1L));

            mockMvc.perform(put("/api/catalysts/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("존재하지 않는 카탈리스트를 수정하면 404를 반환한다")
        void updateBasicInfo_notFound_returns404() throws Exception {
            UpdateCatalystBasicInfoRequest request = new UpdateCatalystBasicInfoRequest(
                    "제목", "a".repeat(50), List.of(1L));
            given(catalystService.updateBasicInfo(999L, 1L, "제목", "a".repeat(50), List.of(1L)))
                    .willThrow(new BusinessException(CatalystErrorCode.NOT_FOUND));

            mockMvc.perform(put("/api/catalysts/{id}", 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/catalysts/{id}/status")
    class ChangeStatus {

        @Test
        @DisplayName("허용된 전이 요청이면 200과 변경된 상태를 반환한다")
        void changeStatus_allowedTransition_returns200() throws Exception {
            ChangeCatalystStatusRequest request = new ChangeCatalystStatusRequest("PAUSED");
            CatalystStatusResponse response = new CatalystStatusResponse("PAUSED", LocalDateTime.now());
            given(catalystService.changeStatus(1L, 1L, "PAUSED")).willReturn(response);

            mockMvc.perform(patch("/api/catalysts/{id}/status", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("PAUSED"))
                    .andDo(document("catalyst/monitoring-status",
                            responseFields(
                                    fieldWithPath("success").description("성공 여부"),
                                    fieldWithPath("data.status").description("변경된 상태"),
                                    fieldWithPath("data.updatedAt").description("상태 변경 반영일시"),
                                    fieldWithPath("error").description("에러 정보 (성공 시 null)")
                                            .optional().type(JsonFieldType.NULL)
                            )
                    ));
        }

        @Test
        @DisplayName("허용되지 않은 전이 요청이면 400을 반환한다")
        void changeStatus_disallowedTransition_returns400() throws Exception {
            ChangeCatalystStatusRequest request = new ChangeCatalystStatusRequest("ACTIVE");
            given(catalystService.changeStatus(1L, 1L, "ACTIVE"))
                    .willThrow(new BusinessException(GlobalErrorCode.INVALID_INPUT));

            mockMvc.perform(patch("/api/catalysts/{id}/status", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("targetStatus가 비어있으면 400을 반환한다")
        void changeStatus_blankTargetStatus_returns400() throws Exception {
            ChangeCatalystStatusRequest request = new ChangeCatalystStatusRequest("");

            mockMvc.perform(patch("/api/catalysts/{id}/status", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("존재하지 않는 카탈리스트의 상태를 변경하면 404를 반환한다")
        void changeStatus_notFound_returns404() throws Exception {
            ChangeCatalystStatusRequest request = new ChangeCatalystStatusRequest("ACTIVE");
            given(catalystService.changeStatus(999L, 1L, "ACTIVE"))
                    .willThrow(new BusinessException(CatalystErrorCode.NOT_FOUND));

            mockMvc.perform(patch("/api/catalysts/{id}/status", 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }
}
