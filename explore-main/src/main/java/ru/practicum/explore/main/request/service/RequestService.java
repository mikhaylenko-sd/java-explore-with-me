package ru.practicum.explore.main.request.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.explore.main.event.model.Event;
import ru.practicum.explore.main.event.repository.EventRepository;
import ru.practicum.explore.main.exceptions.BaseException;
import ru.practicum.explore.main.exceptions.NotFoundException;
import ru.practicum.explore.main.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explore.main.request.dto.RequestDto;
import ru.practicum.explore.main.request.dto.RequestListDto;
import ru.practicum.explore.main.request.mapper.RequestMapper;
import ru.practicum.explore.main.request.model.Request;
import ru.practicum.explore.main.request.repository.RequestRepository;
import ru.practicum.explore.main.user.model.User;
import ru.practicum.explore.main.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RequestService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;

    public RequestService(EventRepository eventRepository,
                          UserRepository userRepository,
                          RequestRepository requestRepository,
                          RequestMapper requestMapper) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.requestMapper = requestMapper;
    }

    public RequestDto createRequest(Long userId, Long eventId) {
        log.info("Создание запроса на участие пользователя в событии userId={}, eventId={}", userId, eventId);
        Event stored = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(NotFoundException.NotFoundType.EVENT, eventId));
        List<Request> alreadyExistsRequests = requestRepository.findAllByRequesterIdAndEventId(userId, eventId);
        List<Request> confirmedRequestsByEvent = requestRepository.findAllByStatusAndEventId(Request.RequestStatus.CONFIRMED, eventId);
        checkRequest(userId, stored, alreadyExistsRequests, confirmedRequestsByEvent);
        return requestMapper.toRequestDto(requestRepository.save(creatingRequest(userId, stored)));
    }

    public RequestDto updCancelStatus(Long userId, Long requestId) {
        log.info("Получен запрос на изменение статуса запроса на участие пользователя userId={}, requestId={}",
                userId, requestId);
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException(NotFoundException.NotFoundType.USER, userId));
        Request requestStored = requestRepository.findById(requestId).orElseThrow(() -> new NotFoundException(NotFoundException.NotFoundType.REQUEST, requestId));
        requestStored.setStatus(Request.RequestStatus.CANCELED);
        return requestMapper.toRequestDto(requestRepository.save(requestStored));
    }

    public List<RequestDto> getAllRequestsForUser(Long userId) {
        log.info("Получение всех запросов пользователя userId={}", userId);
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException(NotFoundException.NotFoundType.USER, userId));
        List<Request> storedRequests = requestRepository.findAllByRequesterId(userId);
        return storedRequests
                .stream()
                .map(requestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    public List<RequestDto> getAllRequestsByEventId(Long eventId, Long userId) {
        log.info("Получение всех запросов на участие пользователя userId={}, eventId={}", userId, eventId);
        eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(NotFoundException.NotFoundType.EVENT, eventId));
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException(NotFoundException.NotFoundType.USER, userId));
        return requestRepository.findAllByEventId(eventId).stream()
                .map(requestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    public RequestListDto updateRequestsStatusForEvent(Long eventId, Long userId, EventRequestStatusUpdateRequest request) {
        log.info("Обновлени запроса пользователя userId={}, eventId={}, request={}", userId, eventId, request);
        Event storedEvent = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(NotFoundException.NotFoundType.EVENT, eventId));
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException(NotFoundException.NotFoundType.USER, userId));
        List<Request> requestsForUpdate = requestRepository.findAllByEventIdAndIdIsIn(eventId, request.getRequestIds());
        checkRequestsListForUpdate(request.getStatus(), storedEvent, requestsForUpdate);
        eventRepository.save(storedEvent);
        return createRequestListDto(request.getRequestIds());
    }

    private RequestListDto createRequestListDto(List<Long> idRequests) {
        List<RequestDto> confirmedRequests = requestRepository.findAllByStatusAndIdIsIn(Request.RequestStatus.CONFIRMED,
                        idRequests)
                .stream()
                .map(requestMapper::toRequestDto)
                .collect(Collectors.toList());
        List<RequestDto> rejectedRequests = requestRepository.findAllByStatusAndIdIsIn(Request.RequestStatus.REJECTED,
                        idRequests)
                .stream()
                .map(requestMapper::toRequestDto)
                .collect(Collectors.toList());
        return new RequestListDto(confirmedRequests, rejectedRequests);
    }

    private void checkRequestsListForUpdate(Request.RequestStatus newStatus,
                                            Event storedEvent, List<Request> requestsForUpdate) {
        for (Request request : requestsForUpdate) {
            List<Request> confirmedRequestByEvent = requestRepository.findAllByStatusAndEventId(Request.RequestStatus.CONFIRMED, storedEvent.getId());
            if (storedEvent.getParticipantLimit() == confirmedRequestByEvent.size()) {
                request.setStatus(Request.RequestStatus.REJECTED);
                requestRepository.save(request);
                throw new BaseException("Мест нет", "Нет свободных мест в событии");
            }
            if (!request.getStatus().equals(Request.RequestStatus.PENDING)) {
                throw new BaseException("Запрос не в ожидании", "Обновление возможно для статсуса" + Request.RequestStatus.PENDING);
            }
            if (newStatus.equals(Request.RequestStatus.CONFIRMED)) {
                request.setStatus(Request.RequestStatus.CONFIRMED);
                requestRepository.save(request);
            }
            if (newStatus.equals(Request.RequestStatus.REJECTED)) {
                request.setStatus(Request.RequestStatus.REJECTED);
                requestRepository.save(request);
            }
        }
    }

    private void checkRequest(long userId, Event stored, List<Request> alreadyExistsRequests, List<Request> confirmedRequestsByEvent) {
        if (alreadyExistsRequests.size() != 0) {
            throw new BaseException("Попытка повторного запроса", "Нельзя повторно отправлять запрос на участие");
        }
        if (confirmedRequestsByEvent.size() >= stored.getParticipantLimit() && stored.getParticipantLimit() > 0) {
            throw new BaseException("На данном событии уже достигнут лимит участников", "Нельзя записаться на событие, так нет свободных мест");
        }
        if (stored.getInitiator().getId() == userId) {
            throw new BaseException("Вы инициатор", "Нельзя ходить на свои мероприятия как гость");
        }
        if (!stored.getState().equals(Event.State.PUBLISHED)) {
            throw new BaseException("Событие не опубликовано", "Нельзя подать запрос на неопубликованное событие");
        }
    }

    private Request creatingRequest(Long userId, Event stored) {
        Request request = new Request();
        if (!stored.isRequestModeration()) {
            request.setStatus(Request.RequestStatus.CONFIRMED);
        } else {
            request.setStatus(Request.RequestStatus.PENDING);
        }
        if (stored.getParticipantLimit() == 0) {
            request.setStatus(Request.RequestStatus.CONFIRMED);
        }
        User requester = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(NotFoundException.NotFoundType.USER, userId));
        request.setRequester(requester);
        request.setEvent(stored);
        request.setCreated(LocalDateTime.now());
        return request;
    }
}
