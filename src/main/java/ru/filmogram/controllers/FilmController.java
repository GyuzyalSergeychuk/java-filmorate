package ru.filmogram.controllers;

import org.springframework.web.bind.annotation.*;
import ru.filmogram.exceptions.ValidationException;
import lombok.extern.slf4j.Slf4j;
import ru.filmogram.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    HashMap<Integer, Film> films = new HashMap<>();

    @GetMapping()
    public List<Film> findAll() {
        return new ArrayList<Film>(films.values());
    }

    @PostMapping()
    public Film create(@RequestBody Film film) throws ValidationException {
        Film afterCheckFilm = standardCheck(film);
        afterCheckFilm.assignId();
        films.put(afterCheckFilm.getId(), afterCheckFilm);
        log.info("Добавлен фильм: {}", afterCheckFilm);
        return afterCheckFilm;
    }

    @PutMapping()
    public Film update(@RequestBody Film film) throws ValidationException {
        Film afterCheckFilm = standardCheck(film);
        if (afterCheckFilm.getId() == films.get(afterCheckFilm.getId()).getId()) {
            films.put(afterCheckFilm.getId(), afterCheckFilm);
            log.info("В объект фильм внесены изменения : {}", afterCheckFilm);
            return afterCheckFilm;
        }
        throw new ValidationException("Вызван endpount Put, но данный пользователь отсутствует");
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
