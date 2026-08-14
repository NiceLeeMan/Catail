package com.catail.backend.signal.inbound;

import jakarta.validation.constraints.NotBlank;

public record SignalStatusChangeRequest(
        @NotBlank String status
) {
}
