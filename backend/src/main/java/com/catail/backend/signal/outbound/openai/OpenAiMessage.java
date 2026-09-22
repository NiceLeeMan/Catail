package com.catail.backend.signal.outbound.openai;

/**
 * <h3>OpenAI 메시지 DTO</h3>
 *
 * <p>Chat Completions API에 전달할 개별 메시지의 역할과 내용을 표현한다.</p>
 * <p>메시지 역할에 따라 모델이 지침과 사용자 입력을 구분하고 우선순위를 적용한다.</p>
 *
 * @param role    메시지 역할({@code developer}, {@code user}, {@code assistant} 등)
 * @param content 모델에 전달할 메시지 내용
 *
 * @see <a href="https://developers.openai.com/api/reference/resources/chat">
 * OpenAI Chat Completions API Reference</a>
 */
public record OpenAiMessage(String role, String content) {
}
