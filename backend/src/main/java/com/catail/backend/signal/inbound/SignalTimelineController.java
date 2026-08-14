package com.catail.backend.signal.inbound;

import com.catail.backend.global.ApiResponse;
import com.catail.backend.global.web.CurrentUserId;
import com.catail.backend.signal.application.SignalTimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalysts/{catalystId}/signals/timeline")
@RequiredArgsConstructor
public class SignalTimelineController {

    private final SignalTimelineService signalTimelineService;

    @GetMapping
    public ResponseEntity<ApiResponse<SignalTimelineResponse>> getTimeline(
            @CurrentUserId Long userId,
            @PathVariable Long catalystId) {

        return ResponseEntity.ok(ApiResponse.ok(signalTimelineService.getTimeline(userId, catalystId)));
    }
}
