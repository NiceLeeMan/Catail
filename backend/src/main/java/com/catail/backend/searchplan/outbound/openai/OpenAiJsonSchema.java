package com.catail.backend.searchplan.outbound.openai;

import tools.jackson.databind.JsonNode;

record OpenAiJsonSchema(String name, boolean strict, JsonNode schema) {
}
