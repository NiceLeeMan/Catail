package com.catail.backend.signal.application;

import com.catail.backend.catalyst.application.CatalystErrorCode;
import com.catail.backend.catalyst.db.Catalyst;
import com.catail.backend.catalyst.domain.CatalystCategory;
import com.catail.backend.catalyst.domain.CatalystStatus;
import com.catail.backend.global.BusinessException;
import com.catail.backend.signal.db.Signal;
import com.catail.backend.signal.db.SignalRepository;
import com.catail.backend.signal.domain.SignalStatus;
import com.catail.backend.signal.inbound.SignalTimelineResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SignalTimelineServiceTest {

    private static final Long OWNER_ID = 1L;
    private static final Long CATALYST_ID = 10L;

    @Mock private CatalystOwnershipValidator catalystOwnershipValidator;
    @Mock private SignalRepository signalRepository;

    private SignalTimelineService service() {
        return new SignalTimelineService(catalystOwnershipValidator, signalRepository);
    }

    private Catalyst catalyst() {
        return Catalyst.create(OWNER_ID, 100L, CatalystCategory.SUPPLY_CHAIN,
                "테스트용 상세내용 10자 이상 작성", "공급망", CatalystStatus.ACTIVE);
    }

    @Test
    @DisplayName("채택된 시그널을 pubDate 내림차순으로 반환한다")
    void getTimeline_adoptedSignals_returnsThem() {
        given(catalystOwnershipValidator.validate(OWNER_ID, CATALYST_ID)).willReturn(catalyst());
        Signal signal = Signal.create(CATALYST_ID, "제목", "요약", "https://origin.example.com/1",
                "https://n.news.naver.com/1", OffsetDateTime.parse("2026-08-10T09:00:00+09:00"), "언론사");
        given(signalRepository.findTimeline(CATALYST_ID, SignalStatus.ADOPTED)).willReturn(List.of(signal));

        SignalTimelineResponse response = service().getTimeline(OWNER_ID, CATALYST_ID);

        assertThat(response.signals()).hasSize(1);
        assertThat(response.signals().get(0).title()).isEqualTo("제목");
    }

    @Test
    @DisplayName("채택된 시그널이 없으면 빈 목록을 반환한다")
    void getTimeline_noAdoptedSignals_returnsEmptyList() {
        given(catalystOwnershipValidator.validate(OWNER_ID, CATALYST_ID)).willReturn(catalyst());
        given(signalRepository.findTimeline(CATALYST_ID, SignalStatus.ADOPTED)).willReturn(List.of());

        SignalTimelineResponse response = service().getTimeline(OWNER_ID, CATALYST_ID);

        assertThat(response.signals()).isEmpty();
    }

    @Test
    @DisplayName("소유권 검증에 실패하면 예외가 그대로 전파된다")
    void getTimeline_ownershipValidationFails_propagatesException() {
        given(catalystOwnershipValidator.validate(OWNER_ID, CATALYST_ID))
                .willThrow(new BusinessException(CatalystErrorCode.CATALYST_ACCESS_DENIED));

        assertThatThrownBy(() -> service().getTimeline(OWNER_ID, CATALYST_ID))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(CatalystErrorCode.CATALYST_ACCESS_DENIED);
    }
}
