package com.catail.backend.company.outbound.nasdaq;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class NasdaqSecurityNameClassifierTest {

    @Test
    @DisplayName("Common Stock/Class 주식은 표시명을 추출해 포함한다")
    void classify_commonStock_returnsDisplayName() {
        assertThat(NasdaqSecurityNameClassifier.classify("AAPL", "Apple Inc. - Common Stock"))
                .contains("Apple Inc.");
        assertThat(NasdaqSecurityNameClassifier.classify("GOOGL", "Alphabet Inc. - Class A Common Stock"))
                .contains("Alphabet Inc.");
    }

    @Test
    @DisplayName("Ordinary Shares는 포함한다")
    void classify_ordinaryShares_returnsDisplayName() {
        assertThat(NasdaqSecurityNameClassifier.classify("SYM", "Some Corp Ltd - Ordinary Shares"))
                .contains("Some Corp Ltd");
    }

    @Test
    @DisplayName("American Depositary Shares(ADR/ADS)는 포함한다")
    void classify_americanDepositaryShares_returnsDisplayName() {
        String securityName = "JOYY Inc. - American Depositary Shares, "
                + "each representing one Class A Ordinary Share";

        Optional<String> result = NasdaqSecurityNameClassifier.classify("JOYY", securityName);

        assertThat(result).contains("JOYY Inc.");
    }

    @Test
    @DisplayName("워런트, 유닛, 우선주, Rights, 채권성 증권은 제외한다")
    void classify_excludedTypes_returnsEmpty() {
        assertThat(NasdaqSecurityNameClassifier.classify("SYM", "Example Corp - Warrant")).isEmpty();
        assertThat(NasdaqSecurityNameClassifier.classify("SYM", "Example Corp - Warrants expiring 2027"))
                .isEmpty();
        assertThat(NasdaqSecurityNameClassifier.classify("SYM", "Example Corp - Unit")).isEmpty();
        assertThat(NasdaqSecurityNameClassifier.classify("SYM", "Example Corp - Units")).isEmpty();
        assertThat(NasdaqSecurityNameClassifier.classify("SYM", "Example Corp - Preferred Stock")).isEmpty();
        assertThat(NasdaqSecurityNameClassifier.classify("SYM", "Example Corp - Rights")).isEmpty();
        assertThat(NasdaqSecurityNameClassifier.classify("SYM", "Example Corp - Senior Notes due 2030"))
                .isEmpty();
    }

    @Test
    @DisplayName("펀드성 증권은 Common Stock 문구가 섞여 있어도 제외한다")
    void classify_fundKeywordPresent_returnsEmptyEvenWithCommonStockWording() {
        assertThat(NasdaqSecurityNameClassifier.classify(
                "SYM", "Example Fund - Common Stock of a Closed End Fund"))
                .isEmpty();
    }

    @Test
    @DisplayName("' - ' 구분자가 없으면 판단 불가로 제외한다")
    void classify_noSeparator_returnsEmpty() {
        assertThat(NasdaqSecurityNameClassifier.classify("SYM", "Some Weird Listing Name")).isEmpty();
    }

    @Test
    @DisplayName("알려진 패턴에 매칭되지 않으면 판단 불가로 제외한다")
    void classify_unknownTypeSuffix_returnsEmpty() {
        assertThat(NasdaqSecurityNameClassifier.classify("SYM", "Example Corp - Something Unusual"))
                .isEmpty();
    }
}
