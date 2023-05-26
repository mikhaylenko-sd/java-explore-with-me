package ru.practicum.explore.main.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.explore.main.exceptions.BaseException;
import ru.practicum.explore.main.exceptions.NotFoundException;
import ru.practicum.explore.main.user.dto.UserDto;
import ru.practicum.explore.main.user.mapper.UserMapper;
import ru.practicum.explore.main.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserDto createUser(UserDto user) {
        log.debug("Получен запрос на создание пользователя {}", user.getName());
        if (userRepository.findAll()
                .stream()
                .anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
            throw new BaseException("Имя уже используется", "Не соблюдены условия уникальности имени",
                    LocalDateTime.now());
        }
        return userMapper.toUserDto(userRepository.save(userMapper.toUser(user)));
    }

    public void deleteUser(Long id) {
        log.debug("Получен запрос на удаление пользователя {}", id);
        userRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Пользователь с id" + id + "не найден", "Запрашиваемый объект не найден или не доступен",
                        LocalDateTime.now()));
        userRepository.deleteById(id);
    }

    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        if (ids == null || ids.isEmpty()) {
            log.info("Поиск всех пользователей с пагинацией");
            return userRepository.findAll(pageable).stream()
                    .map(userMapper::toUserDto)
                    .collect(Collectors.toList());
        } else {
            log.info("Поиск указанных пользователей");
            return userRepository.findAllById(ids).stream()
                    .map(userMapper::toUserDto)
                    .collect(Collectors.toList());
        }
    }
}
