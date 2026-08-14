package com.catail.backend.catalyst.db;

import com.catail.backend.catalyst.domain.CatalystCategory;
import com.catail.backend.catalyst.domain.CatalystStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class CatalystRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private CatalystRepository catalystRepository;

    private Catalyst catalyst(Long userId, Long companyId, CatalystCategory category, String title) {
        return Catalyst.create(userId, companyId, category, "테스트용 상세내용 10자 이상 작성", title, CatalystStatus.ACTIVE);
    }

    private Catalyst softDeleted(Long userId, Long companyId, CatalystCategory category, String title) {
        Catalyst catalyst = catalyst(userId, companyId, category, title);
        catalyst.softDelete();
        return catalyst;
    }

    @Test
    @DisplayName("countActive는 삭제되지 않은 카탈리스트 개수만 센다")
    void countActive_excludesSoftDeleted() {
        catalystRepository.save(catalyst(1L, 10L, CatalystCategory.SUPPLY_CHAIN, "공급망"));
        catalystRepository.save(catalyst(1L, 10L, CatalystCategory.GOVERNANCE, "경영권/지배구조"));
        catalystRepository.save(softDeleted(1L, 10L, CatalystCategory.NEW_BUSINESS, "신사업/전략"));

        long count = catalystRepository.countActive(1L, 10L);

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("countActive는 다른 사용자·다른 기업의 카탈리스트는 포함하지 않는다")
    void countActive_excludesOtherUserOrCompany() {
        catalystRepository.save(catalyst(1L, 10L, CatalystCategory.SUPPLY_CHAIN, "공급망"));
        catalystRepository.save(catalyst(2L, 10L, CatalystCategory.SUPPLY_CHAIN, "공급망"));
        catalystRepository.save(catalyst(1L, 20L, CatalystCategory.SUPPLY_CHAIN, "공급망"));

        long count = catalystRepository.countActive(1L, 10L);

        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("countActiveByCategory는 동일 카테고리이면서 삭제되지 않은 카탈리스트만 센다")
    void countActiveByCategory_countsOnlyMatchingActiveCategory() {
        catalystRepository.save(catalyst(1L, 10L, CatalystCategory.SUPPLY_CHAIN, "공급망"));
        catalystRepository.save(softDeleted(1L, 10L, CatalystCategory.SUPPLY_CHAIN, "공급망"));
        catalystRepository.save(catalyst(1L, 10L, CatalystCategory.GOVERNANCE, "경영권/지배구조"));

        long count = catalystRepository.countActiveByCategory(1L, 10L, CatalystCategory.SUPPLY_CHAIN);

        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("findActive는 생성일시 내림차순(최신순)으로 정렬한다")
    void findActive_ordersByCreatedAtDescending() throws InterruptedException {
        Catalyst first = catalystRepository.save(catalyst(1L, 10L, CatalystCategory.SUPPLY_CHAIN, "공급망"));
        Thread.sleep(10);
        Catalyst second = catalystRepository.save(catalyst(1L, 10L, CatalystCategory.GOVERNANCE, "경영권/지배구조"));
        Thread.sleep(10);
        Catalyst third = catalystRepository.save(catalyst(1L, 10L, CatalystCategory.NEW_BUSINESS, "신사업/전략"));

        List<Catalyst> result = catalystRepository.findActive(1L, 10L);

        assertThat(result).extracting(Catalyst::getId)
                .containsExactly(third.getId(), second.getId(), first.getId());
    }

    @Test
    @DisplayName("findActive는 다른 사용자·다른 기업, 소프트 삭제된 카탈리스트는 제외한다")
    void findActive_excludesOtherUserOrCompanyAndSoftDeleted() {
        Catalyst target = catalystRepository.save(catalyst(1L, 10L, CatalystCategory.SUPPLY_CHAIN, "공급망"));
        catalystRepository.save(catalyst(2L, 10L, CatalystCategory.SUPPLY_CHAIN, "공급망"));
        catalystRepository.save(catalyst(1L, 20L, CatalystCategory.SUPPLY_CHAIN, "공급망"));
        catalystRepository.save(softDeleted(1L, 10L, CatalystCategory.GOVERNANCE, "경영권/지배구조"));

        List<Catalyst> result = catalystRepository.findActive(1L, 10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("findActive는 카탈리스트가 없으면 빈 목록을 반환한다")
    void findActive_noCatalysts_returnsEmptyList() {
        List<Catalyst> result = catalystRepository.findActive(1L, 999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAllByStatus는 사용자·기업에 관계없이 해당 상태이면서 삭제되지 않은 카탈리스트를 전부 반환한다")
    void findAllByStatus_returnsMatchingAcrossAllUsers() {
        Catalyst target1 = catalystRepository.save(catalyst(1L, 10L, CatalystCategory.SUPPLY_CHAIN, "공급망"));
        Catalyst target2 = catalystRepository.save(catalyst(2L, 20L, CatalystCategory.SUPPLY_CHAIN, "공급망"));
        Catalyst deleted = catalyst(1L, 30L, CatalystCategory.SUPPLY_CHAIN, "공급망");
        deleted.softDelete();
        catalystRepository.save(deleted);

        List<Catalyst> result = catalystRepository.findAllByStatus(CatalystStatus.ACTIVE);

        assertThat(result).extracting(Catalyst::getId)
                .containsExactlyInAnyOrder(target1.getId(), target2.getId());
    }

    @Test
    @DisplayName("findAllByStatus는 다른 상태의 카탈리스트는 포함하지 않는다")
    void findAllByStatus_excludesOtherStatus() {
        catalystRepository.save(catalyst(1L, 10L, CatalystCategory.SUPPLY_CHAIN, "공급망"));
        catalystRepository.save(Catalyst.create(1L, 20L, CatalystCategory.SUPPLY_CHAIN,
                "테스트용 상세내용 10자 이상 작성", "공급망", CatalystStatus.PAUSED));

        List<Catalyst> result = catalystRepository.findAllByStatus(CatalystStatus.ACTIVE);

        assertThat(result).hasSize(1);
    }
}
