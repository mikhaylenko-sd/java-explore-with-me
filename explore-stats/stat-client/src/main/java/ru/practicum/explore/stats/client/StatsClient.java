package ru.practicum.explore.stats.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.explore.stats.dto.EndpointHitDto;
import ru.practicum.explore.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class StatsClient {

    private final RestTemplate restTemplate;
    private final String statServerUrl;
    private static final String SAVE_HIT_PATH = "/hit";
    private static final String GET_STATS_PATH = "/stats";

    public StatsClient(RestTemplateBuilder restTemplate, @Value("${stat-server.url}") String statServerUrl) {
        this.restTemplate = restTemplate.build();
        this.statServerUrl = statServerUrl;
    }

    public void saveHit(String app, String uri, String ip) {
        HttpEntity<EndpointHitDto> request = new HttpEntity<>(
                new EndpointHitDto(null, app, uri, ip, LocalDateTime.now().withNano(0))
        );

        restTemplate.postForEntity(statServerUrl + SAVE_HIT_PATH, request, EndpointHitDto.class);
    }

    public List<ViewStatsDto> getStats(String startDate,
                                       String endDate,
                                       List<String> uris,
                                       boolean unique) {
        String urisString = String.join(",", uris);
        ResponseEntity<List<ViewStatsDto>> response = restTemplate.exchange(
                statServerUrl + GET_STATS_PATH + "?start={start}&end={end}&uris={uris}&unique={unique}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                },
                startDate, endDate, urisString, unique
        );

        return response.getBody();
    }
}
