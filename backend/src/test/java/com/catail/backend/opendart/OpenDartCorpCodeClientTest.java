package com.catail.backend.opendart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenDartCorpCodeClientTest {

    private static final String BASE_URL = "https://opendart.fss.or.kr/api";

    private MockRestServiceServer mockServer;
    private OpenDartCorpCodeClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        client = new OpenDartCorpCodeClient(restClient, "test-api-key");
    }

    private byte[] zipOf(String xml) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            zipOutputStream.putNextEntry(new ZipEntry("CORPCODE.xml"));
            zipOutputStream.write(xml.getBytes(StandardCharsets.UTF_8));
            zipOutputStream.closeEntry();
        }
        return byteArrayOutputStream.toByteArray();
    }

    @Test
    @DisplayName("ZIP 안의 CORPCODE.xml을 파싱해 stock_code가 있는 항목만 반환한다")
    void fetchAll_parsesZipAndFiltersEmptyStockCode() throws IOException {
        String xml = """
                <result>
                    <list>
                        <corp_code>00126380</corp_code>
                        <corp_name>삼성전자</corp_name>
                        <stock_code>005930</stock_code>
                        <modify_date>20240101</modify_date>
                    </list>
                    <list>
                        <corp_code>00999999</corp_code>
                        <corp_name>비상장기업</corp_name>
                        <stock_code></stock_code>
                        <modify_date>20240101</modify_date>
                    </list>
                </result>
                """;
        mockServer.expect(method(GET)).andRespond(
                withSuccess(zipOf(xml), MediaType.APPLICATION_OCTET_STREAM));

        List<CorpCodeItem> items = client.fetchAll();

        assertThat(items).containsExactly(new CorpCodeItem("00126380", "005930"));
    }

    @Test
    @DisplayName("ZIP에 CORPCODE.xml이 없으면 예외가 발생한다")
    void fetchAll_missingEntry_throwsException() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            zipOutputStream.putNextEntry(new ZipEntry("OTHER.xml"));
            zipOutputStream.write("<result/>".getBytes(StandardCharsets.UTF_8));
            zipOutputStream.closeEntry();
        }
        mockServer.expect(method(GET)).andRespond(
                withSuccess(byteArrayOutputStream.toByteArray(), MediaType.APPLICATION_OCTET_STREAM));

        assertThatThrownBy(() -> client.fetchAll()).isInstanceOf(OpenDartApiException.class);
    }
}
