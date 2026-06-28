package com.catail.backend.user.application;

import com.catail.backend.global.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "USER_001", "지원하지 않는 OAuth 제공자입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_002", "존재하지 않는 회원입니다."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "USER_003", "유효하지 않은 닉네임입니다.");

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