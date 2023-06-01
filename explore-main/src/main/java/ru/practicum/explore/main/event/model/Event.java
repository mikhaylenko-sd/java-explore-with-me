package ru.practicum.explore.main.event.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.explore.main.category.model.Category;
import ru.practicum.explore.main.user.model.User;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
@Table(name = "events", schema = "public")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id", updatable = false, nullable = false, unique = true)
    private Long id;
    @Column(name = "annotation", nullable = false)
    private String annotation;
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "create_date", nullable = false)
    private LocalDateTime createdOn;
    @Column(name = "description", nullable = false)
    private String description;
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;
    @ManyToOne
    @JoinColumn(name = "initiator_id")
    private User initiator;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "lat", column = @Column(name = "lat")),
            @AttributeOverride(name = "lon", column = @Column(name = "lon"))
    })
    private Location location;
    @Column(name = "paid", nullable = false)
    private boolean paid;
    @Column(name = "participant_limit")
    private int participantLimit;
    @Column(name = "available")
    private boolean available;
    @Column(name = "published_on", nullable = false)
    private LocalDateTime publishedOn;
    @Column(name = "request_moderation", nullable = false)
    private boolean requestModeration;
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private State state;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "event_views", nullable = false)
    private Long views;
    @Column(name = "calculated_rating", nullable = false)
    private Long calculatedRating;

    public enum State {
        PENDING,
        PUBLISHED,
        CANCELED
    }
}
