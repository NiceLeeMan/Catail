package com.catail.backend.crawlresultprove.application;

import com.catail.backend.global.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum CrawlResultProveErrorCode implements ErrorCode {

    SEARCH_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "CRAWL_RESULT_PROVE_001", "검색 결과를 찾을 수 없습니다."),
    CONTENT_NOT_CRAWLED(HttpStatus.BAD_REQUEST, "CRAWL_RESULT_PROVE_002", "아직 크롤링이 완료되지 않은 검색 결과입니다.");

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
