package com.catail.backend.signal.inbound;

import com.catail.backend.global.ApiResponse;
import com.catail.backend.global.web.CurrentUserId;
import com.catail.backend.signal.application.SignalStatusChangeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/signals")
@RequiredArgsConstructor
@Validated
public class SignalStatusChangeController {

    private final SignalStatusChangeService signalStatusChangeService;

    @PatchMapping("/{signalId}/status")
    public ResponseEntity<ApiResponse<SignalStatusResponse>> changeStatus(
            @CurrentUserId Long userId,
            @PathVariable Long signalId,
            @Valid @RequestBody SignalStatusChangeRequest request) {

        SignalStatusResponse response = signalStatusChangeService.changeStatus(userId, signalId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
