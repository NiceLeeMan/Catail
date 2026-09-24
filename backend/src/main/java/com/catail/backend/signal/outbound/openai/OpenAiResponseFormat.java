package com.catail.backend.signal.outbound.openai;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * <h3>OpenAI 응답 형식 DTO</h3>
 *
 * <p>Chat Completions API가 생성할 응답 형식을 지정한다.</p>
 * <p>{@code type}을 {@code json_schema}로 설정하면 모델에 구조화된 JSON 응답을 요청한다.</p>
 *
 * @param type       응답 형식 유형
 * @param jsonSchema 응답이 따라야 할 JSON Schema 설정
 *
 * @see <a href="https://developers.openai.com/api/docs/guides/structured-outputs">
 * OpenAI Structured Outputs Guide</a>
 */
public record OpenAiResponseFormat(
        String type,
        @JsonProperty("json_schema") OpenAiJsonSchema jsonSchema
) {
}
