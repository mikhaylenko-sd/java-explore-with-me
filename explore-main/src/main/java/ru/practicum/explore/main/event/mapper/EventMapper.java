package ru.practicum.explore.main.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.explore.main.category.mapper.CategoryMapper;
import ru.practicum.explore.main.event.dto.EventFullDto;
import ru.practicum.explore.main.event.dto.EventShortDto;
import ru.practicum.explore.main.event.dto.UpdateEventAdminRequest;
import ru.practicum.explore.main.event.dto.UpdateEventUserRequest;
import ru.practicum.explore.main.event.model.Event;
import ru.practicum.explore.main.user.mapper.UserMapper;

@Mapper(componentModel = "spring",
        uses = {UserMapper.class, CategoryMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EventMapper {
    EventFullDto toEventFullDto(Event event);

    EventShortDto toEventShortDto(Event event);

    EventShortDto toEventShortDto(EventFullDto eventFullDto);

    @Mapping(target = "state", ignore = true)
    @Mapping(target = "category", ignore = true)
    Event toEvent(UpdateEventUserRequest updateEventUserRequest, @MappingTarget Event event);

    @Mapping(target = "state", ignore = true)
    @Mapping(target = "category", ignore = true)
    Event toEvent(UpdateEventAdminRequest updateEventAdminRequest, @MappingTarget Event stored);
}
