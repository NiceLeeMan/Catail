package com.catail.backend.company.outbound.nasdaq;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class NasdaqApiClientConfig {

    @Bean
    public RestClient nasdaqApiRestClient(@Value("${nasdaq.api.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
