package ru.practicum.explore.main.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RequestListDto {
    private List<RequestDto> confirmedRequests;

    private List<RequestDto> rejectedRequests;
}
