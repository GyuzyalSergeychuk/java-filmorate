package ru.filmogram.storage.film;

import ru.filmogram.exceptions.ObjectNotFoundException;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.Film;

import java.util.List;

public interface FilmStorage {
    public List<Film> findAllFilm();

    public Film createFilm(Film film) throws ValidationException;

    public Film updateFilm(Film film) throws ValidationException;

    public Film addLikeFilm(Long id, Long userId) throws ObjectNotFoundException;

    public void deleteLikeFilm(Long id, Long userId);

    public List<Film> sortPopularFilm(Integer count) throws ValidationException;
}
