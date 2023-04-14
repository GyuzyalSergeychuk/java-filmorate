package controllers;

import exceptions.ValidationException;
import lombok.extern.slf4j.Slf4j;
import model.Film;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/film")
@Slf4j
public class FilmController {

    HashMap<String, Film> films = new HashMap<>();

    @GetMapping()
    public List<Film> findAll() {
        return new ArrayList<Film>(films.values());
    }

    @PostMapping()
    public Film create(@RequestBody Film film) throws ValidationException {
        Film afterCheckFilm = standardCheck(film);
        films.put(afterCheckFilm.getName(), afterCheckFilm);
        log.info("Добавлен фильм: {}", afterCheckFilm);
        return afterCheckFilm;
    }

    @PutMapping()
    public Film update(@RequestBody Film film) throws ValidationException {
        Film afterCheckFilm = standardCheck(film);
        if (afterCheckFilm.equals(films.get(afterCheckFilm.getName()))) {
            films.put(afterCheckFilm.getName(), afterCheckFilm);
            log.info("В объект фильм внесены изменения : {}", afterCheckFilm);
            return afterCheckFilm;
        }
        throw new ValidationException("Вызван endpount Put, но данный пользователь отсутствует");
    }

    private Film standardCheck(Film film) throws ValidationException {

        LocalDate date = LocalDate.of(1895, 12, 28);
        LocalTime time = LocalTime.of(00, 00, 00);

        if (film.getName().isBlank()) {
            log.debug("Название фильма не может быть пустым : {}", film);
            throw new ValidationException("Название фильма не может быть пустым");
        } else if (film.getDescription().length() < 200) {
            log.debug("Длина описание превышает 200 символов : {}", film);
            throw new ValidationException("Максимальная длина описания — 200 символов");
        } else if (film.getReleaseDate().isBefore(date)) {
            log.debug("Даты релиза - раньше 28 декабря 1895 года : {}", film);
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        } else if (film.getDuration().isAfter(time)) {
            log.debug("Продолжительность фильма отсутствует : {}", film);
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        } else {
            return film;
        }
    }
}
