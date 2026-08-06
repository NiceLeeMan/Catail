package com.catail.backend.websearch.application;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public final class UrlValidator {

    private UrlValidator() {
    }

    public static boolean isValid(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return false;
        }
        try {
            URI uri = new URI(rawUrl);
            if (!uri.isAbsolute()) {
                return false;
            }
            String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
            if (!scheme.equals("http") && !scheme.equals("https")) {
                return false;
            }
            return uri.getHost() != null && !uri.getHost().isBlank();
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
