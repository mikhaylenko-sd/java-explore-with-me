package ru.practicum.explore.main.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import ru.practicum.explore.main.event.model.Event;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class EventCriteriaRepository {
    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;

    public EventCriteriaRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
    }

    public Page<Event> findAllByInitiatorIdInAndStateInAndCategoryIdInAndEventDateBetween(
            List<Long> users,
            List<Event.State> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Pageable pageable) {
        CriteriaQuery<Event> criteriaQuery = criteriaBuilder.createQuery(Event.class);
        Root<Event> eventRoot = criteriaQuery.from(Event.class);
        Predicate predicate = createAdminPredicate(users, states, categories, rangeStart, rangeEnd, eventRoot);

        return getEvents(pageable, criteriaQuery, predicate);
    }

    private Page<Event> getEvents(Pageable pageable, CriteriaQuery<Event> criteriaQuery, Predicate predicate) {
        criteriaQuery.where(predicate);
        TypedQuery<Event> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<Event> events = typedQuery.getResultList();

        return new PageImpl<>(events);
    }

    public Page<Event> findAllByTextAndCategoryIdInAndPaidAndEventDateBetweenAndAvailable(
            String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
            LocalDateTime rangeEnd, Boolean available, Pageable pageable) {

        CriteriaQuery<Event> criteriaQuery = criteriaBuilder.createQuery(Event.class);
        Root<Event> eventRoot = criteriaQuery.from(Event.class);
        Predicate predicate = createUserPredicate(text, categories, paid, rangeStart, rangeEnd, available, eventRoot);

        return getEvents(pageable, criteriaQuery, predicate);
    }

    private Predicate createAdminPredicate(List<Long> users, List<Event.State> states,
                                           List<Long> categories, LocalDateTime rangeStart,
                                           LocalDateTime rangeEnd, Root<Event> root) {
        List<Predicate> predicates = new ArrayList<>();
        if (users != null) {
            predicates.add(root.get("initiator").get("id").in(users));
        }
        if (states != null) {
            predicates.add(root.get("state").in(states));
        }
        if (categories != null) {
            predicates.add(root.get("category").get("id").in(categories));
        }
        predicates.addAll(getEventDatePredicates(rangeStart, rangeEnd, root));
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private Predicate createUserPredicate(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                          LocalDateTime rangeEnd, Boolean available, Root<Event> root) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(root.get("state"), Event.State.PUBLISHED));
        if (text != null) {
            predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), "%" + text.toLowerCase() + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + text.toLowerCase() + "%")
            ));
        }
        if (categories != null) {
            predicates.add(root.get("category").get("id").in(categories));
        }
        if (paid != null) {
            predicates.add(criteriaBuilder.equal(root.get("paid"), paid));
        }
        if (available != null) {
            predicates.add(criteriaBuilder.equal(root.get("available"), true));
        }
        predicates.addAll(getEventDatePredicates(rangeStart, rangeEnd, root));
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private List<Predicate> getEventDatePredicates(LocalDateTime rangeStart, LocalDateTime rangeEnd, Root<Event> root) {
        List<Predicate> predicates = new ArrayList<>();
        if (rangeStart != null) {
            predicates.add(criteriaBuilder.greaterThan(root.get("eventDate"), rangeStart));
        }
        if (rangeEnd != null) {
            predicates.add(criteriaBuilder.lessThan(root.get("eventDate"), rangeEnd));
        }
        if (rangeStart == null) {
            predicates.add(criteriaBuilder.greaterThan(root.get("eventDate"), LocalDateTime.now()));
        }
        return predicates;
    }

}
