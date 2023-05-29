package ru.practicum.explore.main.request.controller;

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
import ru.practicum.explore.main.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explore.main.request.dto.RequestDto;
import ru.practicum.explore.main.request.dto.RequestListDto;
import ru.practicum.explore.main.request.service.RequestService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}")
public class RequestPrivateController {
    private final RequestService requestService;

    public RequestPrivateController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping("/events/{eventId}/requests")
    public ResponseEntity<List<RequestDto>> getRequestsByUserIdAndEventId(@PathVariable Long eventId, @PathVariable Long userId) {
        log.info("Получение информации о запросах на участие в событии текущего пользователя eventId={}, userId={}",
                eventId, userId);
        return new ResponseEntity<>(requestService.getAllRequestsByEventId(eventId, userId), HttpStatus.OK);
    }

    @PatchMapping("/events/{eventId}/requests")
    public ResponseEntity<RequestListDto> patchRequest(@PathVariable Long userId, @PathVariable Long eventId,
                                                       @RequestBody @Valid EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        log.info("Изменение статуса (подтверждена, отменена) заявок на участие в событии текущего пользователя eventId={}, userId={}, request={}",
                eventId, userId, eventRequestStatusUpdateRequest);
        return new ResponseEntity<>(requestService.updateRequestsStatusForEvent(eventId, userId, eventRequestStatusUpdateRequest), HttpStatus.OK);
    }

    @GetMapping("/requests")
    public ResponseEntity<List<RequestDto>> getAllRequestsForUserId(@PathVariable Long userId) {
        log.info("Получение информации о заявках текущего пользователя на участие в чужих событиях userId={}", userId);
        return new ResponseEntity<>(requestService.getAllRequestsForUser(userId), HttpStatus.OK);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public ResponseEntity<RequestDto> patchRequestsStateCancel(@PathVariable Long userId, @PathVariable Long requestId) {
        log.info("Отмена своего запроса на участие в событии userId={}", userId);
        return new ResponseEntity<>(requestService.updCancelStatus(userId, requestId), HttpStatus.OK);
    }

    @PostMapping("/requests")
    public ResponseEntity<RequestDto> createRequestForEvent(@PathVariable Long userId,
                                                            @RequestParam Long eventId) {
        log.info("Добавление запроса от текущего пользователя на участие в событии userId={}, eventId={}", userId, eventId);
        return new ResponseEntity<>(requestService.createRequest(userId, eventId), HttpStatus.CREATED);
    }
}
