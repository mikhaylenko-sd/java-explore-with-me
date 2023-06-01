package ru.practicum.explore.main.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import ru.practicum.explore.main.category.model.Category;
import ru.practicum.explore.main.user.dto.UserDto;
import ru.practicum.explore.main.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventShortDto {
    private Long id;
    private String annotation;

    private Category category;

    private LocalDateTime createdOn;

    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private User initiator;

    private boolean paid;

    private boolean available;

    private String title;

    private Long calculatedRating;

    private List<UserDto> likes;

    private List<UserDto> dislikes;
}
