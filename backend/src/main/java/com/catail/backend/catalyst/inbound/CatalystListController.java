package com.catail.backend.catalyst.inbound;

import com.catail.backend.catalyst.application.CatalystListService;
import com.catail.backend.global.ApiResponse;
import com.catail.backend.global.web.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies/{companyId}/catalysts")
@RequiredArgsConstructor
public class CatalystListController {

    private final CatalystListService catalystListService;

    @GetMapping
    public ResponseEntity<ApiResponse<CatalystListResponse>> getList(
            @CurrentUserId Long userId,
            @PathVariable Long companyId) {

        return ResponseEntity.ok(ApiResponse.ok(catalystListService.getList(userId, companyId)));
    }
}
