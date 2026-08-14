package com.catail.backend.signal.db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class SignalSourceQueryRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private SignalSourceQueryRepository signalSourceQueryRepository;

    @Test
    @DisplayName("동일 시그널에 여러 검색어를 1:N으로 저장할 수 있다")
    void save_multipleQueriesForSameSignal_persistsAll() {
        signalSourceQueryRepository.save(SignalSourceQuery.create(1L, "삼성전자 공급망"));
        signalSourceQueryRepository.save(SignalSourceQuery.create(1L, "삼성전자 반도체 수급"));

        var all = signalSourceQueryRepository.findAll();

        assertThat(all).extracting(SignalSourceQuery::getSignalId).containsExactly(1L, 1L);
        assertThat(all).extracting(SignalSourceQuery::getQuery)
                .containsExactlyInAnyOrder("삼성전자 공급망", "삼성전자 반도체 수급");
    }
}
