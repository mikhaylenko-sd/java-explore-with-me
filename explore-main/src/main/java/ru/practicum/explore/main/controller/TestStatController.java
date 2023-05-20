package ru.practicum.explore.main.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explore.stats.client.StatsClient;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

@RestController
public class TestStatController {
    private final StatsClient statsClient;

    public TestStatController(StatsClient statsClient) {
        this.statsClient = statsClient;
    }

    @GetMapping(value = {"/events", "/events/{eventId}"})
    public Object getStatTest(@PathVariable(required = false) Long eventId, HttpServletRequest request) {
        statsClient.saveHit("ewm-main-service", URI.create(request.getRequestURI()).toString(), request.getRemoteAddr());
        return null;
    }
}
