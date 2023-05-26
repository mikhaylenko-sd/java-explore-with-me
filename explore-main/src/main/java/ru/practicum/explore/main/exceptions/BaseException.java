package ru.practicum.explore.main.exceptions;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BaseException extends RuntimeException {
    private final String message;
    private final String reason;
    private final LocalDateTime timestamp;

    public BaseException(String message, String reason, LocalDateTime timestamp) {
        this.message = message;
        this.reason = reason;
        this.timestamp = timestamp;
    }
}
