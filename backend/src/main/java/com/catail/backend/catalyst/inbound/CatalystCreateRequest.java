package com.catail.backend.catalyst.inbound;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CatalystCreateRequest(
        @NotNull Long companyId,
        @NotBlank String category,
        @NotBlank @Size(min = 10, max = 300) String detail,
        @NotBlank String status
) {
}
