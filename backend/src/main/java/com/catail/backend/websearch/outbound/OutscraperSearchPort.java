package com.catail.backend.websearch.outbound;

import java.util.List;

public interface OutscraperSearchPort {

    OutscraperSubmitResult submitBatch(List<String> queryTexts, OutscraperSearchOptions options);

    OutscraperPollResult pollJob(String externalJobId);
}
