package com.catail.backend.signal.outbound.naver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class NaverNewsPageClientConfig {

    @Bean
    public RestClient naverNewsPageRestClient() {
        return RestClient.builder().build();
    }
}
