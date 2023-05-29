package ru.practicum.explore.main.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    private static final String ERROR_MESSAGE = "Во время работы программы произошла ошибка {}";

    @ExceptionHandler(RequestValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(RequestValidationException exception) {
        log.error(ERROR_MESSAGE, exception.getMessage());
        return new ApiError(exception.getMessage(), exception.getReason(), "BAD_REQUEST", exception.getTimestamp().toString());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException exception) {
        log.error(ERROR_MESSAGE, exception.getMessage());
        return new ApiError(exception.getMessage(), exception.getReason(), "NOT_FOUND", exception.getTimestamp().toString());
    }

    @ExceptionHandler(BaseException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(BaseException exception) {
        log.error(ERROR_MESSAGE, exception.getMessage());
        return new ApiError(exception.getMessage(), exception.getReason(), "CONFLICT", exception.getTimestamp().toString());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleSystemExceptions(IllegalArgumentException exception) {
        log.error(ERROR_MESSAGE, exception.getMessage());
        return new ErrorResponse(exception.getMessage());
    }
}
