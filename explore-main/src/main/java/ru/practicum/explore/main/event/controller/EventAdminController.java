package ru.practicum.explore.main.event.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explore.main.event.dto.EventFullDto;
import ru.practicum.explore.main.event.dto.UpdateEventAdminRequest;
import ru.practicum.explore.main.event.model.Event;
import ru.practicum.explore.main.event.service.EventService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/admin/events")
public class EventAdminController {
    private final EventService eventService;

    public EventAdminController(EventService eventService) {
        this.eventService = eventService;
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> patchEvent(@PathVariable Long eventId, @RequestBody @Valid UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("Редактирование данных события и его статуса id={}, event={}", eventId, updateEventAdminRequest);
        return new ResponseEntity<>(eventService.updateEventsByAdmin(eventId, updateEventAdminRequest), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<EventFullDto>> getAllEvents(@RequestParam(required = false) List<Long> users,
                                                           @RequestParam(required = false) List<Event.State> states,
                                                           @RequestParam(required = false) List<Long> categories,
                                                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                           @RequestParam(defaultValue = "0") Integer from,
                                                           @RequestParam(defaultValue = "10") Integer size) {
        log.info("Поиск событий по параметрам users={}, states={}, categories={}, rangeStart={}, rangeEnd={}, from={}, size={}",
                users, states, categories, rangeStart, rangeEnd, from, size);
        return new ResponseEntity<>(eventService.getEventsForAdmin(users, states, categories, rangeStart, rangeEnd, from, size), HttpStatus.OK);
    }
}
