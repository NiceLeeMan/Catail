package com.catail.backend.catalyst.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CatalystCategory {

    FINANCIAL_PERFORMANCE("실적/재무"),
    REGULATION_POLICY("규제/정책"),
    SUPPLY_CHAIN("공급망"),
    COMPETITIVE_LANDSCAPE("경쟁구도"),
    GOVERNANCE("경영권/지배구조"),
    NEW_BUSINESS("신사업/전략");

    private final String label;
}
