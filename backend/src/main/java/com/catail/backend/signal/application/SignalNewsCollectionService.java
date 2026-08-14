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

@Service
@RequiredArgsConstructor
public class SignalNewsCollectionService {

    private final NaverNewsSearchAdapter naverNewsSearchAdapter;
    private final ExecutorService naverNewsSearchExecutor;

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
