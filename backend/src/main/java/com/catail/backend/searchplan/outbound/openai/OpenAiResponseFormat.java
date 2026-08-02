package com.catail.backend.searchplan.outbound.openai;

import com.fasterxml.jackson.annotation.JsonProperty;

record OpenAiResponseFormat(
        String type,
        @JsonProperty("json_schema") OpenAiJsonSchema jsonSchema
) {
}
