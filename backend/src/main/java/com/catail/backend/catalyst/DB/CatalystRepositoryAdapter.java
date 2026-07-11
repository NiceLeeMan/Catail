package com.catail.backend.catalyst.DB;

import com.catail.backend.catalyst.domain.CatalystDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CatalystRepositoryAdapter {

    private final CatalystRepository catalystRepository;
    private final CatalystIndustryRepository catalystIndustryRepository;
    private final IndustryRepository industryRepository;

    public CatalystDomain save(CatalystDomain domain) {
        Catalyst entity = CatalystMapper.toNewEntity(domain);
        Catalyst saved = catalystRepository.save(entity);

        List<CatalystIndustry> joins = domain.getIndustryIds().stream()
                .map(industryId -> {
                    CatalystIndustry join = new CatalystIndustry();
                    join.setCatalystId(saved.getId());
                    join.setIndustryId(industryId);
                    return join;
                })
                .toList();
        catalystIndustryRepository.saveAll(joins);

        return CatalystMapper.toDomain(saved, domain.getIndustryIds());
    }

    public Optional<CatalystDomain> findByIdAndUserId(Long id, Long userId) {
        return catalystRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .map(entity -> CatalystMapper.toDomain(entity, industryIdsOf(entity.getId())));
    }

    public Page<CatalystDomain> findPageByUserId(Long userId, Pageable pageable) {
        Page<Catalyst> page = catalystRepository.findByUserIdAndDeletedAtIsNull(userId, pageable);

        List<Long> catalystIds = page.getContent().stream().map(Catalyst::getId).toList();
        Map<Long, List<Long>> industryIdsByCatalyst = catalystIndustryRepository.findByCatalystIdIn(catalystIds).stream()
                .collect(Collectors.groupingBy(CatalystIndustry::getCatalystId,
                        Collectors.mapping(CatalystIndustry::getIndustryId, Collectors.toList())));

        return page.map(entity -> CatalystMapper.toDomain(entity,
                industryIdsByCatalyst.getOrDefault(entity.getId(), List.of())));
    }

    public boolean existsAllIndustries(List<Long> industryIds) {
        return industryRepository.findAllById(industryIds).size() == industryIds.size();
    }

    public Map<Long, String> findIndustryNamesByIds(List<Long> ids) {
        return industryRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Industry::getId, Industry::getName));
    }

    // TODO(M6): SELECT catalyst_id, COUNT(*) FROM signal WHERE status='PENDING' AND catalyst_id IN (:ids) GROUP BY catalyst_id
    public Map<Long, Integer> countPendingSignalsByCatalystIds(List<Long> catalystIds) {
        return catalystIds.stream().collect(Collectors.toMap(id -> id, id -> 0));
    }

    public void persistStatusAndDeletion(Long catalystId, CatalystStatus newStatus) {
        Catalyst entity = catalystRepository.findById(catalystId)
                .orElseThrow(() -> new IllegalStateException("Catalyst not found: " + catalystId));
        entity.setStatus(newStatus);
        entity.delete();
    }

    public LocalDateTime persistBasicInfo(Long catalystId, String title, String content) {
        Catalyst entity = catalystRepository.findById(catalystId)
                .orElseThrow(() -> new IllegalStateException("Catalyst not found: " + catalystId));
        entity.setTitle(title);
        entity.setContent(content);
        return catalystRepository.saveAndFlush(entity).getUpdatedAt();
    }

    public void replaceIndustries(Long catalystId, List<Long> industryIds) {
        catalystIndustryRepository.deleteByCatalystId(catalystId);

        List<CatalystIndustry> joins = industryIds.stream()
                .map(industryId -> {
                    CatalystIndustry join = new CatalystIndustry();
                    join.setCatalystId(catalystId);
                    join.setIndustryId(industryId);
                    return join;
                })
                .toList();
        catalystIndustryRepository.saveAll(joins);
    }

    private List<Long> industryIdsOf(Long catalystId) {
        return catalystIndustryRepository.findByCatalystId(catalystId).stream()
                .map(CatalystIndustry::getIndustryId)
                .toList();
    }
}
