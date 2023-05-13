package ru.practicum.explore.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ViewStatsDto {
    private String app; //Название сервиса
    private String uri; //URI сервиса
    private Long hits; //Количество просмотров
}
