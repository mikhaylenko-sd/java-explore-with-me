package ru.practicum.explore.main.event.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.explore.main.category.model.Category;
import ru.practicum.explore.main.category.repository.CategoryRepository;
import ru.practicum.explore.main.event.dto.EventFullDto;
import ru.practicum.explore.main.event.dto.EventShortDto;
import ru.practicum.explore.main.event.dto.NewEventDto;
import ru.practicum.explore.main.event.dto.UpdateEventAdminRequest;
import ru.practicum.explore.main.event.dto.UpdateEventUserRequest;
import ru.practicum.explore.main.event.mapper.EventMapper;
import ru.practicum.explore.main.event.model.Event;
import ru.practicum.explore.main.event.model.FilterSort;
import ru.practicum.explore.main.event.repository.EventCriteriaRepository;
import ru.practicum.explore.main.event.repository.EventRepository;
import ru.practicum.explore.main.exceptions.BaseException;
import ru.practicum.explore.main.exceptions.NotFoundException;
import ru.practicum.explore.main.rating.dto.EventRatingsDto;
import ru.practicum.explore.main.rating.service.RatingService;
import ru.practicum.explore.main.request.model.Request;
import ru.practicum.explore.main.request.repository.RequestRepository;
import ru.practicum.explore.main.user.model.User;
import ru.practicum.explore.main.user.repository.UserRepository;
import ru.practicum.explore.stats.client.StatsClient;
import ru.practicum.explore.stats.dto.ViewStatsDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.explore.main.event.model.FilterSort.RATING;
import static ru.practicum.explore.main.event.model.FilterSort.VIEWS;

@Slf4j
@Service
public class EventService {
    private final EventRepository eventRepository;
    private final EventCriteriaRepository eventCriteriaRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final RatingService ratingService;
    private final DateTimeFormatter returnedTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final StatsClient statsClient;

    @Autowired
    public EventService(EventRepository eventRepository,
                        EventCriteriaRepository eventCriteriaRepository,
                        UserRepository userRepository,
                        CategoryRepository categoryRepository,
                        RequestRepository requestRepository,
                        EventMapper eventMapper,
                        RatingService ratingService,
                        StatsClient statsClient) {
        this.eventRepository = eventRepository;
        this.eventCriteriaRepository = eventCriteriaRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.requestRepository = requestRepository;
        this.eventMapper = eventMapper;
        this.ratingService = ratingService;
        this.statsClient = statsClient;
    }

