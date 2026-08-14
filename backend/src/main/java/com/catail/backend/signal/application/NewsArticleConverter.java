package com.catail.backend.signal.application;

import com.catail.backend.signal.domain.NewsArticle;
import com.catail.backend.signal.outbound.naver.NaverNewsItem;
import com.catail.backend.signal.outbound.naver.NaverPressCrawler;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class NewsArticleConverter {

    private final NaverPressCrawler naverPressCrawler;

    public NewsArticle convert(NewsArticleCandidate candidate) {
        NaverNewsItem item = candidate.item();
        String press = naverPressCrawler.crawl(candidate.normalizedLink()).orElse(null);

        return new NewsArticle(
                cleanHtml(item.title()),
                cleanHtml(item.description()),
                item.originallink(),
                candidate.normalizedLink(),
                parsePubDate(item.pubDate()),
                press,
                candidate.sourceQueries()
        );
    }

    private String cleanHtml(String raw) {
        return Jsoup.parse(raw).text();
    }

    private OffsetDateTime parsePubDate(String rfc822) {
        return OffsetDateTime.parse(rfc822, DateTimeFormatter.RFC_1123_DATE_TIME);
    }
}
