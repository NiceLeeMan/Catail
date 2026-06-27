package com.catail.backend.global;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {

    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "GLOBAL_001", "인증이 필요합니다"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "GLOBAL_002", "토큰이 만료되었습니다"),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "GLOBAL_003", "유효하지 않은 입력값입니다"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GLOBAL_004", "서버 내부 오류가 발생했습니다");

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