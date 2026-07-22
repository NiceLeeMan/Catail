package com.catail.backend.disclosure.application;

import com.catail.backend.global.BusinessException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;

public record DisclosureCursor(
        LocalDate receivedDate,
        String externalDisclosureId
) {
    private static final String SEPARATOR = "|";

    public static String encode(LocalDate receivedDate, String externalDisclosureId) {
        String raw = receivedDate + SEPARATOR + externalDisclosureId;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static DisclosureCursor decode(String cursor) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            int separatorIndex = raw.indexOf(SEPARATOR);
            if (separatorIndex < 0) {
                throw new IllegalArgumentException("구분자가 없습니다.");
            }
            LocalDate receivedDate = LocalDate.parse(raw.substring(0, separatorIndex));
            String externalDisclosureId = raw.substring(separatorIndex + 1);
            return new DisclosureCursor(receivedDate, externalDisclosureId);
        } catch (RuntimeException e) {
            throw new BusinessException(DisclosureErrorCode.INVALID_CURSOR);
        }
    }
}
