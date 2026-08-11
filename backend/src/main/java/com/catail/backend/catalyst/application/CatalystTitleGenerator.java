package com.catail.backend.catalyst.application;

import com.catail.backend.catalyst.domain.CatalystCategory;

public final class CatalystTitleGenerator {

    private CatalystTitleGenerator() {
    }

    public static String generate(CatalystCategory category, long sameCategoryCount) {
        return sameCategoryCount == 0
                ? category.getLabel()
                : category.getLabel() + " #" + (sameCategoryCount + 1);
    }
}
