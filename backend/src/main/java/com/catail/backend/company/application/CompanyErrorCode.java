package com.catail.backend.company.application;

import com.catail.backend.global.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum CompanyErrorCode implements ErrorCode {

    UNSUPPORTED_MARKET(HttpStatus.BAD_REQUEST, "COMPANY_001", "지원하지 않는 시장입니다."),
    COLLECTION_SOURCE_ERROR(HttpStatus.BAD_GATEWAY, "COMPANY_002", "외부 기업 정보 수집에 실패했습니다."),
    COLLECTION_DATA_NOT_FOUND(HttpStatus.BAD_GATEWAY, "COMPANY_003", "최근 영업일 기준 상장기업 데이터를 찾을 수 없습니다."),
    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "COMPANY_004", "기업을 찾을 수 없습니다.");

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
