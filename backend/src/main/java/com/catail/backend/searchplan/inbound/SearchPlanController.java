package com.catail.backend.searchplan.inbound;

import com.catail.backend.global.ApiResponse;
import com.catail.backend.searchplan.application.SearchPlanRequest;
import com.catail.backend.searchplan.application.SearchPlanService;
import com.catail.backend.searchplan.domain.SearchPlanFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관련 참여주체 탐색 계획 포맷을 실제 LLM 호출로 생성해 눈으로 확인하기 위한 내부 전용 엔드포인트.
 * FE에 노출되는 정식 API가 아니라, 개발 중 실제 OpenAI 연동 결과를 확인하는 용도다.
 */
@RestController
@RequestMapping("/api/internal/search-plans")
@RequiredArgsConstructor
public class SearchPlanController {

    private final SearchPlanService searchPlanService;

    @PostMapping
    public ResponseEntity<ApiResponse<SearchPlanFormat>> generate(@RequestBody SearchPlanRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(searchPlanService.generate(request)));
    }
}
