package com.catail.backend.user.application;

import java.util.concurrent.ThreadLocalRandom;

public final class NicknameGenerator {

    private static final String[] ADJECTIVES = {
            "Happy", "Brave", "Calm", "Bright", "Clever",
            "Swift", "Kind", "Lucky", "Gentle", "Sunny"
    };

    private static final String[] NOUNS = {
            "Cat", "Tiger", "Panda", "Fox", "Lion",
            "Bear", "Rabbit", "Wolf", "Koala", "Otter"
    };

    private NicknameGenerator() {
    }

    public static String generate() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[random.nextInt(NOUNS.length)];
        int number = random.nextInt(1000, 10000);

        return adjective + noun + number;
    }
}