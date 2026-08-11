package com.catail.backend.catalyst.inbound;

import jakarta.validation.constraints.NotBlank;

public record CatalystStatusChangeRequest(
        @NotBlank String status
) {
}
