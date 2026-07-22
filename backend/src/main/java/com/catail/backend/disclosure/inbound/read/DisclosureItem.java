package com.catail.backend.disclosure.inbound.read;

import com.catail.backend.disclosure.db.Disclosure;

import java.time.LocalDate;
import java.util.List;

public record DisclosureItem(
        Long id,
        String provider,
        String reportName,
        String submitterName,
        LocalDate receivedDate,
        List<String> remarkCodes,
        String sourceUrl
) {
    public static DisclosureItem from(Disclosure disclosure, List<String> remarkCodes) {
        return new DisclosureItem(
                disclosure.getId(),
                disclosure.getProvider().name(),
                disclosure.getReportName(),
                disclosure.getSubmitterName(),
                disclosure.getReceivedDate(),
                remarkCodes,
                disclosure.getProvider().buildSourceUrl(disclosure.getExternalDisclosureId())
        );
    }
}
