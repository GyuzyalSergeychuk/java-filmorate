package ru.filmogram.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class Film {
    private static int nextId;
    private int id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    // продолжительность фильма в минутах
    private long duration;

    public void assignId() {
        nextId++;
        id = nextId;
    }
}
