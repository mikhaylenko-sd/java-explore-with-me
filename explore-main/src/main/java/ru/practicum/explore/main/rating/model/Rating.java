package ru.practicum.explore.main.rating.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.practicum.explore.main.event.model.Event;
import ru.practicum.explore.main.user.model.User;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;


@Entity
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "ratings", schema = "public")
public class Rating {
    @EmbeddedId
    private RatingId id;

    @ManyToOne
    @JoinColumn(name = "event_id", updatable = false, insertable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "user_id", updatable = false, insertable = false)
    private User user;

    @Column(name = "is_like")
    private Boolean isLike;

    @Embeddable
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RatingId implements Serializable {
        @Column(name = "event_id")
        private Long eventId;

        @Column(name = "user_id")
        private Long userId;
    }
}

