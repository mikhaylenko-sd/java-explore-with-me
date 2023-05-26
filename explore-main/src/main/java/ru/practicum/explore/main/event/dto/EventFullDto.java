package ru.practicum.explore.main.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import ru.practicum.explore.main.category.model.Category;
import ru.practicum.explore.main.event.model.Event;
import ru.practicum.explore.main.event.model.Location;
import ru.practicum.explore.main.user.model.User;

import java.time.LocalDateTime;

@Data
public class EventFullDto {
    private Long id;
    private String annotation;

    private Category category;
    private int confirmedRequests;
    private LocalDateTime createdOn;

    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private User initiator;
    private Location location;

    private boolean paid;

    private int participantLimit;

    private boolean available;

    private LocalDateTime publishedOn;

    private boolean requestModeration;

    private Event.State state;

    private String title;
    private Long views;
}
