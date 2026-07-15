package com.catail.backend.catalyst.db;

import com.catail.backend.catalyst.domain.CatalystDomain;
import com.catail.backend.catalyst.domain.CatalystStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CatalystMapperTest {

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("신규 컬럼(searchIntervalHours/lastSearchedAt/activatedAt)을 포함해 도메인으로 변환한다")
        void toDomain_mapsNewColumns() {
            Catalyst entity = new Catalyst();
            entity.setUserId(1L);
            entity.setTitle("title");
            entity.setContent("content");
            entity.setStatus(CatalystStatus.ACTIVE);
            entity.setSearchConditions(List.of("검색어"));
            entity.setSearchIntervalHours(6);

            LocalDateTime lastSearchedAt = LocalDateTime.now().minusHours(1);
            LocalDateTime activatedAt = LocalDateTime.now().minusDays(1);
            ReflectionTestUtils.setField(entity, "id", 1L);
            ReflectionTestUtils.setField(entity, "lastSearchedAt", lastSearchedAt);
            ReflectionTestUtils.setField(entity, "activatedAt", activatedAt);
            ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.now());
            ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.now());

            CatalystDomain domain = CatalystMapper.toDomain(entity, List.of(1L));

            assertThat(domain.getSearchIntervalHours()).isEqualTo(6);
            assertThat(domain.getLastSearchedAt()).isEqualTo(lastSearchedAt);
            assertThat(domain.getActivatedAt()).isEqualTo(activatedAt);
        }
    }

    @Nested
    @DisplayName("toNewEntity")
    class ToNewEntity {

        @Test
        @DisplayName("생성 도메인의 searchIntervalHours(기본값 4)를 엔티티에 반영한다")
        void toNewEntity_mapsSearchIntervalHours() {
            CatalystDomain domain = CatalystDomain.create(1L, "title", "a".repeat(50), List.of(1L), "ACTIVE");

            Catalyst entity = CatalystMapper.toNewEntity(domain);

            assertThat(entity.getSearchIntervalHours()).isEqualTo(4);
            assertThat(entity.getLastSearchedAt()).isNull();
            assertThat(entity.getActivatedAt()).isNull();
        }
    }
}
