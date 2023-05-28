package ru.filmogram.storage.film;

import ru.filmogram.exceptions.ObjectNotFoundException;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.Film;

import java.util.List;

public interface FilmStorage {
    List<Film> findAllFilm();

    Film createFilm(Film film) throws ValidationException;

    Film updateFilm(Film film) throws ValidationException;

    Film addLikeFilm(Long id, Long userId) throws ObjectNotFoundException, ValidationException;

    void deleteLikeFilm(Long id, Long userId);

    List<Film> sortPopularFilm(Integer count) throws ValidationException;

    List<Film> getAllPopular();

    Film getFilmId(Long id);
}
