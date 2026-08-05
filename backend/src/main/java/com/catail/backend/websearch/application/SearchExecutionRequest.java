package com.catail.backend.websearch.application;

public record SearchExecutionRequest(
        Long userId,
        Long companyId,
        String analysisScope
) {
}
