package com.catail.backend.signal.inbound;

import com.catail.backend.global.ApiResponse;
import com.catail.backend.signal.application.SignalNewsCollectionService;
import com.catail.backend.signal.application.SignalSearchQueryGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 시그널 수집 파이프라인의 검색어 생성/네이버 뉴스 수집 단계를 수동으로 검증하기 위한 개발자 전용
 * 엔드포인트. 정식 시그널 API가 아니며 인증이 필요 없다(/api/internal/** 는 SecurityConfig에서 permitAll).
 * DB 저장은 하지 않고 결과만 반환한다 — 저장/오케스트레이션은 별도 이슈(#94)에서 다룬다.
 */
@RestController
@RequestMapping("/api/internal/signals")
@RequiredArgsConstructor
@Validated
public class SignalCollectionPreviewController {

    private final SignalSearchQueryGenerationService signalSearchQueryGenerationService;
    private final SignalNewsCollectionService signalNewsCollectionService;

    @PostMapping("/search-queries")
    public ResponseEntity<ApiResponse<SignalSearchQueriesResponse>> generateSearchQueries(
            @Valid @RequestBody SignalSearchQueriesRequest request) {

        var queries = signalSearchQueryGenerationService.generate(
                request.companyId(), request.category(), request.detail());
        return ResponseEntity.ok(ApiResponse.ok(SignalSearchQueriesResponse.from(queries)));
    }

    @PostMapping("/news-articles")
    public ResponseEntity<ApiResponse<SignalNewsArticlesResponse>> fetchNewsArticles(
            @Valid @RequestBody SignalNewsArticlesRequest request) {

        var resultsByQuery = signalNewsCollectionService.collect(request.queries());
        return ResponseEntity.ok(ApiResponse.ok(SignalNewsArticlesResponse.from(resultsByQuery)));
    }
}
