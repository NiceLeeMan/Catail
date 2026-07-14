package com.catail.backend.catalyst.db;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class IndustrySeeder implements CommandLineRunner {

    private static final List<String> DEFAULT_INDUSTRY_NAMES = List.of(
            "반도체", "AI 데이터센터", "전력", "해운", "방산", "2차전지"
    );

    private final IndustryRepository industryRepository;

    @Override
    public void run(String... args) {
        if (industryRepository.count() > 0) {
            return;
        }
        DEFAULT_INDUSTRY_NAMES.stream()
                .map(Industry::create)
                .forEach(industryRepository::save);
    }
}
