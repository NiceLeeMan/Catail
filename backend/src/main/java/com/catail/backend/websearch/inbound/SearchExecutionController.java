package com.catail.backend.websearch.inbound;

import com.catail.backend.global.ApiResponse;
import com.catail.backend.websearch.application.SearchExecutionDetailService;
import com.catail.backend.websearch.application.SearchExecutionRequest;
import com.catail.backend.websearch.application.SearchExecutionService;
import com.catail.backend.websearch.inbound.read.SearchExecutionDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 탐색 계획 검색어를 Outscraper로 실행하고 결과를 저장하는 파이프라인을 눈으로 확인하기 위한
 * 내부 전용 엔드포인트. FE에 노출되는 정식 API가 아니다.
 *
 * 인증(userId) 임시 해제: 개발 단계에서 토큰 없이 호출할 수 있도록 {@code @CurrentUserId}를 빼고
 * 고정 userId(DEV_USER_ID)를 사용한다. 인증을 다시 걸 때는 파라미터를
 * {@code @CurrentUserId Long userId}로 되돌리면 된다.
 */
@RestController
@RequestMapping("/api/internal/search-executions")
@RequiredArgsConstructor
public class SearchExecutionController {

    // TODO: 인증 재적용 시 제거하고 @CurrentUserId Long userId로 되돌릴 것.
    private static final Long DEV_USER_ID = 1L;

    private final SearchExecutionService searchExecutionService;
    private final SearchExecutionDetailService searchExecutionDetailService;

    @PostMapping
    public ResponseEntity<ApiResponse<SearchExecutionDetailResponse>> create(
            @RequestBody SearchExecutionCreateRequest request
    ) {
        Long executionId = searchExecutionService.create(
                new SearchExecutionRequest(DEV_USER_ID, request.companyId(), request.analysisScope()));
        return ResponseEntity.ok(ApiResponse.ok(searchExecutionDetailService.getDetail(executionId)));
    }

    @GetMapping("/{executionId}")
    public ResponseEntity<ApiResponse<SearchExecutionDetailResponse>> getDetail(@PathVariable Long executionId) {
        return ResponseEntity.ok(ApiResponse.ok(searchExecutionDetailService.getDetail(executionId)));
    }
}
