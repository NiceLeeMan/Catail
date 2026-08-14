package com.catail.backend.signal.application;

import com.catail.backend.global.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum SignalErrorCode implements ErrorCode {

    LLM_CALL_FAILED(HttpStatus.BAD_GATEWAY, "SIGNAL_001", "검색어 생성을 위한 LLM 호출에 실패했습니다."),
    LLM_INVALID_RESPONSE(HttpStatus.BAD_GATEWAY, "SIGNAL_002", "LLM 응답이 검색어 포맷과 일치하지 않습니다."),
    INVALID_CURSOR(HttpStatus.BAD_REQUEST, "SIGNAL_003", "유효하지 않은 커서입니다.");

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
