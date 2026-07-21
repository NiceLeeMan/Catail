package com.catail.backend.company.application;

import com.catail.backend.company.db.Company;
import com.catail.backend.company.db.CompanyRepository;
import com.catail.backend.company.domain.Market;
import com.catail.backend.company.inbound.read.CompanyListResponse;
import com.catail.backend.global.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CompanyListServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyListService companyListService;

    @Nested
    @DisplayName("getList")
    class GetList {

        @Test
        @DisplayName("market=KOSPI이면 정상적으로 페이지를 조회한다")
        void getList_kospi_returnsPage() {
            Company company = Company.create(Market.KOSPI, "005930", "삼성전자");
            Page<Company> page = new PageImpl<>(List.of(company), PageRequest.of(0, 50), 1);
            given(companyRepository.findByMarket(eq(Market.KOSPI), any(Pageable.class))).willReturn(page);

            CompanyListResponse response = companyListService.getList("KOSPI", null, 0);

            assertThat(response.items()).hasSize(1);
            assertThat(response.items().get(0).stockCode()).isEqualTo("005930");
            assertThat(response.totalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("지원하지 않는 시장(NASDAQ)이면 UNSUPPORTED_MARKET 예외가 발생한다")
        void getList_nasdaq_throwsUnsupportedMarket() {
            assertThatThrownBy(() -> companyListService.getList("NASDAQ", null, 0))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(CompanyErrorCode.UNSUPPORTED_MARKET);

            verifyNoInteractions(companyRepository);
        }

        @Test
        @DisplayName("존재하지 않는 시장 문자열이면 UNSUPPORTED_MARKET 예외가 발생한다")
        void getList_unknownMarketString_throwsUnsupportedMarket() {
            assertThatThrownBy(() -> companyListService.getList("FOO", null, 0))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(CompanyErrorCode.UNSUPPORTED_MARKET);

            verifyNoInteractions(companyRepository);
        }

        @Test
        @DisplayName("market이 null이면 UNSUPPORTED_MARKET 예외가 발생한다")
        void getList_nullMarket_throwsUnsupportedMarket() {
            assertThatThrownBy(() -> companyListService.getList(null, null, 0))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(CompanyErrorCode.UNSUPPORTED_MARKET);

            verifyNoInteractions(companyRepository);
        }

        @Test
        @DisplayName("keyword가 있으면 searchByKeyword로 조회한다")
        void getList_withKeyword_usesSearchByKeyword() {
            Page<Company> page = new PageImpl<>(List.of(), PageRequest.of(0, 50), 0);
            given(companyRepository.searchByKeyword(eq(Market.KOSPI), eq("삼성"), any(Pageable.class)))
                    .willReturn(page);

            companyListService.getList("KOSPI", "삼성", 0);

            verify(companyRepository).searchByKeyword(eq(Market.KOSPI), eq("삼성"), any(Pageable.class));
            verify(companyRepository, never()).findByMarket(any(), any());
        }

        @Test
        @DisplayName("keyword 앞뒤 공백은 제거되고, 빈 문자열은 null로 취급되어 findByMarket을 사용한다")
        void getList_blankKeyword_normalizedToNull() {
            Page<Company> page = new PageImpl<>(List.of(), PageRequest.of(0, 50), 0);
            given(companyRepository.findByMarket(eq(Market.KOSPI), any(Pageable.class))).willReturn(page);

            companyListService.getList("KOSPI", "   ", 0);

            verify(companyRepository).findByMarket(eq(Market.KOSPI), any(Pageable.class));
            verify(companyRepository, never()).searchByKeyword(any(), any(), any());
        }

        @Test
        @DisplayName("페이지 크기는 50, 정렬은 stockCode 오름차순으로 고정된다")
        void getList_alwaysUsesFixedPageSizeAndSort() {
            Page<Company> page = new PageImpl<>(List.of(), PageRequest.of(1, 50), 0);
            given(companyRepository.findByMarket(eq(Market.KOSPI), any(Pageable.class))).willReturn(page);

            companyListService.getList("KOSPI", null, 1);

            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(companyRepository).findByMarket(eq(Market.KOSPI), captor.capture());
            Pageable captured = captor.getValue();
            assertThat(captured.getPageNumber()).isEqualTo(1);
            assertThat(captured.getPageSize()).isEqualTo(50);
            assertThat(captured.getSort().getOrderFor("stockCode").getDirection()).isEqualTo(Sort.Direction.ASC);
        }
    }
}
