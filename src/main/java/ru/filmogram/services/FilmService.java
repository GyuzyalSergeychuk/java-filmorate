package ru.filmogram.services;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.Film;
import ru.filmogram.storage.film.FilmStorage;

import java.util.List;

@Service
public class FilmService {

    @Qualifier("filmDbStorageImpl")
    private FilmStorage filmStorage;

    public List<Film> findAll() {
        return filmStorage.findAllFilm();
    }

    public Film create(Film film) throws ValidationException {
        return filmStorage.createFilm(film);
    }

    public Film update(Film film) throws ValidationException {
        return filmStorage.updateFilm(film);
    }

    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film addLike(Long id, Long userId) throws ValidationException {
        return filmStorage.addLikeFilm(id, userId);
    }

    public void deleteLike(Long id, Long userId) {
        filmStorage.deleteLikeFilm(id, userId);
    }

    public List<Film> sortFilmCount(Integer count) throws ValidationException {
        return filmStorage.sortPopularFilm(count);
    }

    public List<Film> allPopularFilms() throws ValidationException {
        return filmStorage.getAllPopular();
    }

    public Film getIdFilm(Long id) {
        return filmStorage.getFilmId(id);
    }
}
