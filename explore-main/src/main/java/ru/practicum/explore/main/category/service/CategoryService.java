package ru.practicum.explore.main.category.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.explore.main.category.dto.CategoryDto;
import ru.practicum.explore.main.category.dto.NewCategoryDto;
import ru.practicum.explore.main.category.mapper.CategoryMapper;
import ru.practicum.explore.main.category.model.Category;
import ru.practicum.explore.main.category.repository.CategoryRepository;
import ru.practicum.explore.main.exceptions.BaseException;
import ru.practicum.explore.main.exceptions.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    public CategoryDto createCategory(NewCategoryDto category) {
        log.debug("Получен запрос на создание категории {}", category.getName());
        if (categoryRepository.findAll()
                .stream()
                .anyMatch(c -> c.getName().equals(category.getName()))) {
            throw new BaseException("Имя уже используется", "Не соблюдены условия уникальности имени",
                    LocalDateTime.now());
        }
        return categoryMapper.toCategoryDto(categoryRepository.save(categoryMapper.toCategory(category)));
    }

    public void deleteCategory(Long id) {
        log.debug("Получен запрос на удаление категории {}", id);
        categoryRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Категория с id" + id + "не найдена", "Запрашиваемый объект не найден или не доступен",
                        LocalDateTime.now()));
        try {
            categoryRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new BaseException(
                    "Условия выполнения не соблюдены",
                    "Удалять можно только непривязанную категорию",
                    LocalDateTime.now());
        }
    }

    public CategoryDto update(Long id, CategoryDto updatingDto) {
        log.debug("Получен запрос обновления категории пользователем с id {}", id);
        Category stored = categoryRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Категория с id" + id + "не найдена", "Запрашиваемый объект не найден или не доступен",
                        LocalDateTime.now()));
        checkNameForUniq(id, updatingDto);
        categoryMapper.toCategory(updatingDto, stored);
        Category actualCategory = categoryRepository.save(stored);
        return categoryMapper.toCategoryDto(actualCategory);
    }

    public CategoryDto getCategoryById(Long id) {
        log.debug("Получен запрос на получение категории {}", id);
        Category stored = categoryRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Категория с id" + id + "не найдена", "Запрашиваемый объект не найден или не доступен",
                        LocalDateTime.now()));
        return categoryMapper.toCategoryDto(stored);
    }

    public List<CategoryDto> getCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        log.info("Поиск всех категорий с пагинацией");
        return categoryRepository.findAll(pageable)
                .stream()
                .map(categoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    private void checkNameForUniq(Long id, CategoryDto updatingDto) {
        if (StringUtils.isNotBlank(updatingDto.getName()) && categoryRepository.findAll().stream()
                .anyMatch(u -> u.getName().equals(updatingDto.getName()) && !Objects.equals(u.getId(), id))) {
            throw new BaseException("Имя категории уже используется", "Не соблюдены условия уникальности имени",
                    LocalDateTime.now());
        }
    }
}
