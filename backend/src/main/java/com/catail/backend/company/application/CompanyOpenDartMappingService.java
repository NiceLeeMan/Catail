package com.catail.backend.company.application;

import com.catail.backend.company.db.Company;
import com.catail.backend.company.db.CompanyRepository;
import com.catail.backend.company.domain.Market;
import com.catail.backend.opendart.CorpCodeItem;
import com.catail.backend.opendart.OpenDartCorpCodeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyOpenDartMappingService {

    private final OpenDartCorpCodeClient openDartCorpCodeClient;
    private final CompanyRepository companyRepository;

    public void mapKospiCorpCodes() {
        List<Company> unmapped = companyRepository.findByMarketAndOpenDartCorpCodeIsNull(Market.KOSPI);
        if (unmapped.isEmpty()) {
            return;
        }

        Map<String, String> corpCodeByStockCode = openDartCorpCodeClient.fetchAll().stream()
                .collect(Collectors.toMap(
                        CorpCodeItem::stockCode, CorpCodeItem::corpCode, (first, second) -> first));

        int mappedCount = 0;
        for (Company company : unmapped) {
            String corpCode = corpCodeByStockCode.get(company.getStockCode());
            if (corpCode != null) {
                company.assignOpenDartCorpCode(corpCode);
                companyRepository.save(company);
                mappedCount++;
            }
        }
        log.info("OpenDART 고유번호 매핑 완료: 대상={}, 매핑성공={}", unmapped.size(), mappedCount);
    }
}
