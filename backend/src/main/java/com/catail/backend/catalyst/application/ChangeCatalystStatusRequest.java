package com.catail.backend.catalyst.application;

import jakarta.validation.constraints.NotBlank;

public record ChangeCatalystStatusRequest(
        @NotBlank(message = "목표 상태값은 필수입니다.")
        String targetStatus
) {
}
