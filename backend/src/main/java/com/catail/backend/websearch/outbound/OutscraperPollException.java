package com.catail.backend.websearch.outbound;

import lombok.Getter;

@Getter
public class OutscraperPollException extends RuntimeException {

    private final boolean retryable;
    private final Integer retryAfterSeconds;

    public OutscraperPollException(String message, boolean retryable, Integer retryAfterSeconds, Throwable cause) {
        super(message, cause);
        this.retryable = retryable;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public OutscraperPollException(String message, boolean retryable) {
        this(message, retryable, null, null);
    }
}
