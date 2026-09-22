package com.catail.backend.signal.outbound.openai;

import tools.jackson.databind.JsonNode;
/**
 * <h3>OpenAI JSON Schema 설정 DTO</h3>
 *
 * <p>구조화된 응답에 적용할 JSON Schema의 식별 정보와 실제 데이터 구조를 정의한다.</p>
 *
 * @param name   JSON Schema 식별 이름
 * @param strict 모델이 지정된 스키마를 엄격하게 준수하도록 요구할지 여부
 * @param schema 모델 응답의 구조를 정의한 JSON Schema
 *
 * @see <a href="https://developers.openai.com/api/docs/guides/structured-outputs">
 * OpenAI Structured Outputs Guide</a>
 */
public record OpenAiJsonSchema(String name, boolean strict, JsonNode schema) {
}
