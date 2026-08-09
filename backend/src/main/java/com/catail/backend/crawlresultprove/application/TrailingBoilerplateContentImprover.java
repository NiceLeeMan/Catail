package com.catail.backend.crawlresultprove.application;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * 저작권 고지나 "많이 본 뉴스" 류 위젯 제목은 실제 본문이 끝나고 다른 기사의 헤드라인·조회수·
 * 이미지가 나열되는 영역의 시작을 알리는 경계선 역할을 한다. 이런 위젯 안의 개별 헤드라인은
 * 기사마다 문구가 달라 3단계처럼 반복 빈도로 걸러낼 수 없으므로, 경계선을 찾아 그 지점부터
 * 문서 끝까지 통째로 잘라내는 방식으로 처리한다. 경계선이 없는 문서는 그대로 둔다.
 */
@Component
public class TrailingBoilerplateContentImprover implements ContentImprover {

    private static final Pattern COPYRIGHT_LINE = Pattern.compile(
            "copyright|저작권자|ⓒ|무단전재|무단 전재|무단 전재.?재배포", Pattern.CASE_INSENSITIVE);

    private static final Set<String> TRAILING_WIDGET_TITLES = Set.of(
            "관련기사", "관련 기사", "관련뉴스", "관련 뉴스",
            "많이 본 뉴스", "많이 본 기사", "인기 뉴스", "핫뉴스", "핫이슈",
            "랭킹뉴스", "주요뉴스", "주요 기사", "포토뉴스", "긴급뉴스", "프리미엄뉴스",
            "이 시각 헤드라인", "오래 머문 뉴스", "공감 많은 뉴스", "놓치면 안되는 기사",
            "뉴스룸 PICK", "에디터스 픽 Editor's Picks", "다른기사 보기"
    );

    private final UiChromeContentImprover uiChromeContentImprover = new UiChromeContentImprover();
    private final BasicNormalizationContentImprover basicNormalizationContentImprover = new BasicNormalizationContentImprover();

    @Override
    public String improve(String content) {
        String preprocessed = uiChromeContentImprover.improve(content);

        String[] lines = preprocessed.split("\n", -1);
        int boundary = findBoundary(lines);
        if (boundary < 0) {
            return preprocessed;
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < boundary; i++) {
            result.append(lines[i]).append('\n');
        }

        return basicNormalizationContentImprover.improve(result.toString());
    }

    private int findBoundary(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].strip();
            if (line.isEmpty()) {
                continue;
            }
            if (COPYRIGHT_LINE.matcher(line).find() || TRAILING_WIDGET_TITLES.contains(line)) {
                return i;
            }
        }
        return -1;
    }
}
