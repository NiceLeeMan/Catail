package com.catail.backend.signal.outbound.naver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverNewsSearchResponse(List<NaverNewsItem> items) {
}
