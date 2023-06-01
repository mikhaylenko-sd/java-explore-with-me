package ru.practicum.explore.main.rating.dto;

import lombok.Data;
import ru.practicum.explore.main.user.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

@Data
public class EventRatingsDto {
    private Long eventId;

    private List<UserDto> likes = new ArrayList<>();

    private List<UserDto> dislikes = new ArrayList<>();
}
