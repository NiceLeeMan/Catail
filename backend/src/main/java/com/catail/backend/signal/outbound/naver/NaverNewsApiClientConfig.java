package com.catail.backend.signal.outbound.naver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * <h3>네이버 뉴스 API 클라이언트 구성 클래스</h3>
 *
 * <p>네이버 뉴스 API 호출에 사용할 {@link RestClient}를 생성하여
 * Spring Bean으로 등록한다.</p>
 *
 * <p>모든 요청에 공통으로 적용되는 Base URL과
 * 클라이언트 ID·Secret 기반 인증 헤더를 설정한다.</p>
 */
@Configuration
public class NaverNewsApiClientConfig {

    /**
     * 네이버 뉴스 API 전용 {@link RestClient}를 생성한다.
     *
     * @param baseUrl      네이버 뉴스 API 기본 주소
     * @param clientId     네이버 API 클라이언트 식별자
     * @param clientSecret 네이버 API 인증 Secret
     * @return 공통 주소와 인증 헤더가 설정된 RestClient
     */
    @Bean
    public RestClient naverNewsApiRestClient(
            @Value("${naver.api.base-url}") String baseUrl,
            @Value("${naver.api.client-id}") String clientId,
            @Value("${naver.api.client-secret}") String clientSecret
    ) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-NCP-APIGW-API-KEY-ID", clientId)
                .defaultHeader("X-NCP-APIGW-API-KEY", clientSecret)
                .build();
    }
}
