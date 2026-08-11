package com.catail.backend.catalyst.application;

import com.catail.backend.catalyst.domain.CatalystCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CatalystTitleGeneratorTest {

    @Test
    @DisplayName("동일 카테고리 개수가 0이면 카테고리 라벨을 그대로 반환한다")
    void generate_noExisting_returnsLabelOnly() {
        String title = CatalystTitleGenerator.generate(CatalystCategory.SUPPLY_CHAIN, 0);

        assertThat(title).isEqualTo("공급망");
    }

    @Test
    @DisplayName("동일 카테고리 개수가 1 이상이면 순번(#N)이 붙는다")
    void generate_existing_returnsLabelWithSequence() {
        String title = CatalystTitleGenerator.generate(CatalystCategory.SUPPLY_CHAIN, 1);

        assertThat(title).isEqualTo("공급망 #2");
    }
}
