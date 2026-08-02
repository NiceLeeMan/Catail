package com.catail.backend.searchplan.outbound.openai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

record OpenAiChatRequest(
        String model,
        List<OpenAiMessage> messages,
        @JsonProperty("response_format") OpenAiResponseFormat responseFormat
) {
}
