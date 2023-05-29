package ru.practicum.explore.main.category.dto;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class CategoryDto {
    private Long id;
    @Size(max = 50)
    private String name;
}
