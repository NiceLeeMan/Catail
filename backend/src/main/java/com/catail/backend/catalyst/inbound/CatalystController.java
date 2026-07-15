package com.catail.backend.catalyst.inbound;

import com.catail.backend.catalyst.inbound.create.CatalystCreateResponse;
import com.catail.backend.catalyst.inbound.delete.CatalystDeleteResponse;
import com.catail.backend.catalyst.inbound.read.CatalystInfoResponse;
import com.catail.backend.catalyst.inbound.read.CatalystListResponse;
import com.catail.backend.catalyst.application.CatalystListService;
import com.catail.backend.catalyst.application.CatalystService;
import com.catail.backend.catalyst.inbound.update.CatalystStatusResponse;
import com.catail.backend.catalyst.inbound.update.CatalystUpdateResponse;
import com.catail.backend.catalyst.inbound.update.ChangeCatalystStatusRequest;
import com.catail.backend.catalyst.inbound.create.CreateCatalystRequest;
import com.catail.backend.catalyst.inbound.update.UpdateCatalystBasicInfoRequest;
import com.catail.backend.global.ApiResponse;
import com.catail.backend.global.web.CurrentUserId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalysts")
@RequiredArgsConstructor
@Validated
public class CatalystController {

    private final CatalystService catalystService;
    private final CatalystListService catalystListService;

    @PostMapping
    public ResponseEntity<ApiResponse<CatalystCreateResponse>> create(
            @CurrentUserId Long userId,
            @Valid @RequestBody CreateCatalystRequest request) {

        CatalystCreateResponse response = catalystService.create(
                userId, request.title(), request.content(), request.industryIds(), request.status());

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CatalystListResponse>> getList(
            @CurrentUserId Long userId,
            @RequestParam(defaultValue = "0") @Min(0) int page) {

        return ResponseEntity.ok(ApiResponse.ok(catalystListService.getList(userId, page)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CatalystInfoResponse>> getDetail(
            @CurrentUserId Long userId,
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.ok(catalystService.getDetail(id, userId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<CatalystDeleteResponse>> delete(
            @CurrentUserId Long userId,
            @PathVariable Long id) {

        CatalystDeleteResponse response = catalystService.delete(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CatalystUpdateResponse>> updateBasicInfo(
            @CurrentUserId Long userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCatalystBasicInfoRequest request) {

        CatalystUpdateResponse response = catalystService.updateBasicInfo(
                id, userId, request.title(), request.content(), request.industryIds());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CatalystStatusResponse>> changeStatus(
            @CurrentUserId Long userId,
            @PathVariable Long id,
            @Valid @RequestBody ChangeCatalystStatusRequest request) {

        CatalystStatusResponse response = catalystService.changeStatus(id, userId, request.targetStatus());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
