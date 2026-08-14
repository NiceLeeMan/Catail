package com.catail.backend.signal.outbound.openai;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenAiResponseFormat(
        String type,
        @JsonProperty("json_schema") OpenAiJsonSchema jsonSchema
) {
}
