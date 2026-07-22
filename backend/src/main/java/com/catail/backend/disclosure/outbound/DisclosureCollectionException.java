package com.catail.backend.disclosure.outbound;

public class DisclosureCollectionException extends RuntimeException {

    public DisclosureCollectionException(String message) {
        super(message);
    }

    public DisclosureCollectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
