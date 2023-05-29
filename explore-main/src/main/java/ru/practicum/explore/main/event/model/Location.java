package ru.practicum.explore.main.event.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Embeddable
public class Location {
    private Float lat;
    private Float lon;
}
