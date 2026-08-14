package com.catail.backend.catalyst.application;

import com.catail.backend.catalyst.db.Catalyst;
import com.catail.backend.catalyst.db.CatalystRepository;
import com.catail.backend.catalyst.domain.CatalystCategory;
import com.catail.backend.catalyst.domain.CatalystStatus;
import com.catail.backend.catalyst.inbound.CatalystCreateRequest;
import com.catail.backend.catalyst.inbound.CatalystCreateResponse;
import com.catail.backend.company.application.CompanyErrorCode;
import com.catail.backend.company.db.Company;
import com.catail.backend.company.db.CompanyRepository;
import com.catail.backend.company.domain.Market;
import com.catail.backend.global.BusinessException;
import com.catail.backend.global.GlobalErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CatalystCreateServiceTest {

    private static final Long COMPANY_ID = 100L;

    @Mock
    private CatalystRepository catalystRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private CatalystCreateService catalystCreateService;

    private CatalystCreateRequest request(String category, String detail, String status) {
        return new CatalystCreateRequest(COMPANY_ID, category, detail, status);
    }

    @Test
    @DisplayName("동일 카테고리로 처음 생성하면 title에 접미사가 붙지 않는다")
    void create_firstOfCategory_titleHasNoSuffix() {
        catalystCreateService = new CatalystCreateService(catalystRepository, companyRepository, applicationEventPublisher);
        Company company = Company.create(Market.KOSPI, "005930", "삼성전자");
        given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(company));
        given(catalystRepository.countActive(eq(1L), isNull())).willReturn(0L);
        given(catalystRepository.countActiveByCategory(eq(1L), isNull(), eq(CatalystCategory.SUPPLY_CHAIN))).willReturn(0L);
        given(catalystRepository.save(any(Catalyst.class))).willAnswer(inv -> inv.getArgument(0));

        CatalystCreateResponse response = catalystCreateService.create(
                1L, request("SUPPLY_CHAIN", "HBM 공급망 관련 계약 동향 모니터링", "ACTIVE"));

        assertThat(response.title()).isEqualTo("공급망");
        assertThat(response.category()).isEqualTo("SUPPLY_CHAIN");
        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(response.market()).isEqualTo("KOSPI");
        assertThat(response.stockCode()).isEqualTo("005930");
    }

    @Test
    @DisplayName("status=ACTIVE로 생성하면 CatalystActivatedEvent를 발행한다")
    void create_statusActive_publishesCatalystActivatedEvent() {
        catalystCreateService = new CatalystCreateService(catalystRepository, companyRepository, applicationEventPublisher);
        Company company = Company.create(Market.KOSPI, "005930", "삼성전자");
        given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(company));
        given(catalystRepository.countActive(eq(1L), isNull())).willReturn(0L);
        given(catalystRepository.countActiveByCategory(eq(1L), isNull(), eq(CatalystCategory.SUPPLY_CHAIN))).willReturn(0L);
        given(catalystRepository.save(any(Catalyst.class))).willAnswer(inv -> inv.getArgument(0));

        catalystCreateService.create(1L, request("SUPPLY_CHAIN", "HBM 공급망 관련 계약 동향 모니터링", "ACTIVE"));

        verify(applicationEventPublisher).publishEvent(any(CatalystActivatedEvent.class));
    }

    @Test
    @DisplayName("status=INACTIVE로 생성하면 이벤트를 발행하지 않는다")
    void create_statusInactive_doesNotPublishEvent() {
        catalystCreateService = new CatalystCreateService(catalystRepository, companyRepository, applicationEventPublisher);
        Company company = Company.create(Market.KOSPI, "005930", "삼성전자");
        given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(company));
        given(catalystRepository.countActive(eq(1L), isNull())).willReturn(0L);
        given(catalystRepository.countActiveByCategory(eq(1L), isNull(), eq(CatalystCategory.SUPPLY_CHAIN))).willReturn(0L);
        given(catalystRepository.save(any(Catalyst.class))).willAnswer(inv -> inv.getArgument(0));

        catalystCreateService.create(1L, request("SUPPLY_CHAIN", "HBM 공급망 관련 계약 동향 모니터링", "INACTIVE"));

        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    @DisplayName("동일 기업+동일 카테고리 카탈리스트가 이미 있으면 title에 #N이 붙는다")
    void create_duplicateCategory_titleHasSequenceSuffix() {
        catalystCreateService = new CatalystCreateService(catalystRepository, companyRepository, applicationEventPublisher);
        Company company = Company.create(Market.KOSPI, "005930", "삼성전자");
        given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(company));
        given(catalystRepository.countActive(eq(1L), isNull())).willReturn(1L);
        given(catalystRepository.countActiveByCategory(eq(1L), isNull(), eq(CatalystCategory.SUPPLY_CHAIN))).willReturn(1L);
        given(catalystRepository.save(any(Catalyst.class))).willAnswer(inv -> inv.getArgument(0));

        CatalystCreateResponse response = catalystCreateService.create(
                1L, request("SUPPLY_CHAIN", "HBM 공급망 관련 계약 동향 모니터링", "INACTIVE"));

        assertThat(response.title()).isEqualTo("공급망 #2");
    }

    @Test
    @DisplayName("companyId에 해당하는 기업이 없으면 COMPANY_NOT_FOUND 예외가 발생한다")
    void create_companyNotFound_throwsCompanyNotFound() {
        catalystCreateService = new CatalystCreateService(catalystRepository, companyRepository, applicationEventPublisher);
        given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> catalystCreateService.create(
                1L, request("SUPPLY_CHAIN", "HBM 공급망 관련 계약 동향 모니터링", "ACTIVE")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(CompanyErrorCode.COMPANY_NOT_FOUND);
    }

    @Test
    @DisplayName("동일 사용자·동일 기업의 유효한 카탈리스트가 3개면 CATALYST_LIMIT_EXCEEDED 예외가 발생한다")
    void create_limitReached_throwsCatalystLimitExceeded() {
        catalystCreateService = new CatalystCreateService(catalystRepository, companyRepository, applicationEventPublisher);
        Company company = Company.create(Market.KOSPI, "005930", "삼성전자");
        given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(company));
        given(catalystRepository.countActive(eq(1L), isNull())).willReturn(3L);

        assertThatThrownBy(() -> catalystCreateService.create(
                1L, request("SUPPLY_CHAIN", "HBM 공급망 관련 계약 동향 모니터링", "ACTIVE")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(CatalystErrorCode.CATALYST_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("category가 정의된 6개 값이 아니면 INVALID_INPUT 예외가 발생한다")
    void create_invalidCategory_throwsInvalidInput() {
        catalystCreateService = new CatalystCreateService(catalystRepository, companyRepository, applicationEventPublisher);
        Company company = Company.create(Market.KOSPI, "005930", "삼성전자");
        given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(company));

        assertThatThrownBy(() -> catalystCreateService.create(
                1L, request("UNKNOWN_CATEGORY", "HBM 공급망 관련 계약 동향 모니터링", "ACTIVE")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(GlobalErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("status가 ACTIVE/INACTIVE가 아니면 INVALID_INPUT 예외가 발생한다")
    void create_invalidStatus_throwsInvalidInput() {
        catalystCreateService = new CatalystCreateService(catalystRepository, companyRepository, applicationEventPublisher);
        Company company = Company.create(Market.KOSPI, "005930", "삼성전자");
        given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(company));

        assertThatThrownBy(() -> catalystCreateService.create(
                1L, request("SUPPLY_CHAIN", "HBM 공급망 관련 계약 동향 모니터링", "UNKNOWN")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(GlobalErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("status가 PAUSED이면 생성 시점에는 허용되지 않아 INVALID_INPUT 예외가 발생한다")
    void create_pausedStatus_throwsInvalidInput() {
        catalystCreateService = new CatalystCreateService(catalystRepository, companyRepository, applicationEventPublisher);
        Company company = Company.create(Market.KOSPI, "005930", "삼성전자");
        given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(company));

        assertThatThrownBy(() -> catalystCreateService.create(
                1L, request("SUPPLY_CHAIN", "HBM 공급망 관련 계약 동향 모니터링", CatalystStatus.PAUSED.name())))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(GlobalErrorCode.INVALID_INPUT);
    }
}
