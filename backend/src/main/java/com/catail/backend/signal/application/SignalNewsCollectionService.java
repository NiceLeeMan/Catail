package com.catail.backend.signal.application;

import com.catail.backend.signal.outbound.naver.NaverNewsItem;
import com.catail.backend.signal.outbound.naver.NaverNewsSearchAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
/**
 * <h3>검색어 기반 네이버 뉴스 수집 서비스</h3>
 *
 * <p>여러 검색어에 대한 네이버 뉴스 검색 요청을 전용 Executor에 비동기로 제출하여
 * 동시에 처리하고, 검색어별 결과를 하나의 Map으로 취합한다.</p>
 *
 * <p>개별 뉴스 검색은 비동기로 실행되지만, 모든 검색이 완료될 때까지 기다린 후
 * 전체 결과를 호출자에게 반환한다.</p>
 */
@Service
@RequiredArgsConstructor
public class SignalNewsCollectionService {

    private final NaverNewsSearchAdapter naverNewsSearchAdapter;
    private final ExecutorService naverNewsSearchExecutor;

    /**
     * 각 검색어의 뉴스 검색을 비동기로 실행하고 결과를 검색어별로 취합한다.
     *
     * <p>모든 검색 작업을 먼저 전용 Executor에 제출한 뒤 완료를 기다리므로,
     * 검색어를 순차적으로 처리하는 것보다 전체 수집 시간을 단축할 수 있다.</p>
     *
     * <p>반환되는 Map은 입력 검색어의 순서를 유지하며,
     * 하나의 검색 작업이라도 실패하면 전체 수집 작업이 실패한다.</p>
     *
     * @param queries 뉴스 검색에 사용할 검색어 목록
     * @return 검색어별 네이버 뉴스 검색 결과
     */
    public Map<String, List<NaverNewsItem>> collect(List<String> queries) {
        Map<String, CompletableFuture<List<NaverNewsItem>>> futuresByQuery = new LinkedHashMap<>();
        for (String query : queries) {
            futuresByQuery.put(query, CompletableFuture.supplyAsync(
                    () -> naverNewsSearchAdapter.search(query), naverNewsSearchExecutor));
        }

        CompletableFuture.allOf(futuresByQuery.values().toArray(CompletableFuture[]::new)).join();

        Map<String, List<NaverNewsItem>> resultsByQuery = new LinkedHashMap<>();
        futuresByQuery.forEach((query, future) -> resultsByQuery.put(query, future.join()));
        return resultsByQuery;
    }
}
