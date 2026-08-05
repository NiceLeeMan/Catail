package com.catail.backend.websearch.inbound;

public record SearchExecutionCreateRequest(
        Long companyId,
        String analysisScope
) {
}
