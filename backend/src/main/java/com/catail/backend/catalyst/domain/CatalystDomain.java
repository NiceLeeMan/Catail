package com.catail.backend.catalyst.domain;

import com.catail.backend.catalyst.DB.CatalystStatus;
import com.catail.backend.global.BusinessException;
import com.catail.backend.global.GlobalErrorCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Getter
public class CatalystDomain {

    private static final int TITLE_MAX_LENGTH = 50;
    private static final int CONTENT_MIN_LENGTH = 50;
    private static final int CONTENT_MAX_LENGTH = 500;
    private static final int INDUSTRY_IDS_MAX_SIZE = 10;
    private static final int DEFAULT_SEARCH_INTERVAL_HOURS = 4;

    private final Long id;
    private final Long userId;
    private final String title;
    private final String content;
    private CatalystStatus status;
    private final List<Long> industryIds;
    private final List<String> searchConditions;
    private final int searchIntervalHours;
    private final LocalDateTime lastSearchedAt;
    private final LocalDateTime activatedAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private CatalystDomain(Long id, Long userId, String title, String content, CatalystStatus status,
                            List<Long> industryIds, List<String> searchConditions, int searchIntervalHours,
                            LocalDateTime lastSearchedAt, LocalDateTime activatedAt,
                            LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.status = status;
        this.industryIds = industryIds;
        this.searchConditions = searchConditions;
        this.searchIntervalHours = searchIntervalHours;
        this.lastSearchedAt = lastSearchedAt;
        this.activatedAt = activatedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static CatalystDomain create(Long userId, String rawTitle, String rawContent,
                                         List<Long> industryIds, String rawStatus) {
        String title = validateTitle(rawTitle);
        String content = validateContent(rawContent);
        List<Long> validIndustryIds = validateIndustryIds(industryIds);
        CatalystStatus status = validateStatus(rawStatus);

        return new CatalystDomain(null, userId, title, content, status,
                validIndustryIds, new ArrayList<>(), DEFAULT_SEARCH_INTERVAL_HOURS, null, null,
                null, null, null);
    }

    public static CatalystDomain reconstruct(Long id, Long userId, String title, String content,
                                              CatalystStatus status, List<Long> industryIds,
                                              List<String> searchConditions, int searchIntervalHours,
                                              LocalDateTime lastSearchedAt, LocalDateTime activatedAt,
                                              LocalDateTime createdAt, LocalDateTime updatedAt,
                                              LocalDateTime deletedAt) {
        return new CatalystDomain(id, userId, title, content, status, industryIds,
                searchConditions, searchIntervalHours, lastSearchedAt, activatedAt,
                createdAt, updatedAt, deletedAt);
    }

    private static String validateTitle(String rawTitle) {
        if (rawTitle == null) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
        String trimmed = rawTitle.trim();
        if (trimmed.isEmpty() || trimmed.length() > TITLE_MAX_LENGTH || containsControlChar(trimmed)) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
        return trimmed;
    }

    private static String validateContent(String rawContent) {
        if (rawContent == null) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
        String trimmed = rawContent.trim();
        if (trimmed.length() < CONTENT_MIN_LENGTH || trimmed.length() > CONTENT_MAX_LENGTH || containsControlChar(trimmed)) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
        return trimmed;
    }

    private static boolean containsControlChar(String value) {
        return value.chars().anyMatch(Character::isISOControl);
    }

    private static List<Long> validateIndustryIds(List<Long> industryIds) {
        if (industryIds == null || industryIds.isEmpty()
                || industryIds.size() > INDUSTRY_IDS_MAX_SIZE
                || new HashSet<>(industryIds).size() != industryIds.size()) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
        return List.copyOf(industryIds);
    }

    private static CatalystStatus validateStatus(String rawStatus) {
        CatalystStatus status;
        try {
            status = CatalystStatus.valueOf(rawStatus);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
        if (status != CatalystStatus.ACTIVE && status != CatalystStatus.INACTIVE) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
        return status;
    }

    public void delete() {
        this.status = CatalystStatus.ENDED;
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.status == CatalystStatus.ACTIVE;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
