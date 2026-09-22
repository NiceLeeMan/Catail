package com.catail.backend.signal.outbound.openai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
/**
 * <h3>OpenAI 외부 API 클라이언트 구성 클래스</h3>
 *
 * <p>OpenAI API 호출에 사용할 {@link RestClient}를 생성하여 Spring Bean으로 등록한다.</p>
 *
 * <p>모든 OpenAI API 요청에 공통으로 적용되는 다음 정보를 설정한다.</p>
 * <ul>
 *     <li>OpenAI API Base URL</li>
 *     <li>API Key 기반 Authorization 헤더</li>
 * </ul>
 *
 * <p>이를 통해 외부 API 호출부가 서버 주소와 인증 정보를 반복해서 설정하지 않도록 한다.</p>
 */
public class OpenAiApiClientConfig {

    @Bean
    // 환경변수를 통해서 baseUrl과 apiKey를 주입 받는다.
    public RestClient openAiApiRestClient(
            @Value("${openai.api.base-url}") String baseUrl,
            @Value("${openai.api.key}") String apiKey
    ) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }
}
