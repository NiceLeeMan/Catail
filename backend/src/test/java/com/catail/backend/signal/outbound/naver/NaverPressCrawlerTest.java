package com.catail.backend.signal.outbound.naver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class NaverPressCrawlerTest {

    private static final String LINK = "https://n.news.naver.com/mnews/article/001/0016252366";

    private MockRestServiceServer mockServer;
    private NaverPressCrawler crawler;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        crawler = new NaverPressCrawler(builder.build());
    }

    @Test
    @DisplayName("네이버 뉴스 페이지면 헤더 로고 alt 속성에서 언론사명을 추출한다")
    void crawl_naverNewsPage_returnsPressName() {
        String html = """
                <html><body>
                <img src="logo.png" width="20" height="20" alt="연합뉴스" class="media_end_head_top_img" style="display:none;">
                </body></html>
                """;
        mockServer.expect(requestTo(LINK)).andExpect(method(GET))
                .andRespond(withSuccess(html, MediaType.parseMediaType("text/html;charset=UTF-8")));

        Optional<String> result = crawler.crawl(LINK);

        assertThat(result).contains("연합뉴스");
    }

    @Test
    @DisplayName("네이버 뉴스 페이지가 아니면 네트워크 호출 없이 빈 값을 반환한다")
    void crawl_notNaverNewsPage_returnsEmptyWithoutRequest() {
        Optional<String> result = crawler.crawl("https://www.chosun.com/article/1");

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    @DisplayName("로고 요소를 찾을 수 없으면 빈 값을 반환한다")
    void crawl_logoNotFound_returnsEmpty() {
        mockServer.expect(requestTo(LINK)).andExpect(method(GET))
                .andRespond(withSuccess("<html><body>본문만 있음</body></html>", MediaType.parseMediaType("text/html;charset=UTF-8")));

        Optional<String> result = crawler.crawl(LINK);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("페이지 요청이 실패해도 예외를 던지지 않고 빈 값을 반환한다")
    void crawl_requestFails_returnsEmptyWithoutThrowing() {
        mockServer.expect(requestTo(LINK)).andExpect(method(GET))
                .andRespond(withServerError());

        Optional<String> result = crawler.crawl(LINK);

        assertThat(result).isEmpty();
    }
}
