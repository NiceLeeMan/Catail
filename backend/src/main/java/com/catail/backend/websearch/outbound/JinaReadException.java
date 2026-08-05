package com.catail.backend.websearch.outbound;

import com.catail.backend.websearch.domain.CrawlFailureCode;
import lombok.Getter;

@Getter
public class JinaReadException extends RuntimeException {

    private final CrawlFailureCode failureCode;
    private final boolean retryable;

    public JinaReadException(CrawlFailureCode failureCode, boolean retryable, String message, Throwable cause) {
        super(message, cause);
        this.failureCode = failureCode;
        this.retryable = retryable;
    }

    public JinaReadException(CrawlFailureCode failureCode, boolean retryable, String message) {
        this(failureCode, retryable, message, null);
    }
}
