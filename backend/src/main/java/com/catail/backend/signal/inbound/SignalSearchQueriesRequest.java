package com.catail.backend.signal.inbound;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SignalSearchQueriesRequest(
        @NotNull Long companyId,
        @NotBlank String category,
        @NotBlank String detail
) {
}
