package com.catail.backend.signal.application;

import com.catail.backend.global.BusinessException;
import com.catail.backend.global.GlobalErrorCode;
import com.catail.backend.signal.db.Signal;
import com.catail.backend.signal.db.SignalRepository;
import com.catail.backend.signal.domain.SignalStatus;
import com.catail.backend.signal.inbound.SignalListItem;
import com.catail.backend.signal.inbound.SignalListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SignalListService {

    private static final Set<SignalStatus> QUERYABLE_STATUSES = Set.of(SignalStatus.PENDING, SignalStatus.EXCLUDED);

    private final CatalystOwnershipValidator catalystOwnershipValidator;
    private final SignalRepository signalRepository;

    @Transactional(readOnly = true)
    public SignalListResponse getList(Long userId, Long catalystId, String rawStatus, String cursor, int size) {
        catalystOwnershipValidator.validate(userId, catalystId);
        SignalStatus status = resolveQueryableStatus(rawStatus);

        Pageable pageable = PageRequest.of(0, size + 1);
        List<Signal> fetched = StringUtils.hasText(cursor)
                ? fetchNextPage(catalystId, status, cursor, pageable)
                : signalRepository.findFirstPage(catalystId, status, pageable);

        boolean hasNext = fetched.size() > size;
        List<Signal> pageItems = hasNext ? fetched.subList(0, size) : fetched;
        String nextCursor = hasNext ? buildNextCursor(pageItems.get(pageItems.size() - 1)) : null;

        List<SignalListItem> items = pageItems.stream().map(SignalListItem::from).toList();
        return new SignalListResponse(items, nextCursor, hasNext);
    }

    private SignalStatus resolveQueryableStatus(String rawStatus) {
        SignalStatus status;
        try {
            status = SignalStatus.valueOf(rawStatus);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
        if (!QUERYABLE_STATUSES.contains(status)) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
        return status;
    }

    private List<Signal> fetchNextPage(Long catalystId, SignalStatus status, String cursor, Pageable pageable) {
        SignalCursor decoded = SignalCursor.decode(cursor);
        return signalRepository.findNextPage(catalystId, status, decoded.createdAt(), decoded.id(), pageable);
    }

    private String buildNextCursor(Signal last) {
        return SignalCursor.encode(last.getCreatedAt(), last.getId());
    }
}
