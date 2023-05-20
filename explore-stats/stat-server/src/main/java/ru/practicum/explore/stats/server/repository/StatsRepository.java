package ru.practicum.explore.stats.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.explore.stats.server.entity.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<EndpointHit, Integer> {
    @Query("SELECT new EndpointHit(e.app, e.uri, count(e.ip)) FROM EndpointHit e " +
            "WHERE e.timestamp BETWEEN ?1 AND ?2 " +
            "GROUP BY e.app, e.uri")
    List<EndpointHit> findByTime(LocalDateTime start, LocalDateTime end);

    @Query("SELECT new EndpointHit(e.app, e.uri, count(DISTINCT e.ip)) FROM EndpointHit e " +
            "WHERE e.timestamp BETWEEN ?1 AND ?2 " +
            "GROUP BY e.app, e.uri")
    List<EndpointHit> findByUniqueIpAndTime(LocalDateTime start, LocalDateTime end);

    @Query("SELECT new EndpointHit(e.app, e.uri, count(e.ip)) FROM EndpointHit e " +
            "WHERE e.uri IN ?1 AND e.timestamp BETWEEN ?2 AND ?3 " +
            "GROUP BY e.app, e.uri")
    List<EndpointHit> findByUriAndTime(List<String> uris, LocalDateTime start, LocalDateTime end);

    @Query("SELECT new EndpointHit(e.app, e.uri, count(DISTINCT e.ip)) FROM EndpointHit e " +
            "WHERE e.uri IN ?1 AND e.timestamp BETWEEN ?2 AND ?3 " +
            "GROUP BY e.app, e.uri")
    List<EndpointHit> findByUriAndUniqueIpAndTime(List<String> uris, LocalDateTime start, LocalDateTime end);
}
