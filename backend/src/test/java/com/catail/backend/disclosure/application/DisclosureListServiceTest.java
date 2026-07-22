package com.catail.backend.disclosure.application;

import com.catail.backend.company.db.CompanyRepository;
import com.catail.backend.disclosure.db.Disclosure;
import com.catail.backend.disclosure.db.DisclosureRemark;
import com.catail.backend.disclosure.db.DisclosureRemarkRepository;
import com.catail.backend.disclosure.db.DisclosureRepository;
import com.catail.backend.disclosure.db.DisclosureSyncStatus;
import com.catail.backend.disclosure.db.DisclosureSyncStatusRepository;
import com.catail.backend.disclosure.domain.DisclosureProvider;
import com.catail.backend.disclosure.inbound.read.DisclosureListResponse;
import com.catail.backend.global.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DisclosureListServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private DisclosureRepository disclosureRepository;

    @Mock
    private DisclosureRemarkRepository disclosureRemarkRepository;

    @Mock
    private DisclosureSyncStatusRepository disclosureSyncStatusRepository;

    private DisclosureListService disclosureListService;

    @BeforeEach
    void setUp() {
        disclosureListService = new DisclosureListService(
                companyRepository, disclosureRepository, disclosureRemarkRepository, disclosureSyncStatusRepository);
    }

    private Disclosure disclosureWithId(Long id, String externalId, LocalDate receivedDate) throws Exception {
        Disclosure disclosure = Disclosure.create(1L, DisclosureProvider.OPEN_DART, externalId, receivedDate, "보고서", "삼성전자");
        Field idField = Disclosure.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(disclosure, id);
        return disclosure;
    }

    @Test
    @DisplayName("존재하지 않는 기업이면 COMPANY_NOT_FOUND 예외가 발생한다")
    void getList_companyNotFound_throwsException() {
        given(companyRepository.existsById(999L)).willReturn(false);

        assertThatThrownBy(() -> disclosureListService.getList(999L, null, 20))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(DisclosureErrorCode.COMPANY_NOT_FOUND);
    }

    @Test
    @DisplayName("결과가 size보다 많으면 hasNext=true이고 nextCursor를 반환한다")
    void getList_moreThanSize_hasNextTrue() throws Exception {
        given(companyRepository.existsById(1L)).willReturn(true);
        Disclosure d1 = disclosureWithId(1L, "20240103000001", LocalDate.of(2024, 1, 3));
        Disclosure d2 = disclosureWithId(2L, "20240102000001", LocalDate.of(2024, 1, 2));
        given(disclosureRepository.findFirstPage(any(), any(), any()))
                .willReturn(List.of(d1, d2));
        given(disclosureRemarkRepository.findByDisclosureIdIn(any())).willReturn(List.of());

        DisclosureListResponse response = disclosureListService.getList(1L, null, 1);

        assertThat(response.hasNext()).isTrue();
        assertThat(response.items()).hasSize(1);
        assertThat(response.nextCursor()).isNotNull();
    }

    @Test
    @DisplayName("결과가 size 이하이면 hasNext=false이고 nextCursor는 null이다")
    void getList_lessThanOrEqualSize_hasNextFalse() throws Exception {
        given(companyRepository.existsById(1L)).willReturn(true);
        Disclosure d1 = disclosureWithId(1L, "20240103000001", LocalDate.of(2024, 1, 3));
        given(disclosureRepository.findFirstPage(any(), any(), any())).willReturn(List.of(d1));
        given(disclosureRemarkRepository.findByDisclosureIdIn(any())).willReturn(List.of());

        DisclosureListResponse response = disclosureListService.getList(1L, null, 20);

        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }

    @Test
    @DisplayName("동기화 상태가 있으면 lastSyncedAt과 initialSyncCompleted를 반환한다")
    void getList_withSyncStatus_returnsSyncFields() throws Exception {
        given(companyRepository.existsById(1L)).willReturn(true);
        given(disclosureRepository.findFirstPage(any(), any(), any())).willReturn(List.of());
        DisclosureSyncStatus status = DisclosureSyncStatus.create(1L, DisclosureProvider.OPEN_DART);
        status.markSuccess();
        given(disclosureSyncStatusRepository.findByCompanyIdAndProvider(1L, DisclosureProvider.OPEN_DART))
                .willReturn(Optional.of(status));

        DisclosureListResponse response = disclosureListService.getList(1L, null, 20);

        assertThat(response.initialSyncCompleted()).isTrue();
        assertThat(response.lastSyncedAt()).isNotNull();
    }

    @Test
    @DisplayName("동기화 상태가 없으면 초기 수집 미완료 상태로 반환한다")
    void getList_noSyncStatus_returnsPendingState() {
        given(companyRepository.existsById(1L)).willReturn(true);
        given(disclosureRepository.findFirstPage(any(), any(), any())).willReturn(List.of());
        given(disclosureSyncStatusRepository.findByCompanyIdAndProvider(1L, DisclosureProvider.OPEN_DART))
                .willReturn(Optional.empty());

        DisclosureListResponse response = disclosureListService.getList(1L, null, 20);

        assertThat(response.initialSyncCompleted()).isFalse();
        assertThat(response.lastSyncedAt()).isNull();
    }

    @Test
    @DisplayName("잘못된 커서를 전달하면 INVALID_CURSOR 예외가 발생한다")
    void getList_invalidCursor_throwsException() {
        given(companyRepository.existsById(1L)).willReturn(true);

        assertThatThrownBy(() -> disclosureListService.getList(1L, "not-a-valid-cursor!!", 20))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(DisclosureErrorCode.INVALID_CURSOR);
    }

    @Test
    @DisplayName("공시별 remarkCode를 올바르게 그룹핑하여 반환한다")
    void getList_groupsRemarkCodesByDisclosure() throws Exception {
        given(companyRepository.existsById(1L)).willReturn(true);
        Disclosure d1 = disclosureWithId(1L, "20240103000001", LocalDate.of(2024, 1, 3));
        given(disclosureRepository.findFirstPage(any(), any(), any())).willReturn(List.of(d1));
        given(disclosureRemarkRepository.findByDisclosureIdIn(List.of(1L)))
                .willReturn(List.of(DisclosureRemark.create(1L, "유"), DisclosureRemark.create(1L, "정")));

        DisclosureListResponse response = disclosureListService.getList(1L, null, 20);

        assertThat(response.items().get(0).remarkCodes()).containsExactlyInAnyOrder("유", "정");
    }
}
