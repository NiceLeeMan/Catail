package com.catail.backend.crawlresultprove.application;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BasicNormalizationContentImprover implements ContentImprover {

    private static final Pattern CODE_FENCE = Pattern.compile("^```[^\\n]*$", Pattern.MULTILINE);
    private static final Pattern INLINE_CODE = Pattern.compile("`([^`]+)`");
    private static final Pattern HEADING = Pattern.compile("^ {0,3}#{1,6}\\s+", Pattern.MULTILINE);
    private static final Pattern BOLD = Pattern.compile("\\*\\*([^*]+)\\*\\*|__([^_]+)__");
    private static final Pattern ITALIC = Pattern.compile("\\*([^*]+)\\*|_([^_]+)_");
    private static final Pattern BLOCKQUOTE = Pattern.compile("^ {0,3}>\\s?", Pattern.MULTILINE);
    private static final Pattern HORIZONTAL_RULE = Pattern.compile("^ {0,3}([-*_])( *\\1){2,} *$\\n?", Pattern.MULTILINE);
    private static final Pattern LIST_BULLET = Pattern.compile("^ {0,3}[-*+]\\s+", Pattern.MULTILINE);
    private static final Pattern LIST_ORDERED = Pattern.compile("^ {0,3}\\d+\\.\\s+", Pattern.MULTILINE);
    private static final Pattern LINK = Pattern.compile("\\[([^\\]]*)\\]\\([^)]*\\)");
    private static final Pattern HORIZONTAL_WHITESPACE_RUN = Pattern.compile("[ \\t]{2,}");
    private static final Pattern TRAILING_LINE_WHITESPACE = Pattern.compile("[ \\t]+\\n");
    private static final Pattern EXCESS_BLANK_LINES = Pattern.compile("\\n{3,}");

    @Override
    public String improve(String content) {
        String result = Normalizer.normalize(content, Normalizer.Form.NFC);
        result = result.replace("\r\n", "\n").replace("\r", "\n");

        result = CODE_FENCE.matcher(result).replaceAll("");
        result = INLINE_CODE.matcher(result).replaceAll("$1");
        result = HEADING.matcher(result).replaceAll("");
        result = BOLD.matcher(result).replaceAll(this::firstNonNullGroup);
        result = ITALIC.matcher(result).replaceAll(this::firstNonNullGroup);
        result = LINK.matcher(result).replaceAll("$1");
        result = BLOCKQUOTE.matcher(result).replaceAll("");
        result = HORIZONTAL_RULE.matcher(result).replaceAll("");
        result = LIST_BULLET.matcher(result).replaceAll("");
        result = LIST_ORDERED.matcher(result).replaceAll("");

        result = TRAILING_LINE_WHITESPACE.matcher(result).replaceAll("\n");
        result = HORIZONTAL_WHITESPACE_RUN.matcher(result).replaceAll(" ");
        result = EXCESS_BLANK_LINES.matcher(result).replaceAll("\n\n");

        return result.strip();
    }

    private String firstNonNullGroup(MatchResult match) {
        for (int i = 1; i <= match.groupCount(); i++) {
            if (match.group(i) != null) {
                return Matcher.quoteReplacement(match.group(i));
            }
        }
        return match.group();
    }
}
