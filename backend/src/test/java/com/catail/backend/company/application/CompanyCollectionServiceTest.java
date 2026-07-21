package com.catail.backend.company.application;

import com.catail.backend.company.db.Company;
import com.catail.backend.company.db.CompanyRepository;
import com.catail.backend.company.domain.Market;
import com.catail.backend.company.outbound.ListedCompanyCollectionException;
import com.catail.backend.company.outbound.ListedCompanyCollectionPort;
import com.catail.backend.company.outbound.ListedCompanyItem;
import com.catail.backend.company.outbound.ListedCompanyPage;
import com.catail.backend.global.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CompanyCollectionServiceTest {

    @Mock
    private ListedCompanyCollectionPort krxPort;

    @Mock
    private CompanyRepository companyRepository;

    private CompanyCollectionService companyCollectionService;

    @BeforeEach
    void setUp() {
        companyCollectionService = new CompanyCollectionService(List.of(krxPort), companyRepository);
    }

    @Nested
    @DisplayName("collect")
    class Collect {

        @Test
        @DisplayName("오늘 데이터가 있으면 오늘 날짜로 수집하고 신규 기업을 저장한다")
        void collect_todayHasData_savesNewCompany() {
            given(krxPort.market()).willReturn(Market.KOSPI);
            LocalDate today = LocalDate.now();
            ListedCompanyItem item = new ListedCompanyItem(Market.KOSPI, "005930", "삼성전자");
            given(krxPort.fetchPage(eq(today), eq(1), eq(1)))
                    .willReturn(new ListedCompanyPage(1, List.of(item)));
            given(krxPort.fetchPage(eq(today), eq(1), eq(1000)))
                    .willReturn(new ListedCompanyPage(1, List.of(item)));
            given(companyRepository.findByMarketAndStockCode(Market.KOSPI, "005930"))
                    .willReturn(Optional.empty());

            companyCollectionService.collect(Market.KOSPI);

            verify(companyRepository).save(any(Company.class));
        }

        @Test
        @DisplayName("오늘 데이터가 없으면 이전 날짜로 거슬러 올라가 데이터가 있는 날짜를 찾는다")
        void collect_todayHasNoData_looksBackToPreviousDay() {
            given(krxPort.market()).willReturn(Market.KOSPI);
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);
            ListedCompanyItem item = new ListedCompanyItem(Market.KOSPI, "005930", "삼성전자");

            given(krxPort.fetchPage(eq(today), eq(1), eq(1)))
                    .willReturn(new ListedCompanyPage(0, List.of()));
            given(krxPort.fetchPage(eq(yesterday), eq(1), eq(1)))
                    .willReturn(new ListedCompanyPage(1, List.of(item)));
            given(krxPort.fetchPage(eq(yesterday), eq(1), eq(1000)))
                    .willReturn(new ListedCompanyPage(1, List.of(item)));
            given(companyRepository.findByMarketAndStockCode(Market.KOSPI, "005930"))
                    .willReturn(Optional.empty());

            companyCollectionService.collect(Market.KOSPI);

            verify(krxPort).fetchPage(eq(yesterday), eq(1), eq(1000));
        }

        @Test
        @DisplayName("최대 lookback 기간 내 데이터가 없으면 COLLECTION_DATA_NOT_FOUND 예외가 발생한다")
        void collect_noDataWithinLookback_throwsDataNotFound() {
            given(krxPort.market()).willReturn(Market.KOSPI);
            given(krxPort.fetchPage(any(LocalDate.class), eq(1), eq(1)))
                    .willReturn(new ListedCompanyPage(0, List.of()));

            assertThatThrownBy(() -> companyCollectionService.collect(Market.KOSPI))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(CompanyErrorCode.COLLECTION_DATA_NOT_FOUND);

            verify(companyRepository, never()).save(any());
        }

        @Test
        @DisplayName("이미 존재하는 기업이면 companyName을 갱신해 저장한다")
        void collect_existingCompany_updatesCompanyName() {
            given(krxPort.market()).willReturn(Market.KOSPI);
            LocalDate today = LocalDate.now();
            ListedCompanyItem item = new ListedCompanyItem(Market.KOSPI, "005930", "삼성전자(신)");
            given(krxPort.fetchPage(eq(today), eq(1), eq(1)))
                    .willReturn(new ListedCompanyPage(1, List.of(item)));
            given(krxPort.fetchPage(eq(today), eq(1), eq(1000)))
                    .willReturn(new ListedCompanyPage(1, List.of(item)));
            Company existing = Company.create(Market.KOSPI, "005930", "삼성전자");
            given(companyRepository.findByMarketAndStockCode(Market.KOSPI, "005930"))
                    .willReturn(Optional.of(existing));

            companyCollectionService.collect(Market.KOSPI);

            assertThat(existing.getCompanyName()).isEqualTo("삼성전자(신)");
            verify(companyRepository).save(existing);
        }

        @Test
        @DisplayName("totalCount가 페이지 크기를 초과하면 다음 페이지까지 순회해 수집한다")
        void collect_multiplePages_fetchesAllPages() {
            given(krxPort.market()).willReturn(Market.KOSPI);
            LocalDate today = LocalDate.now();
            ListedCompanyItem item1 = new ListedCompanyItem(Market.KOSPI, "005930", "삼성전자");
            ListedCompanyItem item2 = new ListedCompanyItem(Market.KOSPI, "000660", "SK하이닉스");

            given(krxPort.fetchPage(eq(today), eq(1), eq(1)))
                    .willReturn(new ListedCompanyPage(1001, List.of(item1)));
            given(krxPort.fetchPage(eq(today), eq(1), eq(1000)))
                    .willReturn(new ListedCompanyPage(1001, List.of(item1)));
            given(krxPort.fetchPage(eq(today), eq(2), eq(1000)))
                    .willReturn(new ListedCompanyPage(1001, List.of(item2)));
            given(companyRepository.findByMarketAndStockCode(any(), any()))
                    .willReturn(Optional.empty());

            companyCollectionService.collect(Market.KOSPI);

            verify(krxPort).fetchPage(eq(today), eq(2), eq(1000));
            verify(companyRepository, times(2)).save(any(Company.class));
        }

        @Test
        @DisplayName("담당 Port가 없는 시장을 요청하면 IllegalStateException이 발생한다")
        void collect_unsupportedMarketPort_throwsIllegalState() {
            given(krxPort.market()).willReturn(Market.KOSPI);

            assertThatThrownBy(() -> companyCollectionService.collect(Market.NASDAQ))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("외부 API 호출이 실패하면 COLLECTION_SOURCE_ERROR 예외로 변환된다")
        void collect_portThrowsCollectionException_wrapsAsCollectionSourceError() {
            given(krxPort.market()).willReturn(Market.KOSPI);
            given(krxPort.fetchPage(any(LocalDate.class), eq(1), eq(1)))
                    .willThrow(new ListedCompanyCollectionException("KRX API 호출 실패"));

            assertThatThrownBy(() -> companyCollectionService.collect(Market.KOSPI))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(CompanyErrorCode.COLLECTION_SOURCE_ERROR);
        }
    }
}
