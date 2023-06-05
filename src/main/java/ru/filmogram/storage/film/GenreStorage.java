package ru.filmogram.storage.film;

import ru.filmogram.model.Genre;

import java.util.List;

public interface GenreStorage {

    List<Genre> findAllGenres();

    Genre getGenreId(Long id);
}
