package com.catail.backend.catalyst.inbound.read;

import java.util.List;

public record CatalystListResponse(
        List<CatalystListItem> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
}
