package ru.practicum.explore.main.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import ru.practicum.explore.main.request.model.Request;

import java.time.LocalDateTime;

@Data
public class RequestDto {
    private Long id;
    private Long event;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;
    private Long requester;
    private Request.RequestStatus status;
}
