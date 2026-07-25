package com.catail.backend.company.outbound.nasdaq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class NasdaqListedFileParser {

    private static final String FIELD_DELIMITER = "\\|";
    private static final String FOOTER_PREFIX = "File Creation Time";

    private NasdaqListedFileParser() {
    }

    static List<NasdaqListedRecord> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }

        List<String> lines = raw.lines().filter(line -> !line.isBlank()).toList();
        if (lines.isEmpty()) {
            return List.of();
        }

        Map<String, Integer> columnIndex = indexHeader(lines.get(0));
        List<NasdaqListedRecord> records = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith(FOOTER_PREFIX)) {
                continue;
            }
            records.add(toRecord(line.split(FIELD_DELIMITER, -1), columnIndex));
        }
        return records;
    }

    private static Map<String, Integer> indexHeader(String headerLine) {
        String[] columns = headerLine.split(FIELD_DELIMITER, -1);
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < columns.length; i++) {
            index.put(columns[i].trim(), i);
        }
        return index;
    }

    private static NasdaqListedRecord toRecord(String[] fields, Map<String, Integer> columnIndex) {
        return new NasdaqListedRecord(
                field(fields, columnIndex, "Symbol"),
                field(fields, columnIndex, "Security Name"),
                field(fields, columnIndex, "Market Category"),
                field(fields, columnIndex, "Test Issue"),
                field(fields, columnIndex, "ETF"),
                field(fields, columnIndex, "NextShares")
        );
    }

    private static String field(String[] fields, Map<String, Integer> columnIndex, String columnName) {
        Integer index = columnIndex.get(columnName);
        if (index == null || index >= fields.length) {
            return null;
        }
        return fields[index].trim();
    }
}
