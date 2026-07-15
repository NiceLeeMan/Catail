package com.catail.backend.catalyst.inbound.read;

import com.catail.backend.catalyst.domain.CatalystDomain;

import java.time.LocalDateTime;
import java.util.List;

public record CatalystListItem(
        Long id,
        String title,
        String status,
        List<String> industryTags,
        int pendingSignalCount,
        LocalDateTime createdAt
) {
    public static CatalystListItem from(
            CatalystDomain catalyst,
            List<String> industryTags,
            int pendingSignalCount
    ) {
        return new CatalystListItem(
                catalyst.getId(),
                catalyst.getTitle(),
                catalyst.getStatus().name(),
                industryTags,
                pendingSignalCount,
                catalyst.getCreatedAt()
        );
    }
}
