package com.catail.backend.disclosure.application;

import com.catail.backend.global.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum DisclosureErrorCode implements ErrorCode {

    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "DISCLOSURE_001", "기업을 찾을 수 없습니다."),
    INVALID_CURSOR(HttpStatus.BAD_REQUEST, "DISCLOSURE_002", "유효하지 않은 커서입니다."),
    SYNC_SOURCE_ERROR(HttpStatus.BAD_GATEWAY, "DISCLOSURE_003", "외부 공시 정보 수집에 실패했습니다.");

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
