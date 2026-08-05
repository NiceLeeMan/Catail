package com.catail.backend.websearch.inbound;

import com.catail.backend.global.ApiResponse;
import com.catail.backend.global.web.CurrentUserId;
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
 * 인증(userId) 임시 처리: {@code /api/internal/**}는 SecurityConfig에서 permitAll이라
 * 인증 없이 호출하면 {@link CurrentUserId}가 UNAUTHENTICATED 예외를 던진다.
 * 즉 이 엔드포인트를 호출하려면 유효한 로그인 토큰(JWT)을 함께 보내야 userId가 채워진다.
 * 이는 이 API가 추후 정식 FE 대면 API로 승격될 때와 동일한 인증 메커니즘이므로 별도 변경이 필요 없다.
 */
@RestController
@RequestMapping("/api/internal/search-executions")
@RequiredArgsConstructor
public class SearchExecutionController {

    private final SearchExecutionService searchExecutionService;
    private final SearchExecutionDetailService searchExecutionDetailService;

    @PostMapping
    public ResponseEntity<ApiResponse<SearchExecutionDetailResponse>> create(
            @CurrentUserId Long userId,
            @RequestBody SearchExecutionCreateRequest request
    ) {
        Long executionId = searchExecutionService.create(
                new SearchExecutionRequest(userId, request.companyId(), request.analysisScope()));
        return ResponseEntity.ok(ApiResponse.ok(searchExecutionDetailService.getDetail(executionId)));
    }

    @GetMapping("/{executionId}")
    public ResponseEntity<ApiResponse<SearchExecutionDetailResponse>> getDetail(@PathVariable Long executionId) {
        return ResponseEntity.ok(ApiResponse.ok(searchExecutionDetailService.getDetail(executionId)));
    }
}
