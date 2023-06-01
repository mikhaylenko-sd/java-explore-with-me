package ru.practicum.explore.main.rating.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.explore.main.event.model.Event;
import ru.practicum.explore.main.event.repository.EventRepository;
import ru.practicum.explore.main.exceptions.BaseException;
import ru.practicum.explore.main.exceptions.NotFoundException;
import ru.practicum.explore.main.rating.dto.EventRatingsDto;
import ru.practicum.explore.main.rating.model.Rating;
import ru.practicum.explore.main.rating.repository.RatingRepository;
import ru.practicum.explore.main.user.mapper.UserMapper;
import ru.practicum.explore.main.user.model.User;
import ru.practicum.explore.main.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static ru.practicum.explore.main.event.model.Event.State.PUBLISHED;

@Slf4j
@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final UserMapper userMapper;

    private static final long MAX_RATING = 100L;

    public RatingService(RatingRepository ratingRepository,
                         UserRepository userRepository,
                         EventRepository eventRepository,
                         UserMapper userMapper) {
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.userMapper = userMapper;
    }

    public void changeRating(Long userId, Long eventId, Boolean ratingValue) {
        Rating rating = validateAndConstructRatingId(userId, eventId, ratingValue);

        if (ratingValue == null) {
            if (ratingRepository.existsById(rating.getId())) {
                ratingRepository.deleteById(rating.getId());
                recalculateEventRating(rating.getEvent());
                log.info("Рейтинг удален");
            }
        } else {
            ratingRepository.save(rating);
            recalculateEventRating(rating.getEvent());
            log.info("Событию проставлен рейтинг");
        }
    }

    private Rating validateAndConstructRatingId(Long userId, Long eventId, Boolean ratingValue) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new NotFoundException(NotFoundException.NotFoundType.USER, userId);
        }
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            throw new NotFoundException(NotFoundException.NotFoundType.EVENT, eventId);
        }
        if (event.get().getState() != PUBLISHED) {
            throw new BaseException("Невозможно проставить рейтинг",
                    String.format("Для проставления рейтинга событие должно быть опубликовано, текущий статус=%s", event.get().getState()));
        }

        return new Rating(
                new Rating.RatingId(event.get().getId(), user.get().getId()),
                event.get(), user.get(), ratingValue
        );
    }

    private void recalculateEventRating(Event event) {
        List<Rating> ratings = ratingRepository.findAllById_EventId(event.getId());
        int likeCount = 0;
        int dislikeCount = 0;
        for (Rating rating : ratings) {
            if (rating.getIsLike()) {
                likeCount++;
            } else {
                dislikeCount++;
            }
        }
        long totalLikeDislikeCount = likeCount + dislikeCount;
        long calculatedRating = totalLikeDislikeCount == 0 ? 0 :
                Math.round(MAX_RATING * Math.log1p(totalLikeDislikeCount) * (likeCount - dislikeCount) / totalLikeDislikeCount);
        log.info("Для события рассчитан рейтинг eventId={}, rating={}", event.getId(), calculatedRating);
        event.setCalculatedRating(calculatedRating);
        eventRepository.save(event);
    }

    public EventRatingsDto getEventRatings(Long eventId) {
        log.info("Получение рейтинга события eventId={}", eventId);
        List<Rating> ratings = ratingRepository.findAllById_EventId(eventId);
        EventRatingsDto eventRatingsDto = new EventRatingsDto();
        for (Rating rating : ratings) {
            if (rating.getIsLike()) {
                eventRatingsDto.getLikes().add(userMapper.toUserDto(rating.getUser()));
            } else {
                eventRatingsDto.getDislikes().add(userMapper.toUserDto(rating.getUser()));
            }
        }
        return eventRatingsDto;
    }
}
