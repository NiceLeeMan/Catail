package com.catail.backend.signal.db;

import com.catail.backend.signal.domain.SignalStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class SignalRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private SignalRepository signalRepository;

    private Signal signal(Long catalystId, String link) {
        return Signal.create(catalystId, "제목", "요약", "https://origin.example.com/1",
                link, OffsetDateTime.now(), "언론사");
    }

    @Test
    @DisplayName("create로 생성한 시그널은 PENDING 상태로 저장된다")
    void create_savesWithPendingStatus() {
        Signal saved = signalRepository.save(signal(1L, "https://n.news.naver.com/1"));

        Signal found = signalRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getStatus()).isEqualTo(SignalStatus.PENDING);
    }

    @Test
    @DisplayName("existsByCatalystIdAndLink는 동일 카탈리스트에 동일 link가 있으면 true를 반환한다")
    void existsByCatalystIdAndLink_existingLink_returnsTrue() {
        signalRepository.save(signal(1L, "https://n.news.naver.com/1"));

        boolean exists = signalRepository.existsByCatalystIdAndLink(1L, "https://n.news.naver.com/1");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByCatalystIdAndLink는 다른 카탈리스트의 동일 link는 포함하지 않는다")
    void existsByCatalystIdAndLink_differentCatalyst_returnsFalse() {
        signalRepository.save(signal(1L, "https://n.news.naver.com/1"));

        boolean exists = signalRepository.existsByCatalystIdAndLink(2L, "https://n.news.naver.com/1");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByCatalystIdAndLink는 존재하지 않는 link면 false를 반환한다")
    void existsByCatalystIdAndLink_nonExistingLink_returnsFalse() {
        signalRepository.save(signal(1L, "https://n.news.naver.com/1"));

        boolean exists = signalRepository.existsByCatalystIdAndLink(1L, "https://n.news.naver.com/2");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("findFirstPage는 생성일시 내림차순, 동일시각이면 id 내림차순으로 정렬한다")
    void findFirstPage_ordersByCreatedAtThenIdDescending() throws InterruptedException {
        Signal first = signalRepository.save(signal(1L, "https://n.news.naver.com/1"));
        Thread.sleep(10);
        Signal second = signalRepository.save(signal(1L, "https://n.news.naver.com/2"));
        Thread.sleep(10);
        Signal third = signalRepository.save(signal(1L, "https://n.news.naver.com/3"));

        List<Signal> result = signalRepository.findFirstPage(1L, SignalStatus.PENDING, PageRequest.of(0, 10));

        assertThat(result).extracting(Signal::getId)
                .containsExactly(third.getId(), second.getId(), first.getId());
    }

    @Test
    @DisplayName("findFirstPage는 다른 카탈리스트이거나 다른 status인 시그널은 제외한다")
    void findFirstPage_excludesOtherCatalystOrStatus() {
        signalRepository.save(signal(2L, "https://n.news.naver.com/1"));
        Signal excluded = signal(1L, "https://n.news.naver.com/2");
        excluded.changeStatus(SignalStatus.EXCLUDED);
        signalRepository.save(excluded);
        Signal target = signalRepository.save(signal(1L, "https://n.news.naver.com/3"));

        List<Signal> result = signalRepository.findFirstPage(1L, SignalStatus.PENDING, PageRequest.of(0, 10));

        assertThat(result).extracting(Signal::getId).containsExactly(target.getId());
    }

    @Test
    @DisplayName("findNextPage는 커서 이후 데이터만 반환한다")
    void findNextPage_returnsOnlyItemsAfterCursor() throws InterruptedException {
        Signal first = signalRepository.save(signal(1L, "https://n.news.naver.com/1"));
        Thread.sleep(10);
        Signal second = signalRepository.save(signal(1L, "https://n.news.naver.com/2"));
        Thread.sleep(10);
        Signal third = signalRepository.save(signal(1L, "https://n.news.naver.com/3"));

        List<Signal> result = signalRepository.findNextPage(
                1L, SignalStatus.PENDING, third.getCreatedAt(), third.getId(), PageRequest.of(0, 10));

        assertThat(result).extracting(Signal::getId).containsExactly(second.getId(), first.getId());
    }

    private Signal adoptedSignal(Long catalystId, String link, OffsetDateTime pubDate) {
        Signal signal = Signal.create(catalystId, "제목", "요약", "https://origin.example.com/1",
                link, pubDate, "언론사");
        signal.changeStatus(SignalStatus.ADOPTED);
        return signal;
    }

    @Test
    @DisplayName("findTimeline은 발행일시(pubDate) 내림차순, 동일시각이면 id 내림차순으로 정렬한다")
    void findTimeline_ordersByPubDateThenIdDescending() {
        Signal older = signalRepository.save(adoptedSignal(1L, "https://n.news.naver.com/1",
                OffsetDateTime.parse("2026-08-10T09:00:00+09:00")));
        Signal newer = signalRepository.save(adoptedSignal(1L, "https://n.news.naver.com/2",
                OffsetDateTime.parse("2026-08-12T09:00:00+09:00")));

        List<Signal> result = signalRepository.findTimeline(1L, SignalStatus.ADOPTED);

        assertThat(result).extracting(Signal::getId).containsExactly(newer.getId(), older.getId());
    }

    @Test
    @DisplayName("findTimeline은 다른 카탈리스트이거나 ADOPTED가 아닌 시그널은 제외한다")
    void findTimeline_excludesOtherCatalystOrNonAdopted() {
        signalRepository.save(adoptedSignal(2L, "https://n.news.naver.com/1",
                OffsetDateTime.parse("2026-08-10T09:00:00+09:00")));
        signalRepository.save(signal(1L, "https://n.news.naver.com/2"));
        Signal target = signalRepository.save(adoptedSignal(1L, "https://n.news.naver.com/3",
                OffsetDateTime.parse("2026-08-11T09:00:00+09:00")));

        List<Signal> result = signalRepository.findTimeline(1L, SignalStatus.ADOPTED);

        assertThat(result).extracting(Signal::getId).containsExactly(target.getId());
    }

    @Test
    @DisplayName("findTimeline은 채택된 시그널이 없으면 빈 목록을 반환한다")
    void findTimeline_noAdoptedSignals_returnsEmptyList() {
        List<Signal> result = signalRepository.findTimeline(999L, SignalStatus.ADOPTED);

        assertThat(result).isEmpty();
    }
}
