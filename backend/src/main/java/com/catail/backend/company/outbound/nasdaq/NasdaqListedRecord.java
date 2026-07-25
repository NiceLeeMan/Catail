package com.catail.backend.company.outbound.nasdaq;

record NasdaqListedRecord(
        String symbol,
        String securityName,
        String marketCategory,
        String testIssue,
        String etf,
        String nextShares
) {
}
