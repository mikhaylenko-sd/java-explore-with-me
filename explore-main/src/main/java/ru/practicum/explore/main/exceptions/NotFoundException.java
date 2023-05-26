package ru.practicum.explore.main.exceptions;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotFoundException extends BaseException {

    public NotFoundException(String massage, String reason, LocalDateTime timestamp) {
        super(massage, reason, timestamp);
    }
}
