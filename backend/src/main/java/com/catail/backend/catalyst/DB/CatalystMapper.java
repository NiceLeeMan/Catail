package com.catail.backend.catalyst.DB;

import com.catail.backend.catalyst.domain.CatalystDomain;

import java.util.List;

public class CatalystMapper {

    private CatalystMapper() {
    }

    public static CatalystDomain toDomain(Catalyst entity, List<Long> industryIds) {
        return CatalystDomain.reconstruct(
                entity.getId(),
                entity.getUserId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getStatus(),
                industryIds,
                entity.getSearchConditions(),
                entity.getSearchIntervalHours(),
                entity.getLastSearchedAt(),
                entity.getActivatedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public static Catalyst toNewEntity(CatalystDomain domain) {
        Catalyst entity = new Catalyst();
        entity.setUserId(domain.getUserId());
        entity.setTitle(domain.getTitle());
        entity.setContent(domain.getContent());
        entity.setStatus(domain.getStatus());
        entity.setSearchConditions(domain.getSearchConditions());
        entity.setSearchIntervalHours(domain.getSearchIntervalHours());
        return entity;
    }
}
