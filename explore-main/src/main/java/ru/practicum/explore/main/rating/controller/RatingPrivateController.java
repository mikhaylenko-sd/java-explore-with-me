package ru.practicum.explore.main.rating.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explore.main.rating.service.RatingService;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/events/{eventId}/ratings")
public class RatingPrivateController {
    private final RatingService ratingService;

    public RatingPrivateController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    public ResponseEntity<Boolean> createRatingForEvent(@PathVariable Long userId,
                                                         @PathVariable Long eventId,
                                                         @RequestParam(required = false) Boolean isLike) {
        if (isLike == null) {
            log.info("Запрос на удаление рейтинга userId={}, eventId={}", userId, eventId);
        } else {
            log.info("Запрос на проставление рейтинга={} событию userId={}, eventId={}", isLike ? "like" : "dislike", userId, eventId);
        }
        ratingService.changeRating(userId, eventId, isLike);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}
