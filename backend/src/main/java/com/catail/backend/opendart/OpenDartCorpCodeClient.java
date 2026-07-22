package com.catail.backend.opendart;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class OpenDartCorpCodeClient {

    private static final String CORP_CODE_ENTRY_NAME = "CORPCODE.xml";

    private final RestClient openDartApiRestClient;
    private final String apiKey;

    public OpenDartCorpCodeClient(
            @Qualifier("openDartApiRestClient") RestClient openDartApiRestClient,
            @Value("${opendart.api.key}") String apiKey
    ) {
        this.openDartApiRestClient = openDartApiRestClient;
        this.apiKey = apiKey;
    }

    public List<CorpCodeItem> fetchAll() {
        byte[] zipBytes = requestCorpCodeZip();
        Document document = parseCorpCodeXml(extractCorpCodeXml(zipBytes));
        return parseItems(document);
    }

    private byte[] requestCorpCodeZip() {
        try {
            return openDartApiRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/corpCode.xml")
                            .queryParam("crtfc_key", apiKey)
                            .build())
                    .retrieve()
                    .body(byte[].class);
        } catch (RestClientException e) {
            throw new OpenDartApiException("OpenDART 고유번호 API 호출에 실패했습니다.", e);
        }
    }

    private byte[] extractCorpCodeXml(byte[] zipBytes) {
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (CORP_CODE_ENTRY_NAME.equals(entry.getName())) {
                    return zipInputStream.readAllBytes();
                }
            }
            throw new OpenDartApiException("OpenDART 고유번호 ZIP에서 CORPCODE.xml을 찾을 수 없습니다.");
        } catch (IOException e) {
            throw new OpenDartApiException("OpenDART 고유번호 ZIP 압축 해제에 실패했습니다.", e);
        }
    }

    private Document parseCorpCodeXml(byte[] xmlBytes) {
        try (InputStream inputStream = new ByteArrayInputStream(xmlBytes)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(inputStream);
        } catch (Exception e) {
            throw new OpenDartApiException("OpenDART 고유번호 XML 파싱에 실패했습니다.", e);
        }
    }

    private List<CorpCodeItem> parseItems(Document document) {
        List<CorpCodeItem> items = new ArrayList<>();
        NodeList listNodes = document.getElementsByTagName("list");
        for (int i = 0; i < listNodes.getLength(); i++) {
            Element listElement = (Element) listNodes.item(i);
            String corpCode = textOf(listElement, "corp_code");
            String stockCode = textOf(listElement, "stock_code");
            if (StringUtils.hasText(stockCode)) {
                items.add(new CorpCodeItem(corpCode, stockCode.trim()));
            }
        }
        return items;
    }

    private String textOf(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return null;
        }
        return nodes.item(0).getTextContent();
    }
}
