package com.catail.backend.catalyst.inbound;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CatalystUpdateRequest(
        @NotBlank String category,
        @NotBlank @Size(min = 10, max = 300) String detail
) {
}
