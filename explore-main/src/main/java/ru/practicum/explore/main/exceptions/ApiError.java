package ru.practicum.explore.main.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiError {
    private final String message;
    private final String reason;
    private final String status;
    private final String timestamp;
}
