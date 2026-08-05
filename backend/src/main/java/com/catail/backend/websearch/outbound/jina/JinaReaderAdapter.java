package com.catail.backend.websearch.outbound.jina;

import com.catail.backend.websearch.domain.CrawlFailureCode;
import com.catail.backend.websearch.outbound.JinaReadException;
import com.catail.backend.websearch.outbound.JinaReadResult;
import com.catail.backend.websearch.outbound.JinaReaderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

@Slf4j
@Component
public class JinaReaderAdapter implements JinaReaderPort {

    private final RestClient jinaApiRestClient;
    private final String baseUrl;

    public JinaReaderAdapter(
            @Qualifier("jinaApiRestClient") RestClient jinaApiRestClient,
            @Value("${jina.api.base-url}") String baseUrl
    ) {
        this.jinaApiRestClient = jinaApiRestClient;
        this.baseUrl = baseUrl;
    }

    @Override
    public JinaReadResult read(String crawlUrl) {
        // crawlUrl 자체가 "://"와 쿼리스트링을 포함한 완전한 URL이므로,
        // RestClient의 uri("/{x}", crawlUrl) 템플릿 치환(퍼센트 인코딩)을 피하고
        // URI.create로 baseUrl과 그대로 이어붙여 전달한다.
        URI targetUri;
        try {
            targetUri = URI.create(baseUrl + "/" + crawlUrl);
        } catch (IllegalArgumentException e) {
            throw new JinaReadException(CrawlFailureCode.UNKNOWN_ERROR, false, "유효하지 않은 crawlUrl 형식입니다: " + crawlUrl, e);
        }

        try {
            JsonNode body = jinaApiRestClient.get()
                    .uri(targetUri)
                    .retrieve()
                    .body(JsonNode.class);

            if (body == null || !body.path("data").isObject()) {
                throw new JinaReadException(CrawlFailureCode.INVALID_RESPONSE, false, "Jina Reader 응답에 data가 없습니다.");
            }
            return parseData(body.path("data"));
        } catch (HttpClientErrorException e) {
            int status = e.getStatusCode().value();
            if (status == 429) {
                throw new JinaReadException(CrawlFailureCode.RATE_LIMITED, true, "Jina Reader 요청 한도 초과 (status=429)", e);
            }
            throw new JinaReadException(CrawlFailureCode.CLIENT_ERROR, false, "Jina Reader 클라이언트 오류 (status=" + status + ")", e);
        } catch (HttpServerErrorException e) {
            throw new JinaReadException(CrawlFailureCode.JINA_SERVER_ERROR, true, "Jina Reader 서버 오류 (status=" + e.getStatusCode().value() + ")", e);
        } catch (ResourceAccessException e) {
            CrawlFailureCode code = isTimeout(e) ? CrawlFailureCode.TIMEOUT : CrawlFailureCode.NETWORK_ERROR;
            throw new JinaReadException(code, true, "Jina Reader 네트워크 오류", e);
        } catch (JinaReadException e) {
            throw e;
        } catch (RestClientException e) {
            throw new JinaReadException(CrawlFailureCode.UNKNOWN_ERROR, false, "Jina Reader 요청에 실패했습니다.", e);
        }
    }

    private boolean isTimeout(ResourceAccessException e) {
        return e.getCause() instanceof java.net.http.HttpTimeoutException
                || e.getCause() instanceof java.net.SocketTimeoutException;
    }

    private JinaReadResult parseData(JsonNode data) {
        String warning = blankToNull(data.path("warning").asString(""));
        String title = data.path("title").asString("");
        String url = data.path("url").asString("");
        String content = data.path("content").asString("");
        LocalDateTime publishedTime = parsePublishedTime(data.path("publishedTime").asString(null));
        Integer tokens = data.path("usage").path("tokens").isNumber()
                ? data.path("usage").path("tokens").asInt()
                : null;

        return new JinaReadResult(warning, title, url, content, publishedTime, tokens);
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private LocalDateTime parsePublishedTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (DateTimeParseException e) {
            log.warn("Jina publishedTime 파싱 실패: {}", value);
            return null;
        }
    }
}
