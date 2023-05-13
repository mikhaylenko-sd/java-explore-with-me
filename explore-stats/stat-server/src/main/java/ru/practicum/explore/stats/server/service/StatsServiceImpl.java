package ru.practicum.explore.stats.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.explore.stats.dto.EndpointHitDto;
import ru.practicum.explore.stats.dto.ViewStatsDto;
import ru.practicum.explore.stats.server.entity.EndpointHit;
import ru.practicum.explore.stats.server.mapper.StatsMapper;
import ru.practicum.explore.stats.server.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatsServiceImpl implements StatsService {

    private final StatsMapper statsMapper;
    private final StatsRepository statsRepository;

    @Autowired
    public StatsServiceImpl(StatsMapper statsMapper, StatsRepository statsRepository) {
        this.statsMapper = statsMapper;
        this.statsRepository = statsRepository;
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        List<EndpointHit> endpointHits;

        if (uris == null || uris.isEmpty()) {
            if (!unique) {
                endpointHits = statsRepository.findByTime(start, end);
            } else {
                endpointHits = statsRepository.findByUniqueIpAndTime(start, end);
            }
        } else {
            if (!unique) {
                endpointHits = statsRepository.findByUriAndTime(uris, start, end);
            } else {
                endpointHits = statsRepository.findByUriAndUniqueIpAndTime(uris, start, end);
            }
        }

        return endpointHits.stream()
                .map(statsMapper::toViewStatsDto)
                .sorted((view1, view2) -> -Long.compare(view1.getHits(), view2.getHits()))
                .collect(Collectors.toList());
    }

    @Override
    public EndpointHitDto saveStats(EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = statsMapper.toEndpointHit(endpointHitDto);
        return statsMapper.toEndpointHitDto(statsRepository.save(endpointHit));
    }
}
