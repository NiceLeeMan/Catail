package com.catail.backend.signal.application;

import com.catail.backend.catalyst.application.CatalystActivatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CatalystActivatedEventListenerTest {

    @Mock
    private SignalCollectionService signalCollectionService;

    private CatalystActivatedEventListener listener() {
        return new CatalystActivatedEventListener(signalCollectionService);
    }

    @Test
    @DisplayName("이벤트를 받으면 catalystId로 시그널 수집을 호출한다")
    void onCatalystActivated_callsCollectFor() {
        listener().onCatalystActivated(new CatalystActivatedEvent(1L));

        verify(signalCollectionService).collectFor(1L);
    }

    @Test
    @DisplayName("수집 중 예외가 발생해도 밖으로 전파하지 않는다")
    void onCatalystActivated_collectFails_doesNotPropagate() {
        given(signalCollectionService.collectFor(1L)).willThrow(new RuntimeException("실패"));

        assertThatCode(() -> listener().onCatalystActivated(new CatalystActivatedEvent(1L)))
                .doesNotThrowAnyException();
    }
}
