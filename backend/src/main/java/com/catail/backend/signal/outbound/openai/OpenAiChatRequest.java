package com.catail.backend.signal.outbound.openai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenAiChatRequest(
        String model,
        List<OpenAiMessage> messages,
        @JsonProperty("response_format") OpenAiResponseFormat responseFormat
) {
}
