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
}
