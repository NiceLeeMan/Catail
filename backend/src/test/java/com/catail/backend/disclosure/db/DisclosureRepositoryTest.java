package com.catail.backend.disclosure.db;

import com.catail.backend.disclosure.domain.DisclosureProvider;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class DisclosureRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private DisclosureRepository disclosureRepository;

    private Disclosure disclosure(String externalId, LocalDate receivedDate) {
        return Disclosure.create(1L, DisclosureProvider.OPEN_DART, externalId, receivedDate, "보고서", "삼성전자");
    }

    @Test
    @DisplayName("findFirstPage는 접수일자 내림차순, 동일일자는 접수번호 내림차순으로 정렬한다")
    void findFirstPage_ordersByReceivedDateThenExternalIdDescending() {
        disclosureRepository.save(disclosure("20240101000001", LocalDate.of(2024, 1, 1)));
        disclosureRepository.save(disclosure("20240103000001", LocalDate.of(2024, 1, 3)));
        disclosureRepository.save(disclosure("20240102000002", LocalDate.of(2024, 1, 2)));
        disclosureRepository.save(disclosure("20240102000001", LocalDate.of(2024, 1, 2)));

        List<Disclosure> result = disclosureRepository.findFirstPage(
                1L, DisclosureProvider.OPEN_DART, PageRequest.of(0, 10));

        assertThat(result).extracting(Disclosure::getExternalDisclosureId)
                .containsExactly("20240103000001", "20240102000002", "20240102000001", "20240101000001");
    }

    @Test
    @DisplayName("findNextPage는 커서 이후 데이터만 반환한다")
    void findNextPage_returnsOnlyItemsAfterCursor() {
        disclosureRepository.save(disclosure("20240101000001", LocalDate.of(2024, 1, 1)));
        disclosureRepository.save(disclosure("20240102000002", LocalDate.of(2024, 1, 2)));
        disclosureRepository.save(disclosure("20240102000001", LocalDate.of(2024, 1, 2)));

        List<Disclosure> result = disclosureRepository.findNextPage(
                1L, DisclosureProvider.OPEN_DART,
                LocalDate.of(2024, 1, 2), "20240102000002",
                PageRequest.of(0, 10));

        assertThat(result).extracting(Disclosure::getExternalDisclosureId)
                .containsExactly("20240102000001", "20240101000001");
    }

    @Test
    @DisplayName("provider와 externalDisclosureId로 공시를 조회한다")
    void findByProviderAndExternalDisclosureId_existing_returnsDisclosure() {
        disclosureRepository.save(disclosure("20240101000001", LocalDate.of(2024, 1, 1)));

        Optional<Disclosure> result = disclosureRepository.findByProviderAndExternalDisclosureId(
                DisclosureProvider.OPEN_DART, "20240101000001");

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("다른 company의 공시는 조회 결과에 포함되지 않는다")
    void findFirstPage_differentCompany_excluded() {
        Disclosure otherCompanyDisclosure = Disclosure.create(
                2L, DisclosureProvider.OPEN_DART, "20240101000009", LocalDate.of(2024, 1, 1), "보고서", "다른회사");
        disclosureRepository.save(otherCompanyDisclosure);
        disclosureRepository.save(disclosure("20240101000001", LocalDate.of(2024, 1, 1)));

        List<Disclosure> result = disclosureRepository.findFirstPage(
                1L, DisclosureProvider.OPEN_DART, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExternalDisclosureId()).isEqualTo("20240101000001");
    }
}
