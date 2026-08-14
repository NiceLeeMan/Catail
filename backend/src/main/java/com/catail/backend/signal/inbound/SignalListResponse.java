package com.catail.backend.signal.inbound;

import java.util.List;

public record SignalListResponse(
        List<SignalListItem> signals,
        String nextCursor,
        boolean hasNext
) {
}
