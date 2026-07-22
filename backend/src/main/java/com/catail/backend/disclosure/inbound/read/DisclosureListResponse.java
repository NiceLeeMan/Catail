package com.catail.backend.disclosure.inbound.read;

import java.time.LocalDateTime;
import java.util.List;

public record DisclosureListResponse(
        List<DisclosureItem> items,
        String nextCursor,
        boolean hasNext,
        LocalDateTime lastSyncedAt,
        boolean initialSyncCompleted
) {
}
