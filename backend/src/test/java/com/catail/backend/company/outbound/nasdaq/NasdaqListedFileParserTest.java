package com.catail.backend.company.outbound.nasdaq;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NasdaqListedFileParserTest {

    @Test
    @DisplayName("헤더를 기준으로 각 컬럼을 파싱하고 File Creation Time 행은 제외한다")
    void parse_headerBasedColumns_excludesFooterLine() {
        String raw = """
                Symbol|Security Name|Market Category|Test Issue|Financial Status|Round Lot Size|ETF|NextShares
                AAPL|Apple Inc. - Common Stock|Q|N|N|100|N|N
                GOOGL|Alphabet Inc. - Class A Common Stock|Q|N|N|100|N|N
                File Creation Time: 0725202600:00|||||||
                """;

        List<NasdaqListedRecord> records = NasdaqListedFileParser.parse(raw);

        assertThat(records).hasSize(2);
        assertThat(records.get(0).symbol()).isEqualTo("AAPL");
        assertThat(records.get(0).securityName()).isEqualTo("Apple Inc. - Common Stock");
        assertThat(records.get(0).marketCategory()).isEqualTo("Q");
        assertThat(records.get(0).testIssue()).isEqualTo("N");
        assertThat(records.get(0).etf()).isEqualTo("N");
        assertThat(records.get(0).nextShares()).isEqualTo("N");
    }

    @Test
    @DisplayName("컬럼 순서가 바뀌어도 헤더 이름으로 매핑한다")
    void parse_reorderedColumns_mapsByHeaderName() {
        String raw = """
                ETF|Symbol|NextShares|Security Name|Test Issue|Market Category
                N|AAPL|N|Apple Inc. - Common Stock|N|Q
                """;

        List<NasdaqListedRecord> records = NasdaqListedFileParser.parse(raw);

        assertThat(records).hasSize(1);
        assertThat(records.get(0).symbol()).isEqualTo("AAPL");
        assertThat(records.get(0).marketCategory()).isEqualTo("Q");
    }

    @Test
    @DisplayName("빈 문자열이면 빈 목록을 반환한다")
    void parse_blankInput_returnsEmptyList() {
        assertThat(NasdaqListedFileParser.parse("")).isEmpty();
        assertThat(NasdaqListedFileParser.parse(null)).isEmpty();
    }
}
