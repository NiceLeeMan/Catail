package com.catail.backend.company.outbound.nasdaq;

import com.catail.backend.company.domain.Market;
import com.catail.backend.company.outbound.CollectionMode;
import com.catail.backend.company.outbound.ListedCompanyCollectionException;
import com.catail.backend.company.outbound.ListedCompanyCollectionPort;
import com.catail.backend.company.outbound.ListedCompanyItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
public class NasdaqListedCompanyAdapter implements ListedCompanyCollectionPort {

    private static final String LISTED_FILE_PATH = "/dynamic/SymDir/nasdaqlisted.txt";
    private static final Set<String> VALID_MARKET_CATEGORIES = Set.of("Q", "G", "S");
    private static final String FLAG_NO = "N";

    private final RestClient nasdaqApiRestClient;

    public NasdaqListedCompanyAdapter(@Qualifier("nasdaqApiRestClient") RestClient nasdaqApiRestClient) {
        this.nasdaqApiRestClient = nasdaqApiRestClient;
    }

    @Override
    public Market market() {
        return Market.NASDAQ;
    }

    @Override
    public CollectionMode mode() {
        return CollectionMode.SNAPSHOT;
    }

    @Override
    public List<ListedCompanyItem> fetchSnapshot() {
        List<NasdaqListedRecord> records = NasdaqListedFileParser.parse(fetchListedFile());

        return records.stream()
                .filter(this::isEligibleListing)
                .map(this::toListedCompanyItem)
                .flatMap(Optional::stream)
                .toList();
    }

    private String fetchListedFile() {
        try {
            return nasdaqApiRestClient.get()
                    .uri(LISTED_FILE_PATH)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            throw new ListedCompanyCollectionException("Nasdaq 상장목록 파일 호출에 실패했습니다.", e);
        }
    }

    private boolean isEligibleListing(NasdaqListedRecord record) {
        return FLAG_NO.equals(record.testIssue())
                && FLAG_NO.equals(record.etf())
                && FLAG_NO.equals(record.nextShares())
                && VALID_MARKET_CATEGORIES.contains(record.marketCategory());
    }

    private Optional<ListedCompanyItem> toListedCompanyItem(NasdaqListedRecord record) {
        return NasdaqSecurityNameClassifier.classify(record.symbol(), record.securityName())
                .map(displayName -> new ListedCompanyItem(
                        Market.NASDAQ,
                        record.symbol().toUpperCase(Locale.ROOT),
                        displayName));
    }
}
