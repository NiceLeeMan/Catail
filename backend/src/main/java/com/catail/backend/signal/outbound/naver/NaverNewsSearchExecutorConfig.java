package com.catail.backend.signal.outbound.naver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class NaverNewsSearchExecutorConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService naverNewsSearchExecutor() {
        return Executors.newFixedThreadPool(10);
    }
}
