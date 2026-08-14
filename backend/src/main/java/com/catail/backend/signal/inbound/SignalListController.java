package com.catail.backend.signal.inbound;

import com.catail.backend.global.ApiResponse;
import com.catail.backend.global.web.CurrentUserId;
import com.catail.backend.signal.application.SignalListService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalysts/{catalystId}/signals")
@RequiredArgsConstructor
@Validated
public class SignalListController {

    private final SignalListService signalListService;

    @GetMapping
    public ResponseEntity<ApiResponse<SignalListResponse>> getList(
            @CurrentUserId Long userId,
            @PathVariable Long catalystId,
            @RequestParam String status,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "7") @Min(1) @Max(100) int size) {

        return ResponseEntity.ok(ApiResponse.ok(
                signalListService.getList(userId, catalystId, status, cursor, size)));
    }
}
