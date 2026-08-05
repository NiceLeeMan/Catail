package com.catail.backend.websearch.domain;

public enum CrawlFailureCode {
    TIMEOUT,
    NETWORK_ERROR,
    RATE_LIMITED,
    CLIENT_ERROR,
    JINA_SERVER_ERROR,
    TARGET_PAGE_ERROR,
    EMPTY_CONTENT,
    INVALID_RESPONSE,
    UNKNOWN_ERROR
}
