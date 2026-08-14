package com.catail.backend.signal.db;

import com.catail.backend.signal.domain.SignalStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;

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
}
