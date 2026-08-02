package com.catail.backend.searchplan.outbound.openai;

import com.catail.backend.global.BusinessException;
import com.catail.backend.searchplan.application.SearchPlanErrorCode;
import com.catail.backend.searchplan.domain.Criterion;
import com.catail.backend.searchplan.domain.TargetCompanyInfo;
import com.catail.backend.searchplan.outbound.CriterionLlmItem;
import com.catail.backend.searchplan.outbound.SearchPlanLlmPort;
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
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OpenAiSearchPlanAdapter implements SearchPlanLlmPort {

    private static final String RESPONSE_SCHEMA_JSON = """
            {
              "type": "object",
              "properties": {
                "SUPPLIER": { "$ref": "#/$defs/criterionItem" },
                "CUSTOMER": { "$ref": "#/$defs/criterionItem" },
                "COMPETITION": { "$ref": "#/$defs/criterionItem" },
                "PARTNER": { "$ref": "#/$defs/criterionItem" },
                "GATE_KEEPER": { "$ref": "#/$defs/criterionItem" },
                "PUBLIC_AUTHORITY": { "$ref": "#/$defs/criterionItem" },
                "CRITICAL_RESOURCE_HOLDER": { "$ref": "#/$defs/criterionItem" }
              },
              "required": [
                "SUPPLIER", "CUSTOMER", "COMPETITION", "PARTNER",
                "GATE_KEEPER", "PUBLIC_AUTHORITY", "CRITICAL_RESOURCE_HOLDER"
              ],
              "additionalProperties": false,
              "$defs": {
                "criterionItem": {
                  "type": "object",
                  "properties": {
                    "scopeRelevance": { "type": "string" },
                    "queries": { "type": "array", "items": { "type": "string" } }
                  },
                  "required": ["scopeRelevance", "queries"],
                  "additionalProperties": false
                }
              }
            }
            """;

    private final RestClient openAiApiRestClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final String systemPrompt;
    private final JsonNode responseSchema;

    public OpenAiSearchPlanAdapter(
            @Qualifier("openAiApiRestClient") RestClient openAiApiRestClient,
            ObjectMapper objectMapper,
            @Value("${openai.api.model}") String model,
            @Value("classpath:prompts/search-plan-system-prompt.txt") Resource systemPromptResource
    ) {
        this.openAiApiRestClient = openAiApiRestClient;
        this.objectMapper = objectMapper;
        this.model = model;
        this.systemPrompt = readResource(systemPromptResource);
        this.responseSchema = objectMapper.readTree(RESPONSE_SCHEMA_JSON);
    }

    @Override
    public Map<Criterion, CriterionLlmItem> generate(TargetCompanyInfo targetCompany, String analysisScope) {
        OpenAiChatRequest request = new OpenAiChatRequest(
                model,
                List.of(
                        new OpenAiMessage("system", systemPrompt),
                        new OpenAiMessage("user", buildUserMessage(targetCompany, analysisScope))
                ),
                new OpenAiResponseFormat(
                        "json_schema",
                        new OpenAiJsonSchema("search_plan_criteria", true, responseSchema)
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
            throw new BusinessException(SearchPlanErrorCode.LLM_CALL_FAILED);
        }

        String content = response.path("choices").path(0).path("message").path("content").asString("");
        log.info("탐색 계획 LLM 응답 (companyId={}, analysisScope={}): {}",
                targetCompany.companyId(), analysisScope, content);
        if (content.isBlank()) {
            throw new BusinessException(SearchPlanErrorCode.LLM_INVALID_RESPONSE);
        }

        return parseContent(content);
    }

    private String buildUserMessage(TargetCompanyInfo targetCompany, String analysisScope) {
        Map<String, Object> company = new LinkedHashMap<>();
        company.put("companyId", targetCompany.companyId());
        company.put("companyName", targetCompany.companyName());
        company.put("englishName", targetCompany.englishName());
        company.put("stockCode", targetCompany.stockCode());
        company.put("market", targetCompany.market().name());
        company.put("countryCode", targetCompany.countryCode());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("targetCompany", company);
        payload.put("analysisScope", analysisScope);

        return objectMapper.writeValueAsString(payload);
    }

    private Map<Criterion, CriterionLlmItem> parseContent(String content) {
        JsonNode root;
        try {
            root = objectMapper.readTree(content);
        } catch (RuntimeException e) {
            log.error("LLM 응답 파싱 실패: content={}", content, e);
            throw new BusinessException(SearchPlanErrorCode.LLM_INVALID_RESPONSE);
        }

        Map<Criterion, CriterionLlmItem> result = new EnumMap<>(Criterion.class);
        for (Criterion criterion : Criterion.values()) {
            JsonNode node = root.path(criterion.name());
            if (node.isMissingNode() || node.isNull()) {
                continue;
            }
            String scopeRelevance = node.path("scopeRelevance").asString("");
            List<String> queries = new ArrayList<>();
            for (JsonNode queryNode : node.path("queries")) {
                queries.add(queryNode.asString(""));
            }
            result.put(criterion, new CriterionLlmItem(scopeRelevance, queries));
        }
        return result;
    }

    private static String readResource(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("탐색 계획 시스템 프롬프트 리소스를 읽을 수 없습니다.", e);
        }
    }
}
