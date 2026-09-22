package com.catail.backend.signal.outbound.openai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
/**
 * <h3>OpenAI 채팅 API 요청 DTO</h3>
 *
 * <p>OpenAI 채팅 API에 전송할 최상위 요청 본문의 구조를 정의한다.</p>
 *
 * @param model          응답 생성에 사용할 OpenAI 모델
 * @param messages       모델에 전달할 대화 메시지 목록
 * @param responseFormat 모델이 반환해야 할 응답 형식
 */
public record OpenAiChatRequest(
        String model,
        List<OpenAiMessage> messages,
        @JsonProperty("response_format") OpenAiResponseFormat responseFormat
) {
}
