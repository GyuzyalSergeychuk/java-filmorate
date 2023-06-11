package ru.filmogram.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.filmogram.exceptions.ObjectNotFoundException;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.Film;
import ru.filmogram.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class FilmService {

    @Qualifier("filmDbStorageImpl")
    private FilmStorage filmStorage;

    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public List<Film> findAll() {
        return filmStorage.findAllFilm();
    }

    public Film create(Film film) throws ValidationException {
        Film afterCheckFilm = standardCheck(film);
        return filmStorage.createFilm(afterCheckFilm);
    }

    public Film update(Film film) throws ValidationException {
        Film afterCheckFilm = standardCheck(film);
        return filmStorage.updateFilm(afterCheckFilm);
    }

    public boolean addLike(Long id, Long userId) throws ValidationException {
        if (id <= 0) {
            throw new ValidationException(String.format("Фильм %d не найден", id));
        }
        if (userId <= 0) {
            throw new ValidationException(String.format("Пользователь %d не найден", userId));
        }
        return filmStorage.addLikeFilm(id, userId);
    }

    public boolean deleteLike(Long id, Long userId) {
        if (id <= 0) {
            throw new ObjectNotFoundException(String.format("Фильм %d не найден", id));
        }
        if (userId <= 0) {
            throw new ObjectNotFoundException(String.format("Пользователь %d не найден", userId));
        }
        return filmStorage.deleteLikeFilm(id, userId);
    }

    public List<Film> sortFilmCount(Integer count) throws ValidationException {
        if (count < 0) {
            throw new ValidationException(String.format("%d не может быть отрицательным", count));
        }
        return filmStorage.sortPopularFilm(count);
    }

    public List<Film> allPopularFilms() throws ValidationException {
        return filmStorage.getAllPopular();
    }

    public Film getIdFilm(Long id) throws ValidationException {
        if (id <= 0) {
            throw new ValidationException(String.format("Фильм %d не найден", id));
        }
        return filmStorage.getFilmId(id);
    }

    private Film standardCheck(Film film) throws ValidationException {
        if (film.getName() == null || film.getName().isBlank() || film.getName().isEmpty()) {
            log.error("Название фильма не может быть пустым: {}", film);
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription().length() > 200) {
            log.error("Длина описание превышает 200 символов: {}", film);
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }

        var dateOfFirstFilm = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate().isBefore(dateOfFirstFilm)) {
            log.error("Даты релиза - раньше 28 декабря 1895 года: {}", film);
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() < 0L) {
            log.error("Продолжительность фильма отсутствует: {}", film);
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        } else {
            return film;
        }
    }
}
