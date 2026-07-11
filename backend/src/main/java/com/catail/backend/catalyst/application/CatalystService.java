package com.catail.backend.catalyst.application;

import com.catail.backend.catalyst.DB.CatalystRepositoryAdapter;
import com.catail.backend.catalyst.DB.CatalystStatus;
import com.catail.backend.catalyst.domain.CatalystDomain;
import com.catail.backend.catalyst.outbound.SignalCollectionPort;
import com.catail.backend.global.BusinessException;
import com.catail.backend.global.GlobalErrorCode;
import com.catail.backend.global.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CatalystService {

    private static final int PAGE_SIZE = 20;

    private final CatalystRepositoryAdapter catalystRepositoryAdapter;
    private final SignalCollectionPort signalCollectionPort;

    // UC-1: 카탈리스트 생성
    @Transactional
    public CatalystCreateResponse create(Long userId, String title, String content,
                                          List<Long> industryIds, String status) {
        CatalystDomain domain = CatalystDomain.create(userId, title, content, industryIds, status);

        if (!catalystRepositoryAdapter.existsAllIndustries(domain.getIndustryIds())) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }

        CatalystDomain saved = catalystRepositoryAdapter.save(domain);

        if (saved.isActive()) {
            signalCollectionPort.triggerCollection(saved.getId());
        }

        return toCreateResponse(saved);
    }

    // UC-2: 카탈리스트 목록 조회
    @Transactional(readOnly = true)
    public PageResponse<CatalystListItemResponse> getList(Long userId, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CatalystDomain> result = catalystRepositoryAdapter.findPageByUserId(userId, pageable);

        List<Long> allIndustryIds = result.getContent().stream()
                .flatMap(domain -> domain.getIndustryIds().stream())
                .distinct()
                .toList();
        Map<Long, String> industryNames = catalystRepositoryAdapter.findIndustryNamesByIds(allIndustryIds);

        List<Long> catalystIds = result.getContent().stream().map(CatalystDomain::getId).toList();
        Map<Long, Integer> pendingCounts = catalystRepositoryAdapter.countPendingSignalsByCatalystIds(catalystIds);

        Page<CatalystListItemResponse> mapped = result.map(domain -> new CatalystListItemResponse(
                domain.getId(),
                domain.getTitle(),
                domain.getStatus().name(),
                domain.getIndustryIds().stream().map(industryNames::get).toList(),
                pendingCounts.getOrDefault(domain.getId(), 0),
                domain.getCreatedAt()));

        return PageResponse.of(mapped);
    }

    // UC-3: 카탈리스트 상세 조회 (카탈리스트 정보 탭)
    @Transactional(readOnly = true)
    public CatalystInfoResponse getDetail(Long id, Long userId) {
        CatalystDomain domain = catalystRepositoryAdapter.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(CatalystErrorCode.NOT_FOUND));
        return toInfoResponse(domain);
    }

    // UC-4: 삭제
    @Transactional
    public void delete(Long id, Long userId) {
        CatalystDomain domain = catalystRepositoryAdapter.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(CatalystErrorCode.NOT_FOUND));
        domain.delete();
        catalystRepositoryAdapter.persistStatusAndDeletion(domain.getId(), domain.getStatus());
    }

    // UC-5: 기본정보 수정
    @Transactional
    public CatalystUpdateResponse updateBasicInfo(Long id, Long userId, String title, String content,
                                                   List<Long> industryIds) {
        CatalystDomain domain = catalystRepositoryAdapter.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(CatalystErrorCode.NOT_FOUND));

        domain.updateBasicInfo(title, content, industryIds);

        if (!catalystRepositoryAdapter.existsAllIndustries(domain.getIndustryIds())) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }

        LocalDateTime updatedAt = catalystRepositoryAdapter.persistBasicInfo(
                domain.getId(), domain.getTitle(), domain.getContent());
        catalystRepositoryAdapter.replaceIndustries(domain.getId(), domain.getIndustryIds());

        List<String> industries = resolveIndustryNames(domain);
        return new CatalystUpdateResponse(domain.getTitle(), domain.getContent(), industries, updatedAt);
    }

    // UC-6: 모니터링 상태 변경
    @Transactional
    public CatalystStatusResponse changeStatus(Long id, Long userId, String rawTargetStatus) {
        CatalystDomain domain = catalystRepositoryAdapter.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(CatalystErrorCode.NOT_FOUND));

        CatalystStatus target = parseTargetStatus(rawTargetStatus);
        domain.changeStatus(target);

        // TODO(M5): INACTIVE→ACTIVE 전이 시 signalCollectionPort.triggerCollection 즉시 1회 트리거 +
        // search_interval_hours 주기 반복 수집 시작 로직 필요 (시그널 수집 파이프라인 미구현으로 보류)

        LocalDateTime updatedAt = catalystRepositoryAdapter.persistStatus(domain.getId(), domain.getStatus());
        return new CatalystStatusResponse(domain.getStatus().name(), updatedAt);
    }

    private CatalystStatus parseTargetStatus(String rawTargetStatus) {
        try {
            return CatalystStatus.valueOf(rawTargetStatus);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
    }

    private CatalystCreateResponse toCreateResponse(CatalystDomain domain) {
        List<String> industries = resolveIndustryNames(domain);

        return new CatalystCreateResponse(
                domain.getId(),
                domain.getTitle(),
                domain.getContent(),
                domain.getStatus().name(),
                industries,
                domain.getCreatedAt());
    }

    private CatalystInfoResponse toInfoResponse(CatalystDomain domain) {
        List<String> industries = resolveIndustryNames(domain);

        CatalystBasicInfo basicInfo = new CatalystBasicInfo(
                domain.getTitle(),
                domain.getContent(),
                industries,
                domain.getCreatedAt(),
                domain.getUpdatedAt());

        CatalystMonitoringOperation monitoringOperation = new CatalystMonitoringOperation(
                domain.getStatus().name(),
                domain.getSearchConditions(),
                domain.getSearchIntervalHours(),
                domain.getLastSearchedAt(),
                domain.getActivatedAt());

        return new CatalystInfoResponse(basicInfo, monitoringOperation);
    }

    private List<String> resolveIndustryNames(CatalystDomain domain) {
        Map<Long, String> industryNames = catalystRepositoryAdapter.findIndustryNamesByIds(domain.getIndustryIds());
        return domain.getIndustryIds().stream()
                .map(industryNames::get)
                .toList();
    }
}
