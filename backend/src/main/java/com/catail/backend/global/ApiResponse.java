package com.catail.backend.global;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ErrorDetail error;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null);
    }

    public static ApiResponse<Void> fail(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, ErrorDetail.from(errorCode));
    }

    public record ErrorDetail(String code, String message) {
        public static ErrorDetail from(ErrorCode errorCode) {
            return new ErrorDetail(errorCode.getCode(), errorCode.getMessage());
        }
    }
}