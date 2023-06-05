package ru.filmogram.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.Film;
import ru.filmogram.model.Genre;
import ru.filmogram.storage.film.FilmStorage;
import ru.filmogram.storage.film.GenreStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GenreDbStorageImplTest {

    @Autowired
    FilmStorage filmStorage;
    @Autowired
    GenreStorage genreStorage;

    @Test
    void getGenreId() throws ValidationException {
        Film film = Film.builder()
                .description("текст хороший")
                .duration(192L)
                .name("Аватар: Путь воды")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .rating("GP-666")
                .genre(Set.of("приключение", "боевик"))
                .build();
        Film actualFilm = filmStorage.createFilm(film);
        Genre genre1 = genreStorage.getGenreId(1L);
        Genre genre2 = genreStorage.getGenreId(2L);

        assertEquals(actualFilm.getGenre(), Set.of(genre1.getName(), genre2.getName()));
    }

    @Test
    void findAllGenres() throws ValidationException {
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
                .rating("GP-666")
                .genre(Set.of("приключение", "УЖАСЫ"))
                .build();
        filmStorage.createFilm(film1);
        List<Genre> allGenre = genreStorage.findAllGenres();

        assertEquals(3,allGenre.size());
        assertEquals("ужасы", allGenre.get(2).getName());
    }
}