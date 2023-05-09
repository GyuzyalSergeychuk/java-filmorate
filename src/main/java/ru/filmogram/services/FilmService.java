package ru.filmogram.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.Film;
import ru.filmogram.storage.film.FilmStorage;

import java.util.List;

@Service
public class FilmService {

    private FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film addLike(Long id, Long userId) {
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
