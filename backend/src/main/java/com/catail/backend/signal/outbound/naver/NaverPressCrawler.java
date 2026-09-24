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
 * <h3>네이버 뉴스 언론사명 크롤러</h3>
 *
 * <p>기사 URL이 네이버 뉴스 페이지({@code n.news.naver.com})인 경우
 * 해당 페이지의 정적 HTML을 조회하여 언론사명을 추출한다.</p>
 *
 * <p>언론사명은 헤더 로고 이미지의 {@code alt} 속성에 포함되어 있으므로
 * 별도의 JavaScript 실행 없이 Jsoup으로 파싱한다.</p>
 *
 * <p>지원하지 않는 URL이거나 페이지 조회·언론사명 추출에 실패하면
 * 빈 {@link Optional}을 반환한다.</p>
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

    /**
     * 네이버 뉴스 페이지에서 언론사명을 추출한다.
     *
     * @param link 언론사명을 확인할 뉴스 기사 URL
     * @return 추출된 언론사명 또는 조회·추출할 수 없는 경우 빈 Optional
     */
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
