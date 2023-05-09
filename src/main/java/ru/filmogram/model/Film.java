package ru.filmogram.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class Film implements Comparable<Film>{
    private static Long nextId = 0L;
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    // продолжительность фильма в минутах
    private long duration;
    private Set<Long> like;

    public void assignId() {
        nextId++;
        id = nextId;
    }

    public void addLike(Long userId) {
        like.add(userId);
    }

    public void deleteLike(Long userId) {
        like.remove(userId);
    }

    @Override
    public int compareTo(Film o) {
        return this.like.size() - o.like.size();
    }
}
