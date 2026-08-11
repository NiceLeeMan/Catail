package com.catail.backend.catalyst.application;

import com.catail.backend.global.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum CatalystErrorCode implements ErrorCode {

    CATALYST_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "CATALYST_001", "동일 기업에 등록 가능한 카탈리스트는 최대 3개입니다."),
    CATALYST_NOT_FOUND(HttpStatus.NOT_FOUND, "CATALYST_002", "카탈리스트를 찾을 수 없습니다."),
    CATALYST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CATALYST_003", "카탈리스트에 대한 권한이 없습니다.");

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
