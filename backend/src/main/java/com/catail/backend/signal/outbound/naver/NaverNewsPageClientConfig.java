package com.catail.backend.signal.outbound.naver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
/**
 * <h3>네이버 뉴스 페이지 클라이언트 구성 클래스</h3>
 *
 * <p>네이버 뉴스 페이지의 HTML을 조회할 때 사용할 {@link RestClient}를
 * Spring Bean으로 등록한다.</p>
 *
 * <p>요청마다 완전한 페이지 URL을 전달하므로 별도의 Base URL이나
 * API 인증 정보를 설정하지 않는다.</p>
 */
@Configuration
public class NaverNewsPageClientConfig {

    /**
     * 네이버 뉴스 페이지 조회용 {@link RestClient}를 생성한다.
     *
     * @return 네이버 뉴스 페이지 조회에 사용할 RestClient
     */
    @Bean
    public RestClient naverNewsPageRestClient() {
        return RestClient.builder().build();
    }
}
