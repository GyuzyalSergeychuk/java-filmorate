package ru.filmogram.controllers;

import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.Film;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class FilmControllerTest {

    Film film;
    @InjectMocks
    FilmController filmController;

    @BeforeEach
    public void beforeEach() {
        film = Film.builder()
                .id(1)
                .description("После принятия образа аватара солдат Джейк Салли становится предводителем народа на'ви. " +
                        "Когда на Пандору возвращаются до зубов вооруженные земляне, Джейк готов дать им отпор.")
                .duration(192L)
                .name("Аватар: Путь воды")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .build();
    }

    @Test
    void findAll() {
        filmController.films.put(film.getId(), film);
        // action
        List<Film> filmList = filmController.findAll();

        // assert
        assertEquals(film.getName(), filmList.get(0).getName());
    }

    @Test
    void create() throws ValidationException {
        // action
        Film film1 = filmController.create(film);

        // assert
        assertEquals(film.getDescription(), film1.getDescription());
        assertEquals(film.getReleaseDate(), film1.getReleaseDate());
    }

    @Test
    void update() throws ValidationException {
        filmController.films.put(film.getId(), film);
        long duration = 193L;
        Film expectedFilm = Film.builder()
                .id(1)
                .description("После принятия образа аватара солдат Джейк Салли становится предводителем народа на'ви. " +
                        "Когда на Пандору возвращаются до зубов вооруженные земляне, Джейк готов дать им отпор.")
                .duration(193L)
                .name("Аватар: Путь воды")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .build();

        // action
        Film film1 = filmController.update(expectedFilm);

        assertEquals(duration, film1.getDuration());
    }

    @Test
    void updateInvalideName() {
        filmController.films.put(film.getId(), film);
        Film expectedFilm = Film.builder()
                .id(1)
                .description("После принятия образа аватара солдат Джейк Салли становится предводителем народа на'ви. " +
                        "Когда на Пандору возвращаются до зубов вооруженные земляне, Джейк готов дать им отпор.")
                .duration(192L)
                .name("")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .build();

        // action
        assertThrows(ValidationException.class,
                () -> filmController.update(expectedFilm),
                "Название фильма не может быть пустым");
    }

    @Test
    void updateInvalideDescription() {
        filmController.films.put(film.getId(), film);
        Film expectedFilm = Film.builder()
                .id(1)
                .description("После принятия образа аватара солдат Джейк Салли становится предводителем народа на'ви и " +
                        "на себя миссию по защите новых друзей от корыстных бизнесменов с Земли. Теперь ему есть за " +
                        "кого бороться — с Джейком его прекрасная возлюбленная Нейтири. " +
                        "Когда на Пандору возвращаются до зубов вооруженные земляне, Джейк готов дать им отпор.")
                .duration(192L)
                .name("Аватар: Путь воды")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .build();

        // action
        assertThrows(
                ValidationException.class,
                () -> filmController.update(expectedFilm),
                "Максимальная длина описания — 200 символов");
    }

    @Test
    void updateInvalideDuration() {
        filmController.films.put(film.getId(), film);
        Film expectedFilm = Film.builder()
                .id(1)
                .description("После принятия образа аватара солдат Джейк Салли становится предводителем народа на'ви. " +
                        "Когда на Пандору возвращаются до зубов вооруженные земляне, Джейк готов дать им отпор.")
                .duration(-1L)
                .name("Аватар: Путь воды")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .build();

        // action
        assertThrows(
                ValidationException.class,
                () -> filmController.update(expectedFilm),
                "Продолжительность фильма должна быть положительной");
    }

    @Test
    void updateInvalideReleaseDate() {
        filmController.films.put(film.getId(), film);
        Film expectedFilm = Film.builder()
                .id(1)
                .description("После принятия образа аватара солдат Джейк Салли становится предводителем народа на'ви. " +
                        "Когда на Пандору возвращаются до зубов вооруженные земляне, Джейк готов дать им отпор.")
                .duration(-1L)
                .name("Аватар: Путь воды")
                .releaseDate(LocalDate.of(1800, 12, 06))
                .build();

        // action
        assertThrows(
                ValidationException.class,
                () -> filmController.update(expectedFilm),
                "Дата релиза — не раньше 28 декабря 1895 года");
    }
}