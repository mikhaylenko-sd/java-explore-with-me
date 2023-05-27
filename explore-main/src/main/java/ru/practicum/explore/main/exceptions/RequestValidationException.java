package ru.practicum.explore.main.exceptions;

public class RequestValidationException extends BaseException {
    public RequestValidationException(String message, String reason) {
        super(message, reason);
    }
}
