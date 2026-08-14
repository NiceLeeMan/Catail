package com.catail.backend.signal.application;

import com.catail.backend.global.BusinessException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

public record SignalCursor(
        LocalDateTime createdAt,
        Long id
) {
    private static final String SEPARATOR = "|";

    public static String encode(LocalDateTime createdAt, Long id) {
        String raw = createdAt + SEPARATOR + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static SignalCursor decode(String cursor) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            int separatorIndex = raw.indexOf(SEPARATOR);
            if (separatorIndex < 0) {
                throw new IllegalArgumentException("구분자가 없습니다.");
            }
            LocalDateTime createdAt = LocalDateTime.parse(raw.substring(0, separatorIndex));
            Long id = Long.parseLong(raw.substring(separatorIndex + 1));
            return new SignalCursor(createdAt, id);
        } catch (RuntimeException e) {
            throw new BusinessException(SignalErrorCode.INVALID_CURSOR);
        }
    }
}
