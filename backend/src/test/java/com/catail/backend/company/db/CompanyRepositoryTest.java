package com.catail.backend.company.db;

import com.catail.backend.company.domain.Market;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class CompanyRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    @DisplayName("companyName 키워드로 검색하면 부분일치하는 기업만 반환한다")
    void searchByKeyword_byCompanyNameKeyword_matchesPartialName() {
        companyRepository.save(Company.create(Market.KOSPI, "005930", "삼성전자"));
        companyRepository.save(Company.create(Market.KOSPI, "000660", "SK하이닉스"));

        Page<Company> result = companyRepository.searchByKeyword(Market.KOSPI, "삼성", PageRequest.of(0, 50));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStockCode()).isEqualTo("005930");
    }

    @Test
    @DisplayName("stockCode 키워드로 검색하면 부분일치하는 기업만 반환한다")
    void searchByKeyword_byStockCodeKeyword_matchesPartialCode() {
        companyRepository.save(Company.create(Market.KOSPI, "005930", "삼성전자"));
        companyRepository.save(Company.create(Market.KOSPI, "000660", "SK하이닉스"));

        Page<Company> result = companyRepository.searchByKeyword(Market.KOSPI, "0059", PageRequest.of(0, 50));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCompanyName()).isEqualTo("삼성전자");
    }

    @Test
    @DisplayName("findByMarket은 해당 시장의 전체 기업을 반환한다")
    void findByMarket_returnsAllInMarket() {
        companyRepository.save(Company.create(Market.KOSPI, "005930", "삼성전자"));
        companyRepository.save(Company.create(Market.KOSPI, "000660", "SK하이닉스"));

        Page<Company> result = companyRepository.findByMarket(Market.KOSPI, PageRequest.of(0, 50));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("findByMarket은 다른 market의 기업을 포함하지 않는다")
    void findByMarket_differentMarket_excludesOtherMarketCompanies() {
        companyRepository.save(Company.create(Market.KOSPI, "005930", "삼성전자"));
        companyRepository.save(Company.create(Market.NASDAQ, "AAPL", "Apple"));

        Page<Company> result = companyRepository.findByMarket(Market.KOSPI, PageRequest.of(0, 50));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getMarket()).isEqualTo(Market.KOSPI);
    }

    @Test
    @DisplayName("market과 stockCode가 일치하는 기업을 조회한다")
    void findByMarketAndStockCode_existing_returnsCompany() {
        companyRepository.save(Company.create(Market.KOSPI, "005930", "삼성전자"));

        assertThat(companyRepository.findByMarketAndStockCode(Market.KOSPI, "005930")).isPresent();
    }

    @Test
    @DisplayName("일치하는 기업이 없으면 빈 결과를 반환한다")
    void findByMarketAndStockCode_notFound_returnsEmpty() {
        assertThat(companyRepository.findByMarketAndStockCode(Market.KOSPI, "999999")).isEmpty();
    }
}
