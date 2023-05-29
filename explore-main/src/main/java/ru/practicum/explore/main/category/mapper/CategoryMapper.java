package ru.practicum.explore.main.category.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.explore.main.category.dto.CategoryDto;
import ru.practicum.explore.main.category.dto.NewCategoryDto;
import ru.practicum.explore.main.category.model.Category;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CategoryMapper {
    CategoryDto toCategoryDto(Category category);

    Category toCategory(NewCategoryDto categoryDto);

    Category toCategory(CategoryDto categoryDto);

    @Mapping(target = "id", ignore = true)
    void toCategory(CategoryDto updatingDto, @MappingTarget Category stored);
}
