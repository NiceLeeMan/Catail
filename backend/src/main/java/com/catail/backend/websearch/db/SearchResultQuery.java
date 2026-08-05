package com.catail.backend.websearch.db;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "search_result_query")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchResultQuery {

    @EmbeddedId
    private SearchResultQueryId id;

    public static SearchResultQuery create(Long searchResultId, Long searchQueryId) {
        SearchResultQuery entity = new SearchResultQuery();
        entity.id = new SearchResultQueryId(searchResultId, searchQueryId);
        return entity;
    }
}
