package ru.filmogram.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.Film;
import ru.filmogram.model.Mpa;
import ru.filmogram.storage.film.FilmStorage;
import ru.filmogram.storage.film.MpaStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MpaDbStorageImplTest {

    @Autowired
    FilmStorage filmStorage;
    @Autowired
    MpaStorage mpaStorage;

    @Test
    void getMpaId() throws ValidationException {
        Film film = Film.builder()
                .description("текст хороший")
                .duration(192L)
                .name("Аватар: Путь воды")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .rating("GP-666")
                .genre(Set.of("приключение", "боевик"))
                .build();
        Film actualFilm = filmStorage.createFilm(film);
        Mpa mpa = mpaStorage.getMpaId(1l);

        assertEquals(actualFilm.getRating(), mpa.getName());
    }

    @Test
    void findAllMpa() throws ValidationException {
        Film film = Film.builder()
                .description("текст хороший")
                .duration(192L)
                .name("Аватар: Путь воды")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .rating("GP-111")
                .genre(Set.of("приключение", "боевик"))
                .build();
        filmStorage.createFilm(film);
        Film film1 = Film.builder()
                .description("текст хороший")
                .duration(192L)
                .name("Аватар: Путь воды")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .rating("GP")
                .genre(Set.of("приключение", "УЖАСЫ"))
                .build();
        filmStorage.createFilm(film1);
        List<Mpa> allMpa = mpaStorage.findAllMpa();

        assertEquals(2, allMpa.size());
        assertEquals("GP", allMpa.get(1).getName());
    }
}