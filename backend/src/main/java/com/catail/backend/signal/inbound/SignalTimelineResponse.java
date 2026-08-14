package com.catail.backend.signal.inbound;

import java.util.List;

public record SignalTimelineResponse(List<SignalListItem> signals) {
}
