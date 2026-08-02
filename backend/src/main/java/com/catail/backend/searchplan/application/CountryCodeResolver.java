package com.catail.backend.searchplan.application;

import com.catail.backend.company.domain.Market;

public final class CountryCodeResolver {

    private CountryCodeResolver() {
    }

    public static String resolve(Market market) {
        return switch (market) {
            case KOSPI -> "KR";
            case NASDAQ -> "";
        };
    }
}
