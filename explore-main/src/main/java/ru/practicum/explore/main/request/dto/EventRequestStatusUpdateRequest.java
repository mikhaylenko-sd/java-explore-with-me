package ru.practicum.explore.main.request.dto;

import lombok.Data;
import ru.practicum.explore.main.request.model.Request;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private Request.RequestStatus status;
}
