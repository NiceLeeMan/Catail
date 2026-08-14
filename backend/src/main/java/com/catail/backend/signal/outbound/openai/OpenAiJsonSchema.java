package com.catail.backend.signal.outbound.openai;

import tools.jackson.databind.JsonNode;

public record OpenAiJsonSchema(String name, boolean strict, JsonNode schema) {
}
