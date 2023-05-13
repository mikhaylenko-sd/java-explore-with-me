package ru.practicum.explore.stats.server.mapper;

import org.mapstruct.Mapper;
import ru.practicum.explore.stats.dto.EndpointHitDto;
import ru.practicum.explore.stats.dto.ViewStatsDto;
import ru.practicum.explore.stats.server.entity.EndpointHit;

@Mapper(componentModel = "spring")
public interface StatsMapper {
    EndpointHitDto toEndpointHitDto(EndpointHit endpointHit);

    EndpointHit toEndpointHit(EndpointHitDto endpointHitDto);

    ViewStatsDto toViewStatsDto(EndpointHit endpointHit);
}
