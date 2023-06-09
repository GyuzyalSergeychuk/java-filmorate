package ru.filmogram.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Data
@Builder
public class Film implements Comparable<Film> {
    private static Long nextId = 0L;
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private List<Genre> genres;
    private Mpa mpa;
    private Integer rate;
    // продолжительность фильма в минутах
    private long duration;
    private Set<Long> likes;

    public void assignId() {
        nextId++;
        id = nextId;
    }

    public void addLike(Long userId) {
        if (likes == null) {
            likes = new HashSet<>();
        }
        likes.add(userId);
    }

    public void deleteLike(Long userId) {
        likes.remove(userId);
    }

    @Override
    public int compareTo(Film o) {
        if (likes == null) {
            likes = new HashSet<>();
        }
        if (o.getLikes() == null) {
            return -1;
        }
        return this.likes.size() - o.likes.size();
    }

    @Override
    public String toString() {
        return "Film{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", releaseDate=" + releaseDate +
                ", genres=" + genres +
                ", mpa=" + mpa +
                ", rate=" + rate +
                ", duration=" + duration +
                ", likes=" + likes +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Film film = (Film) o;
        return duration == film.duration && Objects.equals(id, film.id) && Objects.equals(name, film.name) && Objects.equals(description, film.description) && Objects.equals(releaseDate, film.releaseDate) && Objects.equals(genres, film.genres) && Objects.equals(mpa, film.mpa) && Objects.equals(rate, film.rate) && Objects.equals(likes, film.likes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, releaseDate, genres, mpa, rate, duration, likes);
    }
}
