package com.catail.backend.signal.inbound;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SignalNewsArticlesRequest(
        @NotEmpty List<@NotBlank String> queries
) {
}
