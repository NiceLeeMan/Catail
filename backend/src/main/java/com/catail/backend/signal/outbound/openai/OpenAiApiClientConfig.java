package com.catail.backend.signal.outbound.openai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OpenAiApiClientConfig {

    @Bean
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
