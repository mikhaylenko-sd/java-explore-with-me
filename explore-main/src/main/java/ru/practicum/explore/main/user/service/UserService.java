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
        log.info("Создание нового пользователя user={}", user);
        boolean isExist = userRepository.findAll()
                .stream()
                .anyMatch(u -> u.getEmail().equals(user.getEmail()));
        if (isExist) {
            throw new BaseException("Имя уже используется", "Не соблюдены условия уникальности имени");
        }
        return userMapper.toUserDto(userRepository.save(userMapper.toUser(user)));
    }

    public void deleteUser(Long id) {
        log.info("Удаление пользователя userId={}", id);
        userRepository.findById(id).orElseThrow(() -> new NotFoundException(NotFoundException.NotFoundType.USER, id));
        userRepository.deleteById(id);
    }

    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        if (ids == null || ids.isEmpty()) {
            log.info("Поиск всех пользователей с пагинацией, page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
            return userRepository.findAll(pageable).stream()
                    .map(userMapper::toUserDto)
                    .collect(Collectors.toList());
        } else {
            log.info("Поиск указанных пользователей по id={}", ids);
            return userRepository.findAllById(ids).stream()
                    .map(userMapper::toUserDto)
                    .collect(Collectors.toList());
        }
    }
}
