package com.catail.backend.signal.inbound;

import java.util.List;

public record SignalSearchQueriesResponse(List<String> queries) {

    public static SignalSearchQueriesResponse from(List<String> queries) {
        return new SignalSearchQueriesResponse(queries);
    }
}
