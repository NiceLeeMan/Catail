package com.catail.backend.crawlresultprove.application;

import org.springframework.stereotype.Component;

// TODO: 본문 정제/노이즈 제거 로직 확정 후 이 구현을 교체한다. 그 전까지는 원문을 그대로 다음 버전으로 넘긴다.
@Component
public class NoOpContentImprover implements ContentImprover {

    @Override
    public String improve(String content) {
        return content;
    }
}
