package com.catail.backend.global;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("BusinessException: code={}, message={}", errorCode.getCode(), errorCode.getMessage());
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse(GlobalErrorCode.INVALID_INPUT.getMessage());
        log.warn("Validation failed: {}", message);
        return ResponseEntity
                .status(GlobalErrorCode.INVALID_INPUT.getStatus())
                .body(new ApiResponse<>(false, null, new ApiResponse.ErrorDetail(GlobalErrorCode.INVALID_INPUT.getCode(), message)));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(cv -> cv.getMessage())
                .findFirst()
                .orElse(GlobalErrorCode.INVALID_INPUT.getMessage());
        log.warn("ConstraintViolation: {}", message);
        return ResponseEntity
                .status(GlobalErrorCode.INVALID_INPUT.getStatus())
                .body(new ApiResponse<>(false, null, new ApiResponse.ErrorDetail(GlobalErrorCode.INVALID_INPUT.getCode(), message)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity
                .status(GlobalErrorCode.INTERNAL_ERROR.getStatus())
                .body(ApiResponse.fail(GlobalErrorCode.INTERNAL_ERROR));
    }
}
