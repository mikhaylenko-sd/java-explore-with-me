package ru.practicum.explore.main.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explore.main.category.mapper.CategoryMapper;
import ru.practicum.explore.main.event.mapper.EventMapper;
import ru.practicum.explore.main.request.dto.RequestDto;
import ru.practicum.explore.main.request.model.Request;
import ru.practicum.explore.main.user.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CategoryMapper.class, EventMapper.class})
public interface RequestMapper {
    @Mapping(source = "requester.id", target = "requester")
    @Mapping(source = "event.id", target = "event")
    RequestDto toRequestDto(Request request);
}
