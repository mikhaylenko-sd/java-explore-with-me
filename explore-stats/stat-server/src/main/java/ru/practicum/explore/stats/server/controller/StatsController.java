package ru.practicum.explore.stats.server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explore.stats.dto.EndpointHitDto;
import ru.practicum.explore.stats.server.service.StatsServiceImpl;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping
@Slf4j
//@Validated
public class StatsController {

    private final StatsServiceImpl statsService;

    public StatsController(StatsServiceImpl statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Object> getStats(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                           @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                           @RequestParam(required = false) List<String> uris,
                                           @RequestParam(defaultValue = "false") boolean unique) {
        log.info("Получен запрос к эндпоинту {} /stats?start={}&end={}&uris={}&unique={}", "GET", start, end, uris, unique);
        return new ResponseEntity<>(statsService.getStats(start, end, uris, unique), HttpStatus.OK);
    }

    @PostMapping("/hit")
    public ResponseEntity<Object> saveStats(@RequestBody @Valid EndpointHitDto endpointHitDto) {
        log.info("Получен запрос к эндпоинту {} /hit", "POST");
        return new ResponseEntity<>(statsService.saveStats(endpointHitDto), HttpStatus.CREATED);
    }
}
