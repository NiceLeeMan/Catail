package com.catail.backend.company.outbound;

public class ListedCompanyCollectionException extends RuntimeException {

    public ListedCompanyCollectionException(String message) {
        super(message);
    }

    public ListedCompanyCollectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
