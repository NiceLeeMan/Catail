package com.catail.backend.signal.application;

import com.catail.backend.global.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SignalCursorTest {

    @Test
    @DisplayName("encode한 커서를 decode하면 원래 값으로 복원된다")
    void encodeAndDecode_roundTrip_returnsOriginalValues() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 14, 10, 30, 0);

        String encoded = SignalCursor.encode(createdAt, 1234L);
        SignalCursor decoded = SignalCursor.decode(encoded);

        assertThat(decoded.createdAt()).isEqualTo(createdAt);
        assertThat(decoded.id()).isEqualTo(1234L);
    }

    @Test
    @DisplayName("Base64로 디코딩할 수 없는 문자열이면 INVALID_CURSOR 예외가 발생한다")
    void decode_notBase64_throwsInvalidCursor() {
        assertThatThrownBy(() -> SignalCursor.decode("!!not-base64!!"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(SignalErrorCode.INVALID_CURSOR);
    }

    @Test
    @DisplayName("구분자가 없는 값이면 INVALID_CURSOR 예외가 발생한다")
    void decode_noSeparator_throwsInvalidCursor() {
        String malformed = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("noSeparatorHere".getBytes());

        assertThatThrownBy(() -> SignalCursor.decode(malformed))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(SignalErrorCode.INVALID_CURSOR);
    }

    @Test
    @DisplayName("id 부분이 숫자가 아니면 INVALID_CURSOR 예외가 발생한다")
    void decode_nonNumericId_throwsInvalidCursor() {
        String malformed = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("2026-08-14T10:30:00|abc".getBytes());

        assertThatThrownBy(() -> SignalCursor.decode(malformed))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(SignalErrorCode.INVALID_CURSOR);
    }
}
