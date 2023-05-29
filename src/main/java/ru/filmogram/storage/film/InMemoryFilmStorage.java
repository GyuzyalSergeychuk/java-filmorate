package ru.filmogram.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.filmogram.exceptions.ObjectNotFoundException;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {

    private HashMap<Long, Film> films = new HashMap<>();

    @Override
    public List<Film> findAllFilm() {
        return new ArrayList<Film>(films.values());
    }

    @Override
    public Film createFilm(Film film) throws ValidationException {
        Film afterCheckFilm = standardCheck(film);
        afterCheckFilm.assignId();
        films.put(afterCheckFilm.getId(), afterCheckFilm);
        log.info("Добавлен фильм: {}", afterCheckFilm);
        return afterCheckFilm;
    }

    @Override
    public Film updateFilm(Film film) throws ValidationException {
        Film afterCheckFilm = standardCheck(film);
        if (afterCheckFilm.getId().equals(films.get(afterCheckFilm.getId()).getId())) {
            films.put(afterCheckFilm.getId(), afterCheckFilm);
            log.info("В объект фильм внесены изменения : {}", afterCheckFilm);
            return afterCheckFilm;
        }
        throw new ValidationException("Вызван endpount Put, но данный пользователь отсутствует");
    }

    @Override
    public Film addLikeFilm(Long id, Long userId) throws ObjectNotFoundException {
        if (!films.containsKey(id)) {
            throw new ObjectNotFoundException("Данный фильм отсутствует");
        }
        Film film = films.get(id);
        film.addLike(userId);
        return film;
    }

    @Override
    public boolean deleteLikeFilm(Long id, Long userId) {
        if (userId < 1) {
            throw new ObjectNotFoundException("Такой пользователь не существует");
        }
        films.get(id).deleteLike(userId);
        return true;
    }

    @Override
    public List<Film> sortPopularFilm(Integer count) throws ValidationException {
        List<Film> sortFilms = films.values()
                .stream()
                .sorted()
                .collect(Collectors.toList());

        if (count == null) {
            return sortFilms.subList(0, 10);
        }
        if (count < 0) {
            throw new ValidationException("Значение не может быть отрицательным");
        }
        return sortFilms.subList(0, count);
    }

    @Override
    public List<Film> getAllPopular() {
        return films.values()
                .stream()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public Film getFilmId(Long id) {
        if (films.containsKey(id)) {
            return films.get(id);
        }
        throw new ObjectNotFoundException("Фильм не найден");
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
