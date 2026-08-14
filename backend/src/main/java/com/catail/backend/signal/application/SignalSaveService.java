package com.catail.backend.signal.application;

import com.catail.backend.signal.db.Signal;
import com.catail.backend.signal.db.SignalRepository;
import com.catail.backend.signal.db.SignalSourceQuery;
import com.catail.backend.signal.db.SignalSourceQueryRepository;
import com.catail.backend.signal.domain.NewsArticle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignalSaveService {

    private final SignalRepository signalRepository;
    private final SignalSourceQueryRepository signalSourceQueryRepository;

    @Transactional
    public Signal save(Long catalystId, NewsArticle article) {
        Signal signal = signalRepository.save(Signal.create(
                catalystId, article.title(), article.description(),
                article.originallink(), article.link(), article.pubDate(), article.press()));

        for (String query : article.sourceQueries()) {
            signalSourceQueryRepository.save(SignalSourceQuery.create(signal.getId(), query));
        }

        return signal;
    }
}
