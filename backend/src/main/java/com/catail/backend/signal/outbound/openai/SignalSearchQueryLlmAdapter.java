package com.catail.backend.signal.outbound.openai;

import com.catail.backend.global.BusinessException;
import com.catail.backend.signal.application.SignalErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class SignalSearchQueryLlmAdapter {

    private static final String RESPONSE_SCHEMA_JSON = """
            {
              "type": "object",
              "properties": {
                "queries": { "type": "array", "items": { "type": "string" } }
              },
              "required": ["queries"],
              "additionalProperties": false
            }
            """;

    private final RestClient openAiApiRestClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final String systemPrompt;
    private final JsonNode responseSchema;

    public SignalSearchQueryLlmAdapter(
            @Qualifier("openAiApiRestClient") RestClient openAiApiRestClient,
            ObjectMapper objectMapper,
            @Value("${openai.api.model}") String model,
            @Value("classpath:prompts/signal-search-query-system-prompt.txt") Resource systemPromptResource
    ) {
        this.openAiApiRestClient = openAiApiRestClient;
        this.objectMapper = objectMapper;
        this.model = model;
        this.systemPrompt = readResource(systemPromptResource);
        this.responseSchema = objectMapper.readTree(RESPONSE_SCHEMA_JSON);
    }

    public List<String> generate(String companyName, String category, String detail) {
        OpenAiChatRequest request = new OpenAiChatRequest(
                model,
                List.of(
                        new OpenAiMessage("system", systemPrompt),
                        new OpenAiMessage("user", buildUserMessage(companyName, category, detail))
                ),
                new OpenAiResponseFormat(
                        "json_schema",
                        new OpenAiJsonSchema("signal_search_queries", true, responseSchema)
                )
        );

        JsonNode response;
        try {
            response = openAiApiRestClient.post()
                    .uri("/chat/completions")
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            log.error("OpenAI 호출 실패", e);
            throw new BusinessException(SignalErrorCode.LLM_CALL_FAILED);
        }

        String content = response.path("choices").path(0).path("message").path("content").asString("");
        if (content.isBlank()) {
            throw new BusinessException(SignalErrorCode.LLM_INVALID_RESPONSE);
        }

        return parseQueries(content);
    }

    private String buildUserMessage(String companyName, String category, String detail) {
        JsonNode payload = objectMapper.createObjectNode()
                .put("companyName", companyName)
                .put("category", category)
                .put("detail", detail);
        return objectMapper.writeValueAsString(payload);
    }

    private List<String> parseQueries(String content) {
        JsonNode root;
        try {
            root = objectMapper.readTree(content);
        } catch (RuntimeException e) {
            log.error("LLM 응답 파싱 실패: content={}", content, e);
            throw new BusinessException(SignalErrorCode.LLM_INVALID_RESPONSE);
        }

        List<String> queries = new ArrayList<>();
        for (JsonNode queryNode : root.path("queries")) {
            queries.add(queryNode.asString(""));
        }
        return queries;
    }

    private static String readResource(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("시그널 검색어 시스템 프롬프트 리소스를 읽을 수 없습니다.", e);
        }
    }
}
