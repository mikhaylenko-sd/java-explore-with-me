package ru.practicum.explore.main.exceptions;

import lombok.Getter;

@Getter
public class NotFoundException extends BaseException {
    public enum NotFoundType {
        CATEGORY,
        USER,
        COMPILATION,
        REQUEST,
        EVENT
    }

    public NotFoundException(NotFoundType notFoundType, Long id) {
        super(null, "Запрашиваемый объект не найден или не доступен");

        String errorMessageTemplate = "%s с id=" + id + " не найден%s";
        switch (notFoundType) {
            case CATEGORY:
                errorMessageTemplate = String.format(errorMessageTemplate, "Категория", "а");
                break;
            case USER:
                errorMessageTemplate = String.format(errorMessageTemplate, "Пользователь", "");
                break;
            case COMPILATION:
                errorMessageTemplate = String.format(errorMessageTemplate, "Подборка", "а");
                break;
            case REQUEST:
                errorMessageTemplate = String.format(errorMessageTemplate, "Запрос", "");
                break;
            case EVENT:
                errorMessageTemplate = String.format(errorMessageTemplate, "Событие", "о");
                break;
        }
        message = errorMessageTemplate;
    }
}
