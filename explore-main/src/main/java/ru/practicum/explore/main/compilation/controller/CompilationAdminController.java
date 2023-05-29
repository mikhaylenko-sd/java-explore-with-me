package ru.practicum.explore.main.compilation.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explore.main.compilation.dto.CompilationDto;
import ru.practicum.explore.main.compilation.dto.NewCompilationDto;
import ru.practicum.explore.main.compilation.dto.UpdateCompilationRequest;
import ru.practicum.explore.main.compilation.service.CompilationService;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping(path = "/admin/compilations")
public class CompilationAdminController {
    private final CompilationService compilationService;

    public CompilationAdminController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @PostMapping
    public ResponseEntity<CompilationDto> createCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        log.info("Добавление новой подборки compilation={}", newCompilationDto);
        return new ResponseEntity<>(compilationService.createCompilation(newCompilationDto), HttpStatus.CREATED);
    }

    @PatchMapping("/{compilationId}")
    public ResponseEntity<CompilationDto> patchCompilation(@PathVariable Long compilationId,
                                                           @RequestBody @Valid UpdateCompilationRequest updateCompilationRequest) {
        log.info("Обновление информации о подборке id={}, compilation={}", compilationId, updateCompilationRequest);
        return new ResponseEntity<>(compilationService.updateCompilation(compilationId, updateCompilationRequest), HttpStatus.OK);
    }

    @DeleteMapping("/{compilationId}")
    public ResponseEntity<Boolean> deleteCompilation(@PathVariable Long compilationId) {
        log.info("Удаление подборки id={}", compilationId);
        compilationService.deleteCompilationById(compilationId);
        return new ResponseEntity<>(true, HttpStatus.NO_CONTENT);
    }
}
