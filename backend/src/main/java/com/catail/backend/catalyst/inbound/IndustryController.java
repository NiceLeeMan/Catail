package com.catail.backend.catalyst.inbound;

import com.catail.backend.catalyst.DB.IndustryRepository;
import com.catail.backend.catalyst.inbound.read.IndustryResponse;
import com.catail.backend.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/industries")
@RequiredArgsConstructor
public class IndustryController {

    private final IndustryRepository industryRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<IndustryResponse>>> getList() {
        List<IndustryResponse> industries = industryRepository.findAll().stream()
                .map(industry -> new IndustryResponse(industry.getId(), industry.getName()))
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(industries));
    }
}
