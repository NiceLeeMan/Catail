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

/**
 * <h3>OpenAI 기반 시그널 검색어 생성 어댑터</h3>
 *
 * <p>기업명, 카탈리스트 카테고리 및 상세 내용을 OpenAI Chat Completions API에 전달하고,
 * 구조화된 응답을 시그널 검색어 목록으로 변환한다.</p>
 *
 * <p>OpenAI 요청 형식 구성, 외부 API 호출, 응답 검증·파싱 및
 * 외부 통신 예외를 애플리케이션 예외로 변환하는 책임을 가진다.</p>
 *
 * @see <a href="https://developers.openai.com/api/reference/resources/chat">
 * OpenAI Chat Completions API Reference</a>
 * @see <a href="https://developers.openai.com/api/docs/guides/structured-outputs">
 * OpenAI Structured Outputs Guide</a>
 */
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

    /**
     * 카탈리스트 정보를 기반으로 뉴스 검색에 사용할 검색어를 생성한다.
     *
     * <p>OpenAI API에 구조화된 응답을 요청하고, 반환된 JSON에서 검색어 목록을 추출한다.</p>
     *
     * @param companyName 검색 대상 기업명
     * @param category    카탈리스트 카테고리
     * @param detail      사용자가 입력한 카탈리스트 상세 내용
     * @return OpenAI가 생성한 검색어 목록
     * @throws BusinessException OpenAI 호출에 실패하거나 응답 형식이 유효하지 않은 경우
     */
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
    /**
     * 사용자 입력을 OpenAI 사용자 메시지에 포함할 JSON 문자열로 직렬화한다.
     */
    private String buildUserMessage(String companyName, String category, String detail) {
        JsonNode payload = objectMapper.createObjectNode()
                .put("companyName", companyName)
                .put("category", category)
                .put("detail", detail);
        return objectMapper.writeValueAsString(payload);
    }
    /**
     * OpenAI 메시지의 content에 포함된 JSON 문자열에서 검색어 목록을 추출한다.
     *
     * @throws BusinessException content가 유효한 JSON이 아닌 경우
     */
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
    /**
     * 시스템 프롬프트 리소스를 애플리케이션 시작 시 읽는다.
     *
     * @throws UncheckedIOException 리소스를 읽을 수 없는 경우
     */
    private static String readResource(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("시그널 검색어 시스템 프롬프트 리소스를 읽을 수 없습니다.", e);
        }
    }
}
