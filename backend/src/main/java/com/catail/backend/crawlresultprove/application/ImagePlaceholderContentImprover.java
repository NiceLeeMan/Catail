package com.catail.backend.crawlresultprove.application;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ImagePlaceholderContentImprover implements ContentImprover {

    private static final Pattern IMAGE_PLACEHOLDER = Pattern.compile("\\(Image\\s+\\d+\\s*:[^)]*\\)");

    private final BasicNormalizationContentImprover basicNormalizationContentImprover =
            new BasicNormalizationContentImprover();

    @Override
    public String improve(String content) {
        String normalized = basicNormalizationContentImprover.improve(content);
        String withoutImages = IMAGE_PLACEHOLDER.matcher(normalized).replaceAll("");
        return basicNormalizationContentImprover.improve(withoutImages);
    }
}
