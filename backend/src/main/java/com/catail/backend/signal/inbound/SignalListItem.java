package com.catail.backend.signal.inbound;

import com.catail.backend.signal.db.Signal;

import java.time.OffsetDateTime;

public record SignalListItem(
        Long signalId,
        String title,
        String description,
        String press,
        String link,
        OffsetDateTime pubDate,
        String relevanceReason,
        String status
) {
    public static SignalListItem from(Signal signal) {
        return new SignalListItem(
                signal.getId(),
                signal.getTitle(),
                signal.getDescription(),
                signal.getPress(),
                signal.getLink(),
                signal.getPubDate(),
                signal.getRelevanceReason(),
                signal.getStatus().name());
    }
}
