package com.catail.backend.disclosure.outbound;

import java.time.LocalDate;
import java.util.List;

public record RawDisclosureItem(
        String externalDisclosureId,
        LocalDate receivedDate,
        String reportName,
        String submitterName,
        List<String> remarkCodes
) {
}
