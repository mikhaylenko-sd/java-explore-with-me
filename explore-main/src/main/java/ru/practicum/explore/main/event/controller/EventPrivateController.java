package ru.practicum.explore.main.event.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explore.main.event.dto.EventFullDto;
import ru.practicum.explore.main.event.dto.EventShortDto;
import ru.practicum.explore.main.event.dto.NewEventDto;
import ru.practicum.explore.main.event.dto.UpdateEventUserRequest;
import ru.practicum.explore.main.event.service.EventService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/events")
public class EventPrivateController {
    private final EventService eventService;

    public EventPrivateController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<EventFullDto> createEvent(@PathVariable Long userId,
                                                    @RequestBody @Valid NewEventDto newEvent) {
        log.info("Добавление нового события userId={}, event={}", userId, newEvent);
        return new ResponseEntity<>(eventService.createEvent(userId, newEvent), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getAllEventsByUserId(@PathVariable Long userId,
                                                                    @RequestParam(defaultValue = "0") int from,
                                                                    @RequestParam(defaultValue = "10") int size) {
        log.info("Получение событий, добавленных текущим пользователем userId={}, from={}, size={}", userId, from, size);
        return new ResponseEntity<>(eventService.getEventsByUserId(userId, from, size), HttpStatus.OK);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getEventsByUserAndEventId(@PathVariable Long userId,
                                                                  @PathVariable Long eventId) {
        log.info("Получение полной информации о событии, добавленном текущим пользователем userId={}, eventId={}", userId, eventId);
        return new ResponseEntity<>(eventService.getEventsByUserAndEventId(userId, eventId), HttpStatus.OK);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEvent(@PathVariable Long userId,
                                                    @PathVariable Long eventId,
                                                    @RequestBody @Valid UpdateEventUserRequest updateEventUserRequest) {
        log.info("Изменение события, добавленного текущим пользователем userId={}, eventId={}, event={}",
                userId, eventId, updateEventUserRequest);
        return new ResponseEntity<>(eventService.updateEventsByUser(userId, eventId, updateEventUserRequest), HttpStatus.OK);
    }
}
