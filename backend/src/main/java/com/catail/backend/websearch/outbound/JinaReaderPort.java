package com.catail.backend.websearch.outbound;

public interface JinaReaderPort {

    JinaReadResult read(String crawlUrl);
}
