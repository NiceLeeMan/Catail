package com.catail.backend.signal.outbound.naver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <h3>네이버 뉴스 검색 Executor 구성 클래스</h3>
 *
 * <p>여러 검색어의 네이버 뉴스 API 요청을 비동기로 실행할 때 사용할
 * 전용 {@link ExecutorService}를 Spring Bean으로 등록한다.</p>
 */
@Configuration
public class NaverNewsSearchExecutorConfig {

    /**
     * 네이버 뉴스 검색 작업을 최대 10개까지 동시에 처리하는
     * 고정 크기 스레드 풀을 생성한다.
     *
     * <p>애플리케이션 종료 시 Spring이 {@code shutdown()}을 호출하여
     * Executor가 사용한 스레드를 정리한다.</p>
     *
     * @return 네이버 뉴스 검색 전용 ExecutorService
     */
    @Bean(destroyMethod = "shutdown")
    public ExecutorService naverNewsSearchExecutor() {
        return Executors.newFixedThreadPool(10);
    }
}
