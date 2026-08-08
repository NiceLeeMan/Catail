package com.catail.backend.crawlresultprove.inbound;

import com.catail.backend.crawlresultprove.application.CrawlResultProveService;
import com.catail.backend.crawlresultprove.inbound.read.CrawlResultProveResponse;
import com.catail.backend.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 크롤링 본문 정제/노이즈 제거 로직의 실효성을 검증하기 위한 개발자 전용 테스트 엔드포인트.
 * 프로덕션 search_result 테이블은 읽기만 하며, 결과는 별도의 crawl_result_prove 테이블에만 버전(V1, V2...)으로 쌓인다.
 * 인증이 필요 없다 (/api/internal/** 는 SecurityConfig에서 permitAll이고 이 컨트롤러는 CurrentUserId를 사용하지 않는다).
 */
@RestController
@RequestMapping("/api/internal/crawl-result-prove")
@RequiredArgsConstructor
public class CrawlResultProveController {

    private final CrawlResultProveService crawlResultProveService;

    @PostMapping("/{searchResultId}")
    public ResponseEntity<ApiResponse<CrawlResultProveResponse>> advance(@PathVariable Long searchResultId) {
        CrawlResultProveResponse response = CrawlResultProveResponse.from(crawlResultProveService.advance(searchResultId));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{searchResultId}")
    public ResponseEntity<ApiResponse<List<CrawlResultProveResponse>>> getHistory(@PathVariable Long searchResultId) {
        List<CrawlResultProveResponse> responses = crawlResultProveService.getHistory(searchResultId).stream()
                .map(CrawlResultProveResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }
}
