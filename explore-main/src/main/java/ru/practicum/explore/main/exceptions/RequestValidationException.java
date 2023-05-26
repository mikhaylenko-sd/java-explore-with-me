package ru.practicum.explore.main.exceptions;

import java.time.LocalDateTime;

public class RequestValidationException extends BaseException {
    public RequestValidationException(String massage, String reason, LocalDateTime timestamp) {
        super(massage, reason, timestamp);
    }
}
