package com.catail.backend.catalyst.application;

import com.catail.backend.catalyst.DB.CatalystRepositoryAdapter;
import com.catail.backend.catalyst.DB.CatalystStatus;
import com.catail.backend.catalyst.domain.CatalystDomain;
import com.catail.backend.catalyst.inbound.create.CatalystCreateResponse;
import com.catail.backend.catalyst.inbound.delete.CatalystDeleteResponse;
import com.catail.backend.catalyst.inbound.list.CatalystListItemResponse;
import com.catail.backend.catalyst.inbound.update.CatalystStatusResponse;
import com.catail.backend.catalyst.inbound.update.CatalystUpdateResponse;
import com.catail.backend.catalyst.outbound.SignalCollectionPort;
import com.catail.backend.global.BusinessException;
import com.catail.backend.global.GlobalErrorCode;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CatalystServiceTest {

    @Mock
    private CatalystRepositoryAdapter catalystRepositoryAdapter;

    @Mock
    private SignalCollectionPort signalCollectionPort;

    @InjectMocks
    private CatalystService catalystService;

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("status가 ACTIVE로 생성되면 저장 후 시그널 수집 트리거가 호출된다")
        void create_activeStatus_triggersSignalCollection() {
            given(catalystRepositoryAdapter.existsAllIndustries(List.of(1L))).willReturn(true);
            CatalystDomain saved = CatalystDomain.reconstruct(
                    10L, 1L, "title", "a".repeat(50), CatalystStatus.ACTIVE, List.of(1L), List.of(), 4, null, null,
                    LocalDateTime.now(), LocalDateTime.now(), null);
            given(catalystRepositoryAdapter.save(any(CatalystDomain.class))).willReturn(saved);
            given(catalystRepositoryAdapter.findIndustryNamesByIds(List.of(1L))).willReturn(Map.of(1L, "IT"));

            CatalystCreateResponse response = catalystService.create(1L, "title", "a".repeat(50), List.of(1L), "ACTIVE");

            assertThat(response.id()).isEqualTo(10L);
            assertThat(response.status()).isEqualTo("ACTIVE");
            assertThat(response.industries()).containsExactly("IT");
            verify(signalCollectionPort).triggerCollection(10L);
        }

        @Test
        @DisplayName("status가 INACTIVE로 생성되면 시그널 수집 트리거가 호출되지 않는다")
        void create_inactiveStatus_doesNotTriggerSignalCollection() {
            given(catalystRepositoryAdapter.existsAllIndustries(List.of(1L))).willReturn(true);
            CatalystDomain saved = CatalystDomain.reconstruct(
                    11L, 1L, "title", "a".repeat(50), CatalystStatus.INACTIVE, List.of(1L), List.of(), 4, null, null,
                    LocalDateTime.now(), LocalDateTime.now(), null);
            given(catalystRepositoryAdapter.save(any(CatalystDomain.class))).willReturn(saved);
            given(catalystRepositoryAdapter.findIndustryNamesByIds(List.of(1L))).willReturn(Map.of(1L, "IT"));

            catalystService.create(1L, "title", "a".repeat(50), List.of(1L), "INACTIVE");

            verify(signalCollectionPort, never()).triggerCollection(any());
        }

        @Test
        @DisplayName("존재하지 않는 industryId가 포함되면 INVALID_INPUT 예외가 발생하고 저장/트리거가 호출되지 않는다")
        void create_nonExistentIndustryId_throwsInvalidInputAndSkipsSaveAndTrigger() {
            given(catalystRepositoryAdapter.existsAllIndustries(List.of(999L))).willReturn(false);

            assertThatThrownBy(() -> catalystService.create(1L, "title", "a".repeat(50), List.of(999L), "ACTIVE"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(GlobalErrorCode.INVALID_INPUT);

            verify(catalystRepositoryAdapter, never()).save(any());
            verify(signalCollectionPort, never()).triggerCollection(any());
        }

        @Test
        @DisplayName("title이 유효하지 않으면 도메인 검증에서 예외가 발생하고 어댑터는 호출되지 않는다")
        void create_invalidTitle_throwsBeforeTouchingAdapter() {
            assertThatThrownBy(() -> catalystService.create(1L, "   ", "a".repeat(50), List.of(1L), "ACTIVE"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(GlobalErrorCode.INVALID_INPUT);

            verifyNoInteractions(catalystRepositoryAdapter, signalCollectionPort);
        }
    }

    @Nested
    @DisplayName("getList")
    class GetList {

        @Test
        @DisplayName("목록 조회 시 industryTags와 pendingSignalCount(스텁 0)가 올바르게 매핑된다")
        void getList_withCatalysts_mapsCorrectly() {
            CatalystDomain domain = CatalystDomain.reconstruct(
                    1L, 1L, "title1", "content1", CatalystStatus.ACTIVE, List.of(1L, 2L), List.of(), 4, null, null,
                    LocalDateTime.now(), LocalDateTime.now(), null);
            Page<CatalystDomain> page = new PageImpl<>(List.of(domain), PageRequest.of(0, 20), 1);
            given(catalystRepositoryAdapter.findPageByUserId(eq(1L), any(Pageable.class))).willReturn(page);
            given(catalystRepositoryAdapter.findIndustryNamesByIds(anyList())).willReturn(Map.of(1L, "IT", 2L, "Finance"));
            given(catalystRepositoryAdapter.countPendingSignalsByCatalystIds(anyList())).willReturn(Map.of());

            var response = catalystService.getList(1L, 0);

            assertThat(response.items()).hasSize(1);
            CatalystListItemResponse item = response.items().get(0);
            assertThat(item.industryTags()).containsExactly("IT", "Finance");
            assertThat(item.pendingSignalCount()).isZero();
            assertThat(response.totalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("결과가 0건이면 예외 없이 빈 목록을 반환한다")
        void getList_noCatalysts_returnsEmptyList() {
            Page<CatalystDomain> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
            given(catalystRepositoryAdapter.findPageByUserId(eq(1L), any(Pageable.class))).willReturn(emptyPage);
            given(catalystRepositoryAdapter.findIndustryNamesByIds(anyList())).willReturn(Map.of());
            given(catalystRepositoryAdapter.countPendingSignalsByCatalystIds(anyList())).willReturn(Map.of());

            var response = catalystService.getList(1L, 0);

            assertThat(response.items()).isEmpty();
            assertThat(response.totalElements()).isZero();
        }

        @Test
        @DisplayName("페이지 크기는 20, 정렬은 createdAt 내림차순으로 고정된다")
        void getList_alwaysUsesFixedPageSizeAndSort() {
            Page<CatalystDomain> page = new PageImpl<>(List.of(), PageRequest.of(2, 20), 0);
            given(catalystRepositoryAdapter.findPageByUserId(eq(1L), any(Pageable.class))).willReturn(page);
            given(catalystRepositoryAdapter.findIndustryNamesByIds(anyList())).willReturn(Map.of());
            given(catalystRepositoryAdapter.countPendingSignalsByCatalystIds(anyList())).willReturn(Map.of());

            catalystService.getList(1L, 2);

            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(catalystRepositoryAdapter).findPageByUserId(eq(1L), captor.capture());
            Pageable captured = captor.getValue();
            assertThat(captured.getPageNumber()).isEqualTo(2);
            assertThat(captured.getPageSize()).isEqualTo(20);
            assertThat(captured.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        @DisplayName("pendingSignalCount 맵에 없는 catalystId는 0으로 기본값 처리된다")
        void getList_missingPendingCount_defaultsToZero() {
            CatalystDomain domain = CatalystDomain.reconstruct(
                    5L, 1L, "title", "content", CatalystStatus.ACTIVE, List.of(1L), List.of(), 4, null, null,
                    LocalDateTime.now(), LocalDateTime.now(), null);
            Page<CatalystDomain> page = new PageImpl<>(List.of(domain), PageRequest.of(0, 20), 1);
            given(catalystRepositoryAdapter.findPageByUserId(eq(1L), any(Pageable.class))).willReturn(page);
            given(catalystRepositoryAdapter.findIndustryNamesByIds(anyList())).willReturn(Map.of(1L, "IT"));
            given(catalystRepositoryAdapter.countPendingSignalsByCatalystIds(anyList())).willReturn(Map.of());

            var response = catalystService.getList(1L, 0);

            assertThat(response.items().get(0).pendingSignalCount()).isZero();
        }
    }

    @Nested
    @DisplayName("getDetail")
    class GetDetail {

        @Test
        @DisplayName("존재하는 본인 소유 catalyst이면 basicInfo와 monitoringOperation을 반환한다")
        void getDetail_existingCatalyst_returnsDetail() {
            CatalystDomain domain = CatalystDomain.reconstruct(
                    1L, 1L, "title", "content", CatalystStatus.ACTIVE, List.of(1L), List.of("검색어"), 6,
                    LocalDateTime.now(), null, LocalDateTime.now(), LocalDateTime.now(), null);
            given(catalystRepositoryAdapter.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(domain));
            given(catalystRepositoryAdapter.findIndustryNamesByIds(List.of(1L))).willReturn(Map.of(1L, "IT"));

            CatalystInfoResponse response = catalystService.getDetail(1L, 1L);

            assertThat(response.basicInfo().title()).isEqualTo("title");
            assertThat(response.basicInfo().industries()).containsExactly("IT");
            assertThat(response.monitoringOperation().status()).isEqualTo("ACTIVE");
            assertThat(response.monitoringOperation().searchConditions()).containsExactly("검색어");
            assertThat(response.monitoringOperation().searchIntervalHours()).isEqualTo(6);
            assertThat(response.monitoringOperation().lastSearchedAt()).isNotNull();
            assertThat(response.monitoringOperation().activatedAt()).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 id로 조회하면 NOT_FOUND 예외가 발생한다")
        void getDetail_nonExistentId_throwsNotFound() {
            given(catalystRepositoryAdapter.findByIdAndUserId(999L, 1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> catalystService.getDetail(999L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(CatalystErrorCode.NOT_FOUND);
        }

        @Test
        @DisplayName("타인 소유 catalyst를 조회하면 NOT_FOUND 예외가 발생한다")
        void getDetail_notOwner_throwsNotFound() {
            given(catalystRepositoryAdapter.findByIdAndUserId(1L, 2L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> catalystService.getDetail(1L, 2L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(CatalystErrorCode.NOT_FOUND);
        }

        @Test
        @DisplayName("삭제된 catalyst를 조회하면 NOT_FOUND 예외가 발생한다")
        void getDetail_deletedCatalyst_throwsNotFound() {
            given(catalystRepositoryAdapter.findByIdAndUserId(1L, 1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> catalystService.getDetail(1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(CatalystErrorCode.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("본인 소유 catalyst 삭제 시 status ENDED로 영속화되고 응답에 status/deletedAt이 담긴다")
        void delete_ownedCatalyst_persistsEndedStatus() {
            CatalystDomain domain = CatalystDomain.reconstruct(
                    1L, 1L, "title", "content", CatalystStatus.ACTIVE, List.of(1L), List.of(), 4, null, null,
                    LocalDateTime.now(), LocalDateTime.now(), null);
            given(catalystRepositoryAdapter.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(domain));

            CatalystDeleteResponse response = catalystService.delete(1L, 1L);

            verify(catalystRepositoryAdapter).persistStatusAndDeletion(1L, CatalystStatus.ENDED);
            assertThat(response.status()).isEqualTo("ENDED");
            assertThat(response.deletedAt()).isNotNull();
        }

        @Test
        @DisplayName("존재하지 않는 id를 삭제하면 NOT_FOUND 예외가 발생하고 영속화되지 않는다")
        void delete_nonExistentId_throwsNotFound() {
            given(catalystRepositoryAdapter.findByIdAndUserId(999L, 1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> catalystService.delete(999L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(CatalystErrorCode.NOT_FOUND);

            verify(catalystRepositoryAdapter, never()).persistStatusAndDeletion(any(), any());
        }

        @Test
        @DisplayName("타인 소유 catalyst를 삭제하면 NOT_FOUND 예외가 발생한다")
        void delete_notOwner_throwsNotFound() {
            given(catalystRepositoryAdapter.findByIdAndUserId(1L, 2L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> catalystService.delete(1L, 2L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(CatalystErrorCode.NOT_FOUND);

            verify(catalystRepositoryAdapter, never()).persistStatusAndDeletion(any(), any());
        }

        @Test
        @DisplayName("이미 삭제된 catalyst를 재삭제하면 NOT_FOUND 예외가 발생한다")
        void delete_alreadyDeleted_throwsNotFound() {
            given(catalystRepositoryAdapter.findByIdAndUserId(1L, 1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> catalystService.delete(1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(CatalystErrorCode.NOT_FOUND);

            verify(catalystRepositoryAdapter, never()).persistStatusAndDeletion(any(), any());
        }
    }

    @Nested
    @DisplayName("updateBasicInfo")
    class UpdateBasicInfo {

        @Test
        @DisplayName("정상 요청이면 title/content/industries를 교체하고 갱신된 응답을 반환한다")
        void updateBasicInfo_validRequest_replacesAndReturnsResponse() {
            CatalystDomain domain = CatalystDomain.reconstruct(
                    1L, 1L, "old title", "a".repeat(50), CatalystStatus.ACTIVE, List.of(1L), List.of(), 4, null, null,
                    LocalDateTime.now(), LocalDateTime.now(), null);
            given(catalystRepositoryAdapter.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(domain));
            given(catalystRepositoryAdapter.existsAllIndustries(List.of(2L))).willReturn(true);
            LocalDateTime updatedAt = LocalDateTime.now();
            given(catalystRepositoryAdapter.persistBasicInfo(1L, "new title", "b".repeat(60))).willReturn(updatedAt);
            given(catalystRepositoryAdapter.findIndustryNamesByIds(List.of(2L))).willReturn(Map.of(2L, "Finance"));

            CatalystUpdateResponse response = catalystService.updateBasicInfo(
                    1L, 1L, "new title", "b".repeat(60), List.of(2L));

            assertThat(response.title()).isEqualTo("new title");
            assertThat(response.content()).hasSize(60);
            assertThat(response.industries()).containsExactly("Finance");
            assertThat(response.updatedAt()).isEqualTo(updatedAt);
            verify(catalystRepositoryAdapter).replaceIndustries(1L, List.of(2L));
        }

        @Test
        @DisplayName("존재하지 않는 catalyst이면 NOT_FOUND 예외가 발생한다")
        void updateBasicInfo_nonExistentId_throwsNotFound() {
            given(catalystRepositoryAdapter.findByIdAndUserId(999L, 1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> catalystService.updateBasicInfo(999L, 1L, "title", "a".repeat(50), List.of(1L)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(CatalystErrorCode.NOT_FOUND);
        }

        @Test
        @DisplayName("존재하지 않는 industryId가 포함되면 INVALID_INPUT 예외가 발생하고 저장되지 않는다")
        void updateBasicInfo_nonExistentIndustryId_throwsInvalidInputAndSkipsPersist() {
            CatalystDomain domain = CatalystDomain.reconstruct(
                    1L, 1L, "title", "a".repeat(50), CatalystStatus.ACTIVE, List.of(1L), List.of(), 4, null, null,
                    LocalDateTime.now(), LocalDateTime.now(), null);
            given(catalystRepositoryAdapter.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(domain));
            given(catalystRepositoryAdapter.existsAllIndustries(List.of(999L))).willReturn(false);

            assertThatThrownBy(() -> catalystService.updateBasicInfo(1L, 1L, "title", "a".repeat(50), List.of(999L)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(GlobalErrorCode.INVALID_INPUT);

            verify(catalystRepositoryAdapter, never()).persistBasicInfo(any(), any(), any());
            verify(catalystRepositoryAdapter, never()).replaceIndustries(any(), any());
        }

        @Test
        @DisplayName("title이 유효하지 않으면 도메인 검증에서 예외가 발생하고 어댑터의 영속화 메서드는 호출되지 않는다")
        void updateBasicInfo_invalidTitle_throwsBeforePersisting() {
            CatalystDomain domain = CatalystDomain.reconstruct(
                    1L, 1L, "title", "a".repeat(50), CatalystStatus.ACTIVE, List.of(1L), List.of(), 4, null, null,
                    LocalDateTime.now(), LocalDateTime.now(), null);
            given(catalystRepositoryAdapter.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(domain));

            assertThatThrownBy(() -> catalystService.updateBasicInfo(1L, 1L, "a".repeat(51), "a".repeat(50), List.of(1L)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(GlobalErrorCode.INVALID_INPUT);

            verify(catalystRepositoryAdapter, never()).persistBasicInfo(any(), any(), any());
            verify(catalystRepositoryAdapter, never()).replaceIndustries(any(), any());
        }
    }

    @Nested
    @DisplayName("changeStatus")
    class ChangeStatus {

        @Test
        @DisplayName("허용된 전이 요청이면 상태를 영속화하고 응답을 반환한다")
        void changeStatus_allowedTransition_persistsAndReturnsResponse() {
            CatalystDomain domain = CatalystDomain.reconstruct(
                    1L, 1L, "title", "a".repeat(50), CatalystStatus.ACTIVE, List.of(1L), List.of(), 4, null, null,
                    LocalDateTime.now(), LocalDateTime.now(), null);
            given(catalystRepositoryAdapter.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(domain));
            LocalDateTime updatedAt = LocalDateTime.now();
            given(catalystRepositoryAdapter.persistStatus(1L, CatalystStatus.PAUSED)).willReturn(updatedAt);

            CatalystStatusResponse response = catalystService.changeStatus(1L, 1L, "PAUSED");

            assertThat(response.status()).isEqualTo("PAUSED");
            assertThat(response.updatedAt()).isEqualTo(updatedAt);
            verify(catalystRepositoryAdapter, never()).persistStatusAndDeletion(any(), any());
        }

        @Test
        @DisplayName("허용되지 않은 전이 요청이면 INVALID_INPUT 예외가 발생하고 영속화되지 않는다")
        void changeStatus_disallowedTransition_throwsInvalidInputAndSkipsPersist() {
            CatalystDomain domain = CatalystDomain.reconstruct(
                    1L, 1L, "title", "a".repeat(50), CatalystStatus.ENDED, List.of(1L), List.of(), 4, null, null,
                    LocalDateTime.now(), LocalDateTime.now(), null);
            given(catalystRepositoryAdapter.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(domain));

            assertThatThrownBy(() -> catalystService.changeStatus(1L, 1L, "ACTIVE"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(GlobalErrorCode.INVALID_INPUT);

            verify(catalystRepositoryAdapter, never()).persistStatus(any(), any());
        }

        @Test
        @DisplayName("존재하지 않는 targetStatus 문자열이면 INVALID_INPUT 예외가 발생한다")
        void changeStatus_unknownTargetStatus_throwsInvalidInput() {
            CatalystDomain domain = CatalystDomain.reconstruct(
                    1L, 1L, "title", "a".repeat(50), CatalystStatus.ACTIVE, List.of(1L), List.of(), 4, null, null,
                    LocalDateTime.now(), LocalDateTime.now(), null);
            given(catalystRepositoryAdapter.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(domain));

            assertThatThrownBy(() -> catalystService.changeStatus(1L, 1L, "FOO"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(GlobalErrorCode.INVALID_INPUT);

            verify(catalystRepositoryAdapter, never()).persistStatus(any(), any());
        }

        @Test
        @DisplayName("존재하지 않는 catalyst이면 NOT_FOUND 예외가 발생한다")
        void changeStatus_nonExistentId_throwsNotFound() {
            given(catalystRepositoryAdapter.findByIdAndUserId(999L, 1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> catalystService.changeStatus(999L, 1L, "ACTIVE"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(CatalystErrorCode.NOT_FOUND);
        }
    }
}
