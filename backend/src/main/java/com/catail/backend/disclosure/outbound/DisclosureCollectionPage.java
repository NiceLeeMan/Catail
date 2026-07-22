package com.catail.backend.disclosure.outbound;

import java.util.List;

public record DisclosureCollectionPage(
        int totalCount,
        List<RawDisclosureItem> items
) {
}
