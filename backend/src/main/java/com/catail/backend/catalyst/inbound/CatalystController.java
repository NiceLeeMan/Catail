package com.catail.backend.catalyst.inbound;

import com.catail.backend.catalyst.application.CatalystCreateService;
import com.catail.backend.catalyst.application.CatalystDeleteService;
import com.catail.backend.catalyst.application.CatalystUpdateService;
import com.catail.backend.global.ApiResponse;
import com.catail.backend.global.web.CurrentUserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalysts")
@RequiredArgsConstructor
@Validated
public class CatalystController {

    private final CatalystCreateService catalystCreateService;
    private final CatalystDeleteService catalystDeleteService;
    private final CatalystUpdateService catalystUpdateService;

    @PostMapping
    public ResponseEntity<ApiResponse<CatalystCreateResponse>> create(
            @CurrentUserId Long userId,
            @Valid @RequestBody CatalystCreateRequest request) {

        CatalystCreateResponse response = catalystCreateService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @DeleteMapping("/{catalystId}")
    public ResponseEntity<Void> delete(
            @CurrentUserId Long userId,
            @PathVariable Long catalystId) {

        catalystDeleteService.delete(userId, catalystId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{catalystId}")
    public ResponseEntity<ApiResponse<CatalystUpdateResponse>> update(
            @CurrentUserId Long userId,
            @PathVariable Long catalystId,
            @Valid @RequestBody CatalystUpdateRequest request) {

        CatalystUpdateResponse response = catalystUpdateService.update(userId, catalystId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
