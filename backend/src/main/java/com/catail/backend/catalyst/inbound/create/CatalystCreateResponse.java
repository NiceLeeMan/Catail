package com.catail.backend.catalyst.inbound.create;

import com.catail.backend.catalyst.domain.CatalystDomain;

import java.time.LocalDateTime;
import java.util.List;

public record CatalystCreateResponse(
        Long id,
        String title,
        String content,
        String status,
        List<String> industries,
        LocalDateTime createdAt
) {

    public static CatalystCreateResponse from(CatalystDomain catalyst, List<String> industries) {
        return new CatalystCreateResponse(
                catalyst.getId(),
                catalyst.getTitle(),
                catalyst.getContent(),
                catalyst.getStatus().name(),
                industries,
                catalyst.getCreatedAt()
        );
    }
}
