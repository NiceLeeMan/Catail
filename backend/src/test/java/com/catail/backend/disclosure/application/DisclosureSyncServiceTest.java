package com.catail.backend.disclosure.application;

import com.catail.backend.company.db.Company;
import com.catail.backend.company.domain.Market;
import com.catail.backend.disclosure.db.Disclosure;
import com.catail.backend.disclosure.db.DisclosureRemark;
import com.catail.backend.disclosure.db.DisclosureRemarkRepository;
import com.catail.backend.disclosure.db.DisclosureRepository;
import com.catail.backend.disclosure.db.DisclosureSyncStatus;
import com.catail.backend.disclosure.db.DisclosureSyncStatusRepository;
import com.catail.backend.disclosure.domain.DisclosureProvider;
import com.catail.backend.disclosure.outbound.DisclosureCollectionPage;
import com.catail.backend.disclosure.outbound.DisclosureCollectionPort;
import com.catail.backend.disclosure.outbound.RawDisclosureItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DisclosureSyncServiceTest {

    @Mock
    private DisclosureCollectionPort openDartPort;

    @Mock
    private DisclosureRepository disclosureRepository;

    @Mock
    private DisclosureRemarkRepository disclosureRemarkRepository;

    @Mock
    private DisclosureSyncStatusRepository disclosureSyncStatusRepository;

    private DisclosureSyncService disclosureSyncService;

    @BeforeEach
    void setUp() {
        disclosureSyncService = new DisclosureSyncService(
                List.of(openDartPort), disclosureRepository, disclosureRemarkRepository, disclosureSyncStatusRepository);
    }

    private Company companyWithId(Long id, String corpCode) throws Exception {
        Company company = Company.create(Market.KOSPI, "005930", "삼성전자");
        if (corpCode != null) {
            company.assignOpenDartCorpCode(corpCode);
        }
        setId(company, id);
        return company;
    }

    private void setId(Object entity, Long id) throws Exception {
        Field idField = entity.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }

    @Nested
    @DisplayName("syncCompany")
    class SyncCompany {

        @Test
        @DisplayName("corp_code가 없으면 아무 작업도 하지 않는다")
        void syncCompany_noCorpCode_skips() throws Exception {
            Company company = companyWithId(1L, null);

            disclosureSyncService.syncCompany(company);

            verify(disclosureSyncStatusRepository, never()).findByCompanyIdAndProvider(any(), any());
        }

        @Test
        @DisplayName("최초 수집이면 3년 전 날짜로 조회하고 성공 시 상태를 갱신한다")
        void syncCompany_initialSync_fetchesThreeYearsAndMarksSuccess() throws Exception {
            Company company = companyWithId(1L, "00126380");
            given(openDartPort.provider()).willReturn(DisclosureProvider.OPEN_DART);
            given(disclosureSyncStatusRepository.findByCompanyIdAndProvider(1L, DisclosureProvider.OPEN_DART))
                    .willReturn(Optional.empty());
            given(openDartPort.fetchPage(eq("00126380"), any(LocalDate.class), any(LocalDate.class), eq(1), eq(100)))
                    .willReturn(new DisclosureCollectionPage(0, List.of()));

            disclosureSyncService.syncCompany(company);

            ArgumentCaptor<LocalDate> beginDateCaptor = ArgumentCaptor.forClass(LocalDate.class);
            verify(openDartPort).fetchPage(eq("00126380"), beginDateCaptor.capture(), any(LocalDate.class), eq(1), eq(100));
            assertThat(beginDateCaptor.getValue()).isEqualTo(LocalDate.now().minusYears(3));

            ArgumentCaptor<DisclosureSyncStatus> statusCaptor = ArgumentCaptor.forClass(DisclosureSyncStatus.class);
            verify(disclosureSyncStatusRepository).save(statusCaptor.capture());
            assertThat(statusCaptor.getValue().isInitialSyncCompleted()).isTrue();
        }

        @Test
        @DisplayName("이미 최초 수집이 완료됐으면 7일 전 날짜로 증분 조회한다")
        void syncCompany_incrementalSync_fetchesSevenDays() throws Exception {
            Company company = companyWithId(1L, "00126380");
            given(openDartPort.provider()).willReturn(DisclosureProvider.OPEN_DART);
            DisclosureSyncStatus existingStatus = DisclosureSyncStatus.create(1L, DisclosureProvider.OPEN_DART);
            existingStatus.markSuccess();
            given(disclosureSyncStatusRepository.findByCompanyIdAndProvider(1L, DisclosureProvider.OPEN_DART))
                    .willReturn(Optional.of(existingStatus));
            given(openDartPort.fetchPage(eq("00126380"), any(LocalDate.class), any(LocalDate.class), eq(1), eq(100)))
                    .willReturn(new DisclosureCollectionPage(0, List.of()));

            disclosureSyncService.syncCompany(company);

            ArgumentCaptor<LocalDate> beginDateCaptor = ArgumentCaptor.forClass(LocalDate.class);
            verify(openDartPort).fetchPage(eq("00126380"), beginDateCaptor.capture(), any(LocalDate.class), eq(1), eq(100));
            assertThat(beginDateCaptor.getValue()).isEqualTo(LocalDate.now().minusDays(7));
        }

        @Test
        @DisplayName("신규 공시는 저장하고 remarkCodes를 함께 저장한다")
        void syncCompany_newDisclosure_savesDisclosureAndRemarks() throws Exception {
            Company company = companyWithId(1L, "00126380");
            given(openDartPort.provider()).willReturn(DisclosureProvider.OPEN_DART);
            given(disclosureSyncStatusRepository.findByCompanyIdAndProvider(1L, DisclosureProvider.OPEN_DART))
                    .willReturn(Optional.empty());

            RawDisclosureItem rawItem = new RawDisclosureItem(
                    "20240101000123", LocalDate.of(2024, 1, 1), "주요사항보고서", "삼성전자", List.of("유", "정"));
            given(openDartPort.fetchPage(eq("00126380"), any(), any(), eq(1), eq(100)))
                    .willReturn(new DisclosureCollectionPage(1, List.of(rawItem)));
            given(disclosureRepository.findByProviderAndExternalDisclosureId(
                    DisclosureProvider.OPEN_DART, "20240101000123"))
                    .willReturn(Optional.empty());

            Disclosure saved = Disclosure.create(1L, DisclosureProvider.OPEN_DART, "20240101000123",
                    LocalDate.of(2024, 1, 1), "주요사항보고서", "삼성전자");
            setId(saved, 10L);
            given(disclosureRepository.save(any(Disclosure.class))).willReturn(saved);
            given(disclosureRemarkRepository.existsByDisclosureIdAndCode(10L, "유")).willReturn(false);
            given(disclosureRemarkRepository.existsByDisclosureIdAndCode(10L, "정")).willReturn(false);

            disclosureSyncService.syncCompany(company);

            verify(disclosureRepository).save(any(Disclosure.class));
            verify(disclosureRemarkRepository).save(argThatRemark("유"));
            verify(disclosureRemarkRepository).save(argThatRemark("정"));
        }

        private DisclosureRemark argThatRemark(String code) {
            return org.mockito.ArgumentMatchers.argThat(remark -> remark.getCode().equals(code));
        }

        @Test
        @DisplayName("이미 존재하는 공시는 내용만 갱신하고 기존 remark는 다시 저장하지 않는다")
        void syncCompany_existingDisclosure_updatesContentAndSkipsExistingRemarks() throws Exception {
            Company company = companyWithId(1L, "00126380");
            given(openDartPort.provider()).willReturn(DisclosureProvider.OPEN_DART);
            given(disclosureSyncStatusRepository.findByCompanyIdAndProvider(1L, DisclosureProvider.OPEN_DART))
                    .willReturn(Optional.empty());

            RawDisclosureItem rawItem = new RawDisclosureItem(
                    "20240101000123", LocalDate.of(2024, 1, 1), "정정 보고서", "삼성전자", List.of("유"));
            given(openDartPort.fetchPage(eq("00126380"), any(), any(), eq(1), eq(100)))
                    .willReturn(new DisclosureCollectionPage(1, List.of(rawItem)));

            Disclosure existing = Disclosure.create(1L, DisclosureProvider.OPEN_DART, "20240101000123",
                    LocalDate.of(2024, 1, 1), "주요사항보고서", "삼성전자");
            setId(existing, 10L);
            given(disclosureRepository.findByProviderAndExternalDisclosureId(
                    DisclosureProvider.OPEN_DART, "20240101000123"))
                    .willReturn(Optional.of(existing));
            given(disclosureRepository.save(existing)).willReturn(existing);
            given(disclosureRemarkRepository.existsByDisclosureIdAndCode(10L, "유")).willReturn(true);

            disclosureSyncService.syncCompany(company);

            assertThat(existing.getReportName()).isEqualTo("정정 보고서");
            verify(disclosureRemarkRepository, never()).save(any(DisclosureRemark.class));
        }
    }
}
