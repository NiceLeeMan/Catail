package com.catail.backend.company.inbound;

import com.catail.backend.company.application.CompanyListService;
import com.catail.backend.company.inbound.read.CompanyListResponse;
import com.catail.backend.global.ApiResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Validated
public class CompanyController {

    private final CompanyListService companyListService;

    @GetMapping
    public ResponseEntity<ApiResponse<CompanyListResponse>> getList(
            @RequestParam(required = false) String market,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page) {

        return ResponseEntity.ok(ApiResponse.ok(companyListService.getList(market, keyword, page)));
    }
}
