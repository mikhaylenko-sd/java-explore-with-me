package ru.practicum.explore.main.compilation.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.explore.main.compilation.dto.CompilationDto;
import ru.practicum.explore.main.compilation.dto.NewCompilationDto;
import ru.practicum.explore.main.compilation.dto.UpdateCompilationRequest;
import ru.practicum.explore.main.compilation.model.Compilation;
import ru.practicum.explore.main.compilation.repository.CompilationRepository;
import ru.practicum.explore.main.event.dto.EventFullDto;
import ru.practicum.explore.main.event.dto.EventShortDto;
import ru.practicum.explore.main.event.mapper.EventMapper;
import ru.practicum.explore.main.event.model.Event;
import ru.practicum.explore.main.event.repository.EventRepository;
import ru.practicum.explore.main.exceptions.NotFoundException;
import ru.practicum.explore.main.exceptions.RequestValidationException;
import ru.practicum.explore.main.rating.service.RatingService;
import ru.practicum.explore.main.request.model.Request;
import ru.practicum.explore.main.request.repository.RequestRepository;
import ru.practicum.explore.stats.client.StatsClient;
import ru.practicum.explore.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class CompilationService {
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final CompilationRepository compilationRepository;
    private final EventMapper eventMapper;
    private final RatingService ratingService;
    private final StatsClient statsClient;

    public void deleteCompilationById(Long compId) {
        log.info("Удаление подборки id={}", compId);
        getCompilationById(compId);
        compilationRepository.deleteById(compId);
    }

    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        log.info("Создание новой подборки compilation={}", newCompilationDto);
        if (newCompilationDto.getTitle() == null) {
            throw new RequestValidationException("Пустой заголовок", "Заголовок не может быть пустым");
        }
        List<Event> storedEvents = eventRepository.findAllByIdIsIn(newCompilationDto.getEvents());
        Compilation compilation = new Compilation(null, storedEvents, newCompilationDto.isPinned(), newCompilationDto.getTitle());
        Compilation saved = compilationRepository.save(compilation);
        return createCompilationDto(saved);
    }

    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        log.info("Обновление подборки id={}, compilation={}", compId, updateCompilationRequest);
        Compilation compilation = getCompilationById(compId);
        Compilation newCompilation = createCompilationForUpdate(compilation, updateCompilationRequest);
        compilationRepository.save(newCompilation);
        return createCompilationDto(newCompilation);
    }


    public CompilationDto getCompilationDtoById(Long compId) {
        log.info("Получение подборки по id={}", compId);
        Compilation compilation = getCompilationById(compId);
        return createCompilationDto(compilation);
    }

    private Compilation getCompilationById(Long compId) {
        log.info("Поиск подборки в БД по id={}", compId);
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(NotFoundException.NotFoundType.COMPILATION, compId));
    }

    public List<CompilationDto> getAllCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Получение всех подборок с пагинацией и привязкой={}", pinned);
        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));

        Page<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(pageRequest);
        } else {
            compilations = compilationRepository.findAllByPinned(pinned, pageRequest);
        }
        return compilations.stream()
                .map(this::createCompilationDto)
                .collect(Collectors.toList());
    }

    private Compilation createCompilationForUpdate(Compilation stored, UpdateCompilationRequest updateCompilationRequest) {
        if (updateCompilationRequest.getPinned() != null) {
            stored.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getTitle() != null) {
            stored.setTitle(updateCompilationRequest.getTitle());
        }
        if (updateCompilationRequest.getEvents() != null) {
            stored.setEvents(eventRepository.findAllByIdIsIn(updateCompilationRequest.getEvents()));
        }
        return stored;
    }

    private CompilationDto createCompilationDto(Compilation compilation) {
        List<EventShortDto> eventShortDtos = compilation.getEvents()
                .stream()
                .map(eventMapper::toEventFullDto)
                .map(this::preparingFullDtoWithStat)
                .collect(Collectors.toList());
        return new CompilationDto(eventShortDtos, compilation.getId(), compilation.isPinned(), compilation.getTitle());
    }

    private EventShortDto preparingFullDtoWithStat(EventFullDto eventFullDto) {
        DateTimeFormatter returnedTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<ViewStatsDto> stat = statsClient.getStats(eventFullDto.getCreatedOn().format(returnedTimeFormat),
                LocalDateTime.now().format(returnedTimeFormat),
                List.of("/events/" + eventFullDto.getId()), false);
        if (stat.size() > 0) {
            eventFullDto.setViews(stat.get(0).getHits());
        }
        List<Request> confirmedRequests = requestRepository.findAllByStatusAndEventId(Request.RequestStatus.CONFIRMED,
                eventFullDto.getId());
        eventFullDto.setConfirmedRequests(confirmedRequests.size());
        eventMapper.toEventFullDto(ratingService.getEventRatings(eventFullDto.getId()), eventFullDto);
        return eventMapper.toEventShortDto(eventFullDto);
    }
}
