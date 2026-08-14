package com.catail.backend.signal.application;

import com.catail.backend.catalyst.application.CatalystErrorCode;
import com.catail.backend.catalyst.db.Catalyst;
import com.catail.backend.catalyst.db.CatalystRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SignalListServiceTest {

    private static final Long OWNER_ID = 1L;
    private static final Long CATALYST_ID = 10L;

    @Mock private CatalystRepository catalystRepository;
    @Mock private SignalRepository signalRepository;

    private SignalListService service() {
        return new SignalListService(catalystRepository, signalRepository);
    }

    private Catalyst catalyst(Long userId) {
        return Catalyst.create(userId, 100L, CatalystCategory.SUPPLY_CHAIN,
                "테스트용 상세내용 10자 이상 작성", "공급망", CatalystStatus.ACTIVE);
    }

    private Signal signal(String link) {
        return Signal.create(CATALYST_ID, "제목", "요약", "https://origin.example.com/1",
                link, OffsetDateTime.now(), "언론사");
    }

    @Test
    @DisplayName("size개 이하로 조회되면 hasNext는 false이고 nextCursor는 null이다")
    void getList_fewerThanSize_hasNextFalse() {
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst(OWNER_ID)));
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
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst(OWNER_ID)));
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
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst(OWNER_ID)));
        String cursor = SignalCursor.encode(java.time.LocalDateTime.of(2026, 8, 14, 10, 0, 0), 999L);
        given(signalRepository.findNextPage(eq(CATALYST_ID), eq(SignalStatus.PENDING), any(), eq(999L), any(Pageable.class)))
                .willReturn(List.of());

        SignalListResponse response = service().getList(OWNER_ID, CATALYST_ID, "PENDING", cursor, 7);

        assertThat(response.signals()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 catalystId면 CATALYST_NOT_FOUND 예외가 발생한다")
    void getList_catalystNotFound_throwsCatalystNotFound() {
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service().getList(OWNER_ID, CATALYST_ID, "PENDING", null, 7))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(CatalystErrorCode.CATALYST_NOT_FOUND);
    }

    @Test
    @DisplayName("삭제된 카탈리스트면 CATALYST_NOT_FOUND 예외가 발생한다")
    void getList_deletedCatalyst_throwsCatalystNotFound() {
        Catalyst catalyst = catalyst(OWNER_ID);
        catalyst.softDelete();
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst));

        assertThatThrownBy(() -> service().getList(OWNER_ID, CATALYST_ID, "PENDING", null, 7))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(CatalystErrorCode.CATALYST_NOT_FOUND);
    }

    @Test
    @DisplayName("요청자가 소유자가 아니면 CATALYST_ACCESS_DENIED 예외가 발생한다")
    void getList_notOwner_throwsCatalystAccessDenied() {
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst(2L)));

        assertThatThrownBy(() -> service().getList(OWNER_ID, CATALYST_ID, "PENDING", null, 7))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(CatalystErrorCode.CATALYST_ACCESS_DENIED);
    }

    @Test
    @DisplayName("status가 ADOPTED면 INVALID_INPUT 예외가 발생한다")
    void getList_statusAdopted_throwsInvalidInput() {
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst(OWNER_ID)));

        assertThatThrownBy(() -> service().getList(OWNER_ID, CATALYST_ID, "ADOPTED", null, 7))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(GlobalErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("status가 정의된 값이 아니면 INVALID_INPUT 예외가 발생한다")
    void getList_invalidStatus_throwsInvalidInput() {
        given(catalystRepository.findById(CATALYST_ID)).willReturn(Optional.of(catalyst(OWNER_ID)));

        assertThatThrownBy(() -> service().getList(OWNER_ID, CATALYST_ID, "UNKNOWN", null, 7))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(GlobalErrorCode.INVALID_INPUT);
    }
}
