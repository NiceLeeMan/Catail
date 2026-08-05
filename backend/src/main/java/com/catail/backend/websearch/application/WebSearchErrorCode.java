package com.catail.backend.websearch.application;

import com.catail.backend.global.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum WebSearchErrorCode implements ErrorCode {

    TOO_MANY_QUERIES_PER_CRITERION(HttpStatus.BAD_REQUEST, "WEB_SEARCH_001", "탐색 기준당 검색어는 최대 5개까지 허용됩니다."),
    DUPLICATE_QUERY_ACROSS_CRITERIA(HttpStatus.BAD_REQUEST, "WEB_SEARCH_002", "탐색 기준 간 동일한 검색어가 존재합니다."),
    SEARCH_EXECUTION_NOT_FOUND(HttpStatus.NOT_FOUND, "WEB_SEARCH_003", "검색 실행 정보를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override
    public HttpStatus getStatus() { return status; }

    @Override
    public String getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}
