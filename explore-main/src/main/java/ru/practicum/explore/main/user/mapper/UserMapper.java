package ru.practicum.explore.main.user.mapper;

import org.mapstruct.Mapper;
import ru.practicum.explore.main.user.dto.UserDto;
import ru.practicum.explore.main.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toUserDto(User user);

    User toUser(UserDto user);
}
