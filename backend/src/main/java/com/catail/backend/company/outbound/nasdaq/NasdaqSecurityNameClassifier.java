package com.catail.backend.company.outbound.nasdaq;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * nasdaqlisted.txt에는 증권 유형 전용 컬럼이 없어, "Security Name" 값에 관례적으로 포함된
 * "{@code <기업명> - <유형 설명>}" 접미사를 근거로 보통주/클래스주/Ordinary Shares/ADR·ADS만 걸러낸다.
 * 접미사가 없거나 알려진 패턴에 매칭되지 않으면 자동 포함하지 않고 로그만 남긴다.
 */
@Slf4j
final class NasdaqSecurityNameClassifier {

    private static final String SEPARATOR = " - ";

    private static final List<Pattern> INCLUDED_PATTERNS = List.of(
            Pattern.compile("common stock", Pattern.CASE_INSENSITIVE),
            Pattern.compile("ordinary shares", Pattern.CASE_INSENSITIVE),
            Pattern.compile("american depositary shares", Pattern.CASE_INSENSITIVE),
            Pattern.compile("american depositary receipt", Pattern.CASE_INSENSITIVE)
    );

    private static final List<Pattern> EXCLUDED_PATTERNS = List.of(
            Pattern.compile("\\bwarrants?\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bunits?\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bpreferred\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\brights?\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bnotes?\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bbonds?\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bdebentures?\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bfunds?\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\btrust\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bnextshares\\b", Pattern.CASE_INSENSITIVE)
    );

    private NasdaqSecurityNameClassifier() {
    }

    static Optional<String> classify(String symbol, String securityName) {
        if (securityName == null || !securityName.contains(SEPARATOR)) {
            log.warn("나스닥 증권 유형을 판단할 수 없어 제외합니다: symbol={}, securityName={}", symbol, securityName);
            return Optional.empty();
        }

        int separatorIndex = securityName.indexOf(SEPARATOR);
        String displayName = securityName.substring(0, separatorIndex).trim();
        String typeDescription = securityName.substring(separatorIndex + SEPARATOR.length()).trim();

        if (matchesAny(EXCLUDED_PATTERNS, typeDescription)) {
            return Optional.empty();
        }
        if (matchesAny(INCLUDED_PATTERNS, typeDescription)) {
            return Optional.of(displayName);
        }

        log.warn("나스닥 증권 유형을 판단할 수 없어 제외합니다: symbol={}, securityName={}", symbol, securityName);
        return Optional.empty();
    }

    private static boolean matchesAny(List<Pattern> patterns, String text) {
        return patterns.stream().anyMatch(pattern -> pattern.matcher(text).find());
    }
}