    public EventFullDto createEvent(Long userId, NewEventDto newEvent) {
        log.info("Создание нового события userId={}, event={}", userId, newEvent);
        User initiator = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(NotFoundException.NotFoundType.USER, userId));
        Category stored = categoryRepository.findById(newEvent.getCategory()).orElseThrow(() -> new NotFoundException(NotFoundException.NotFoundType.CATEGORY, newEvent.getCategory()));
        Event newEventEntity = creatingNewEvent(newEvent, initiator, stored);
        return eventMapper.toEventFullDto(eventRepository.save(newEventEntity));
    }

    public List<EventShortDto> getEventsByUserId(Long userId, int from, int size) {
        log.info("Получение информации о событиях пользователем userId={}", userId);
        Pageable pageable = PageRequest.of(from / size, size);
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException(NotFoundException.NotFoundType.USER, userId));
        return eventRepository.findAllByInitiatorId(userId, pageable).stream()
                .map(eventMapper::toEventShortDto)
                .map(eventShortDto -> eventMapper.toEventShortDto(ratingService.getEventRatings(eventShortDto.getId()), eventShortDto))
                .collect(Collectors.toList());
    }

    public EventFullDto getEventsByUserAndEventId(Long userId, Long eventId) {
        log.info("Получение информации о событии пользователем");
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException(NotFoundException.NotFoundType.USER, userId));
        Event stored = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(NotFoundException.NotFoundType.EVENT, eventId));
        return eventMapper.toEventFullDto(ratingService.getEventRatings(stored.getId()), eventMapper.toEventFullDto(stored));
    }

    public EventFullDto updateEventsByUser(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        log.info("Обновление события пользователем");
        Event stored = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(NotFoundException.NotFoundType.EVENT, eventId));
        checkUpdateWithStateByUser(userId, updateEventUserRequest, stored);
        return eventMapper.toEventFullDto(eventRepository.save(createUserUpdateEvent(stored, updateEventUserRequest)));
    }

    private void checkUpdateWithStateByUser(Long userId, UpdateEventUserRequest updateEventUserRequest, Event stored) {
        if (!stored.getInitiator().getId().equals(userId)) {
            throw new BaseException("Условия выполнения не соблюдены", "Изменять может только владелец");
        }
        if (stored.getState().equals(Event.State.PUBLISHED)) {
            throw new BaseException("Условия выполнения не соблюдены", "Изменять можно неопубликованные события");
        }
        LocalDateTime borderTime = LocalDateTime.now().plusHours(2);
        if (updateEventUserRequest.getEventDate() != null && updateEventUserRequest.getEventDate().isBefore(borderTime)
                || stored.getEventDate().isBefore(borderTime)) {
            throw new BaseException("Условия выполнения не соблюдены", "Изменять можно события за 2 часа до начала");
        }
        if (stored.getParticipantLimit() == 0) {
            throw new BaseException("Мест нет", "Нет свободных мест в событиии");
        }
    }

    public EventFullDto updateEventsByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("Обновление события eventId={}, event={}", eventId, updateEventAdminRequest);
        Event stored = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(NotFoundException.NotFoundType.EVENT, eventId));
        checkUpdateWithState(stored, updateEventAdminRequest);
        return eventMapper.toEventFullDto(eventRepository.save(createAdminUpdateEvent(stored, updateEventAdminRequest)));
    }

    private void checkUpdateWithState(Event stored, UpdateEventAdminRequest updateEventAdminRequest) {
        if (!Objects.equals(Event.State.PENDING, stored.getState())) {
            throw new BaseException("Условия выполнения не соблюдены", "Изменять можно неопубликованные события");
        }
        if (stored.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new BaseException("Неверно указана дата события", "Дата события не может быть менее чем за 1 час до начала");
        }
        if (updateEventAdminRequest.getEventDate() != null) {
            if (updateEventAdminRequest.getEventDate().isBefore(LocalDateTime.now())) {
                throw new BaseException("Условия выполнения не соблюдены", "Новое время в прошлом");
            }
        }
    }

    public EventFullDto getEventById(Long id, HttpServletRequest request) {
        log.info("Получение информации о событии eventId={}", id);
        Event stored = eventRepository.findByIdAndState(id, Event.State.PUBLISHED);
        if (stored == null) {
            throw new NotFoundException(NotFoundException.NotFoundType.EVENT, id);
        }
        statsClient.saveHit("explore-main", request.getRequestURI(), request.getRemoteAddr());
        stored.setViews(stored.getViews() + 1);
        eventRepository.save(stored);
        EventFullDto eventFullDto = eventMapper.toEventFullDto(stored);
        return preparingFullDtoWithStat(eventFullDto);
    }

    public List<EventFullDto> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd, Boolean onlyAvailable, FilterSort sort,
                                        Integer from, Integer size, HttpServletRequest request) {
        log.info("Получение информации о событиях с фильтрами public");
        statsClient.saveHit("explore-main", request.getRequestURI(), request.getRemoteAddr());
        Sort customSort = Sort.by(Sort.Direction.ASC, "eventDate");
        if (VIEWS == sort) {
            customSort = Sort.by(Sort.Direction.DESC, "views");
        } else if (RATING == sort) {
            customSort = Sort.by(Sort.Direction.DESC, "calculatedRating");
        }
        Pageable pageable = PageRequest.of(from / size, size, customSort);
        log.info("Получение информации о событиях с фильтрами public из репозиория");
        return eventCriteriaRepository.findAllByTextAndCategoryIdInAndPaidAndEventDateBetweenAndAvailable(
                        text, categories, paid, rangeStart, rangeEnd, onlyAvailable, pageable
                ).stream()
                .map(eventMapper::toEventFullDto)
                .map(this::preparingFullDtoWithStat)
                .collect(Collectors.toList());
    }

    public List<EventFullDto> getEventsForAdmin(List<Long> users, List<Event.State> states, List<Long> categories,
                                                LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        log.info("Получение информации о событиях с фильтрами admin");
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));
        log.info("Получение информации о событиях с фильтрами admin из репозиория");

        return eventCriteriaRepository.findAllByInitiatorIdInAndStateInAndCategoryIdInAndEventDateBetween(
                        users, states, categories, rangeStart, rangeEnd, pageable
                ).stream()
                .map(eventMapper::toEventFullDto)
                .map(this::preparingFullDtoWithStat)
                .collect(Collectors.toList());
    }

    private Event createAdminUpdateEvent(Event stored, UpdateEventAdminRequest updateEventAdminRequest) {
        Event updEventWithoutState = eventMapper.toEvent(updateEventAdminRequest, stored);
        if (updateEventAdminRequest.getCategory() != null) {
            Category newCategory = categoryRepository.findById(updateEventAdminRequest.getCategory()).get();
            updEventWithoutState.setCategory(newCategory);
        }
        if (Objects.equals(UpdateEventAdminRequest.State.PUBLISH_EVENT, updateEventAdminRequest.getStateAction())) {
            updEventWithoutState.setState(Event.State.PUBLISHED);
            updEventWithoutState.setPublishedOn(LocalDateTime.now());
        }
        if (Objects.equals(UpdateEventAdminRequest.State.REJECT_EVENT, updateEventAdminRequest.getStateAction())) {
            updEventWithoutState.setState(Event.State.CANCELED);
        }
        return updEventWithoutState;
    }

    private Event createUserUpdateEvent(Event stored, UpdateEventUserRequest updateEventUserRequest) {
        Event updEventWithoutState = eventMapper.toEvent(updateEventUserRequest, stored);
        if (updateEventUserRequest.getCategory() != null) {
            Category newCategory = categoryRepository.findById(updateEventUserRequest.getCategory()).get();
            updEventWithoutState.setCategory(newCategory);
        }
        if (Objects.equals(UpdateEventUserRequest.State.SEND_TO_REVIEW, updateEventUserRequest.getStateAction())) {
            updEventWithoutState.setState(Event.State.PENDING);
        }
        if (Objects.equals(UpdateEventUserRequest.State.CANCEL_REVIEW, updateEventUserRequest.getStateAction())) {
            updEventWithoutState.setState(Event.State.CANCELED);
        }
        return updEventWithoutState;
    }

    private EventFullDto preparingFullDtoWithStat(EventFullDto eventFullDto) {
        List<ViewStatsDto> stat =
                statsClient.getStats(eventFullDto.getCreatedOn().format(returnedTimeFormat),
                        LocalDateTime.now().format(returnedTimeFormat),
                        List.of("/events/" + eventFullDto.getId()), true);
        if (stat.size() > 0) {
            eventFullDto.setViews(stat.get(0).getHits());
        }
        List<Request> confirmedRequests = requestRepository.findAllByStatusAndEventId(Request.RequestStatus.CONFIRMED,
                eventFullDto.getId());
        eventFullDto.setConfirmedRequests(confirmedRequests.size());
        EventRatingsDto eventRatingsDto = ratingService.getEventRatings(eventFullDto.getId());
        return eventMapper.toEventFullDto(eventRatingsDto, eventFullDto);
    }

    private Event creatingNewEvent(NewEventDto newEvent, User user, Category category) {
        return new Event(null, newEvent.getAnnotation(), category, LocalDateTime.now(), newEvent.getDescription(),
                newEvent.getEventDate(), user, newEvent.getLocation(), newEvent.getPaid(), newEvent.getParticipantLimit(),
                true, null, newEvent.getRequestModeration(), Event.State.PENDING,
                newEvent.getTitle(), 0L, 0L);
    }
}
