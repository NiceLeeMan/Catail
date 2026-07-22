package com.catail.backend.disclosure.inbound;

import com.catail.backend.disclosure.application.DisclosureListService;
import com.catail.backend.disclosure.inbound.read.DisclosureListResponse;
import com.catail.backend.global.ApiResponse;
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
@RequestMapping("/api/companies/{companyId}/disclosures")
@RequiredArgsConstructor
@Validated
public class DisclosureController {

    private final DisclosureListService disclosureListService;

    @GetMapping
    public ResponseEntity<ApiResponse<DisclosureListResponse>> getList(
            @PathVariable Long companyId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        return ResponseEntity.ok(ApiResponse.ok(disclosureListService.getList(companyId, cursor, size)));
    }
}
