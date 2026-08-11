package com.catail.backend.catalyst.inbound;

import com.catail.backend.catalyst.db.Catalyst;

import java.time.LocalDateTime;

public record CatalystListItem(
        Long catalystId,
        String title,
        String category,
        String detail,
        String status,
        LocalDateTime createdAt
) {
    public static CatalystListItem from(Catalyst catalyst) {
        return new CatalystListItem(
                catalyst.getId(),
                catalyst.getTitle(),
                catalyst.getCategory().name(),
                catalyst.getDetail(),
                catalyst.getStatus().name(),
                catalyst.getCreatedAt()
        );
    }
}
