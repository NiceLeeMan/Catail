package com.catail.backend.catalyst.application;

import com.catail.backend.global.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@RequiredArgsConstructor
public enum CatalystErrorCode implements ErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND, "CATALYST_001", "존재하지 않는 카탈리스트입니다.");

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
