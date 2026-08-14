package com.catail.backend.signal.application;

import com.catail.backend.catalyst.application.CatalystErrorCode;
import com.catail.backend.catalyst.db.Catalyst;
import com.catail.backend.catalyst.domain.CatalystCategory;
import com.catail.backend.catalyst.domain.CatalystStatus;
import com.catail.backend.global.BusinessException;
import com.catail.backend.global.GlobalErrorCode;
import com.catail.backend.signal.db.Signal;
import com.catail.backend.signal.db.SignalRepository;
import com.catail.backend.signal.domain.SignalStatus;
import com.catail.backend.signal.inbound.SignalListResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SignalListServiceTest {

    private static final Long OWNER_ID = 1L;
    private static final Long CATALYST_ID = 10L;

    @Mock private CatalystOwnershipValidator catalystOwnershipValidator;
    @Mock private SignalRepository signalRepository;

    private SignalListService service() {
        return new SignalListService(catalystOwnershipValidator, signalRepository);
    }

    private Catalyst catalyst() {
        return Catalyst.create(OWNER_ID, 100L, CatalystCategory.SUPPLY_CHAIN,
                "테스트용 상세내용 10자 이상 작성", "공급망", CatalystStatus.ACTIVE);
    }

    private Signal signal(String link) {
        return Signal.create(CATALYST_ID, "제목", "요약", "https://origin.example.com/1",
                link, OffsetDateTime.now(), "언론사");
    }

    @Test
    @DisplayName("소유권 검증에 실패하면 예외가 그대로 전파된다")
    void getList_ownershipValidationFails_propagatesException() {
        given(catalystOwnershipValidator.validate(OWNER_ID, CATALYST_ID))
                .willThrow(new BusinessException(CatalystErrorCode.CATALYST_NOT_FOUND));

        assertThatThrownBy(() -> service().getList(OWNER_ID, CATALYST_ID, "PENDING", null, 7))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(CatalystErrorCode.CATALYST_NOT_FOUND);
    }

    @Test
    @DisplayName("size개 이하로 조회되면 hasNext는 false이고 nextCursor는 null이다")
    void getList_fewerThanSize_hasNextFalse() {
        given(catalystOwnershipValidator.validate(OWNER_ID, CATALYST_ID)).willReturn(catalyst());
        given(signalRepository.findFirstPage(eq(CATALYST_ID), eq(SignalStatus.PENDING), any(Pageable.class)))
                .willReturn(List.of(signal("https://n.news.naver.com/1")));

        SignalListResponse response = service().getList(OWNER_ID, CATALYST_ID, "PENDING", null, 7);

        assertThat(response.signals()).hasSize(1);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }

    @Test
    @DisplayName("size+1건이 조회되면 hasNext는 true이고 size만큼만 반환하며 nextCursor가 채워진다")
    void getList_moreThanSize_hasNextTrueAndTrimmed() {
        given(catalystOwnershipValidator.validate(OWNER_ID, CATALYST_ID)).willReturn(catalyst());
        given(signalRepository.findFirstPage(eq(CATALYST_ID), eq(SignalStatus.PENDING), any(Pageable.class)))
                .willReturn(List.of(signal("https://n.news.naver.com/1"), signal("https://n.news.naver.com/2")));

        SignalListResponse response = service().getList(OWNER_ID, CATALYST_ID, "PENDING", null, 1);

        assertThat(response.signals()).hasSize(1);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isNotNull();
    }

    @Test
    @DisplayName("cursor가 있으면 findNextPage로 조회한다")
    void getList_withCursor_usesFindNextPage() {
        given(catalystOwnershipValidator.validate(OWNER_ID, CATALYST_ID)).willReturn(catalyst());
        String cursor = SignalCursor.encode(java.time.LocalDateTime.of(2026, 8, 14, 10, 0, 0), 999L);
        given(signalRepository.findNextPage(eq(CATALYST_ID), eq(SignalStatus.PENDING), any(), eq(999L), any(Pageable.class)))
                .willReturn(List.of());

        SignalListResponse response = service().getList(OWNER_ID, CATALYST_ID, "PENDING", cursor, 7);

        assertThat(response.signals()).isEmpty();
    }

    @Test
    @DisplayName("status가 ADOPTED면 INVALID_INPUT 예외가 발생한다")
    void getList_statusAdopted_throwsInvalidInput() {
        given(catalystOwnershipValidator.validate(OWNER_ID, CATALYST_ID)).willReturn(catalyst());

        assertThatThrownBy(() -> service().getList(OWNER_ID, CATALYST_ID, "ADOPTED", null, 7))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(GlobalErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("status가 정의된 값이 아니면 INVALID_INPUT 예외가 발생한다")
    void getList_invalidStatus_throwsInvalidInput() {
        given(catalystOwnershipValidator.validate(OWNER_ID, CATALYST_ID)).willReturn(catalyst());

        assertThatThrownBy(() -> service().getList(OWNER_ID, CATALYST_ID, "UNKNOWN", null, 7))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(GlobalErrorCode.INVALID_INPUT);
    }
}
