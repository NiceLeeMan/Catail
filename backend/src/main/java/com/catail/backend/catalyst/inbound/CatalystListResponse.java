package com.catail.backend.catalyst.inbound;

import com.catail.backend.catalyst.db.Catalyst;

import java.util.List;

public record CatalystListResponse(
        List<CatalystListItem> catalysts
) {
    public static CatalystListResponse from(List<Catalyst> catalysts) {
        return new CatalystListResponse(catalysts.stream().map(CatalystListItem::from).toList());
    }
}
