package com.catail.backend.signal.outbound.naver;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

/**
 * link가 네이버 뉴스 페이지(n.news.naver.com)인 경우에만 해당 페이지를 크롤링해 언론사명을 채운다.
 * 실측 결과 이 페이지는 정적 HTML에 언론사명이 그대로 내려온다(JS 실행 불필요) — 헤더 로고 이미지의
 * alt 속성(예: alt="연합뉴스")으로 확인됨.
 */
@Slf4j
@Component
public class NaverPressCrawler {

    private static final String NAVER_NEWS_HOST = "n.news.naver.com";
    private static final String PRESS_LOGO_SELECTOR = "img.media_end_head_top_img";

    private final RestClient naverNewsPageRestClient;

    public NaverPressCrawler(@Qualifier("naverNewsPageRestClient") RestClient naverNewsPageRestClient) {
        this.naverNewsPageRestClient = naverNewsPageRestClient;
    }

    public Optional<String> crawl(String link) {
        if (link == null || !link.contains(NAVER_NEWS_HOST)) {
            return Optional.empty();
        }

        try {
            String html = naverNewsPageRestClient.get().uri(link).retrieve().body(String.class);
            if (html == null) {
                return Optional.empty();
            }

            Document document = Jsoup.parse(html);
            Element logo = document.selectFirst(PRESS_LOGO_SELECTOR);
            if (logo == null || logo.attr("alt").isBlank()) {
                return Optional.empty();
            }
            return Optional.of(logo.attr("alt"));
        } catch (RestClientException e) {
            log.warn("네이버 뉴스 페이지 크롤링 실패 (link={})", link, e);
            return Optional.empty();
        }
    }
}
