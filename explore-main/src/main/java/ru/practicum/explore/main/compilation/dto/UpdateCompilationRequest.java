package ru.practicum.explore.main.compilation.dto;

import lombok.Data;

import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Data
public class UpdateCompilationRequest {
    @Size(max = 50)
    private String title;
    private List<Long> events = new ArrayList<>();
    private Boolean pinned;
}
