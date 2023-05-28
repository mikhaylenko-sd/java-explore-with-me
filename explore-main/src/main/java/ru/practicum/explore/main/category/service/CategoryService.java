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

    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        log.info("Создание новой категории name={}", newCategoryDto.getName());
        checkNameForUniq(null, newCategoryDto.getName());
        return categoryMapper.toCategoryDto(categoryRepository.save(categoryMapper.toCategory(newCategoryDto)));
    }

    public void deleteCategory(Long id) {
        log.info("Удаление категории id={}", id);
        getCategoryById(id);
        try {
            categoryRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new BaseException("Условия выполнения не соблюдены", "Удалять можно только непривязанную категорию");
        }
    }

    public CategoryDto update(Long id, CategoryDto updatingDto) {
        log.info("Обновление категории пользователем с id={}, category={}", id, updatingDto);
        CategoryDto storedDto = getCategoryById(id);
        checkNameForUniq(id, updatingDto.getName());
        Category stored = categoryMapper.toCategory(storedDto);
        categoryMapper.toCategory(updatingDto, stored);
        Category actualCategory = categoryRepository.save(stored);
        return categoryMapper.toCategoryDto(actualCategory);
    }

    public CategoryDto getCategoryById(Long id) {
        log.info("Получение категории id={}", id);
        Category stored = categoryRepository.findById(id).orElseThrow(() ->
                new NotFoundException(NotFoundException.NotFoundType.CATEGORY, id)
        );
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

    private void checkNameForUniq(Long id, String categoryName) {
        if (StringUtils.isBlank(categoryName)) {
            return;
        }
        boolean isDuplicate = categoryRepository.findAll().stream()
                .anyMatch(u -> u.getName().equals(categoryName) && !Objects.equals(u.getId(), id));

        if (isDuplicate) {
            throw new BaseException("Имя категории уже используется", "Не соблюдены условия уникальности имени");
        }
    }
}
