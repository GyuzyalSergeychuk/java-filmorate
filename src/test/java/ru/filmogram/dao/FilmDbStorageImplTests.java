package ru.filmogram.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.Film;
import ru.filmogram.model.User;
import ru.filmogram.storage.film.FilmStorage;
import ru.filmogram.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageImplTests {

    Film film;
    User user;

    @Autowired
    FilmStorage filmStorage;
    @Autowired
    UserStorage userStorage;

    @AfterEach
    void init() {

    }

    @Test
    void createFilm() throws ValidationException {
        film = Film.builder()
                .description("текст хороший")
                .duration(192L)
                .name("Аватар: Путь воды")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .rating("GP-666")
                .genre(Set.of("приключение", "боевик"))
                .build();
        Film createdFilm = filmStorage.createFilm(film);

        assertEquals(film.getName(), createdFilm.getName());
        assertEquals(film.getDuration(), createdFilm.getDuration());
        assertEquals(film.getReleaseDate(), createdFilm.getReleaseDate());
        assertEquals(film.getDescription(), createdFilm.getDescription());
    };

    @Test
    void getFilmId() throws ValidationException {
        film = Film.builder()
                .description("текст хороший")
                .duration(192L)
                .name("Аватар: Путь воды")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .rating("GP-666")
                .genre(Set.of("приключение", "боевик"))
                .build();
        filmStorage.createFilm(film);
        film = Film.builder()
                .description("текст хороший1111")
                .duration(200L)
                .name("Аватар: Путь воды2")
                .releaseDate(LocalDate.of(2022, 12, 02))
                .rating("GP")
                .genre(Set.of("приключение", "боевик", "фантастика"))
                .build();
        filmStorage.createFilm(film);
        assertEquals((Film.builder()
                .id(1L)
                .description("текст хороший")
                .duration(192L)
                .name("Аватар: Путь воды")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .rating("GP-666")
                .genre(Set.of("приключение", "боевик"))
                .build()), filmStorage.getFilmId(1l));
        assertEquals((Film.builder()
                .id(2L)
                .description("текст хороший1111")
                .duration(200L)
                .name("Аватар: Путь воды2")
                .releaseDate(LocalDate.of(2022, 12, 02))
                .rating("GP")
                .genre(Set.of("приключение", "боевик", "фантастика"))
                .build()), filmStorage.getFilmId(2l));
    }

    @Test
    void findAllFilm() throws ValidationException {
        film = Film.builder()
                .description("текст хороший")
                .duration(192L)
                .name("Аватар: Путь воды")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .rating("GP-666")
                .genre(Set.of("приключение", "боевик"))
                .build();
        filmStorage.createFilm(film);
        film = Film.builder()
                .description("текст хороший1111")
                .duration(200L)
                .name("Аватар: Путь воды2")
                .releaseDate(LocalDate.of(2022, 12, 02))
                .rating("GP")
                .genre(Set.of("приключение", "боевик", "фантастика"))
                .build();
        filmStorage.createFilm(film);
        List<Film> films = filmStorage.findAllFilm();

        assertEquals(1, films.get(0).getId());
        assertEquals("GP-666", films.get(0).getRating());
        assertEquals(Set.of("приключение","боевик", "фантастика"), films.get(1).getGenre());
    }

    @Test
    void updateFilm() throws ValidationException {
        film = Film.builder()
                .description("текст хороший1111")
                .duration(200L)
                .name("Аватар: Путь воды2")
                .releaseDate(LocalDate.of(2022, 12, 02))
                .rating("GP")
                .genre(Set.of("приключение", "боевик", "фантастика"))
                .build();
        filmStorage.createFilm(film);

        film = Film.builder()
                .id(1l)
                .description("новый")
                .duration(196L)
                .name("Аватар: ПутьНОВЫЙ")
                .releaseDate(LocalDate.of(2022, 12, 02))
                .rating("R")
                .genre(Set.of("приключение", "фантастика"))
                .build();

        Film actualFilm = filmStorage.updateFilm(film);

        assertEquals(1, actualFilm.getId());
        assertEquals(film.getGenre(), actualFilm.getGenre());
        assertEquals(film.getRating(), actualFilm.getRating());
        assertEquals(film.getDuration(), actualFilm.getDuration());
    }

    @Test
    void addLikeFilm() throws ValidationException {
        film = Film.builder()
                .description("текст хороший1111")
                .duration(200L)
                .name("Аватар: Путь воды2")
                .releaseDate(LocalDate.of(2022, 12, 02))
                .rating("GP")
                .genre(Set.of("приключение", "боевик", "фантастика"))
                .build();
        filmStorage.createFilm(film);
        user = User.builder()
                .name("Том1")
                .email("nnjh@come.1")
                .login("oooo")
                .birthday(LocalDate.of(1997, 07, 05))
                .build();
        userStorage.createUser(user);
        user = User.builder()
                .name("Том2")
                .email("nnjh@come.2")
                .login("oooo")
                .birthday(LocalDate.of(1997, 07, 05))
                .build();
        userStorage.createUser(user);

        Film film1 = filmStorage.addLikeFilm(1L, 1L);
        Film film2 = filmStorage.addLikeFilm(1L, 2L);

        assertEquals(Set.of(1L, 2L), film2.getLikes());
    }

    @Test
    void deleteLikeFilm() throws ValidationException {
        film = Film.builder()
                .description("текст хороший1111")
                .duration(200L)
                .name("Аватар: Путь воды2")
                .releaseDate(LocalDate.of(2022, 12, 02))
                .rating("GP")
                .genre(Set.of("приключение", "боевик", "фантастика"))
                .build();
        filmStorage.createFilm(film);
        user = User.builder()
                .name("Том1")
                .email("nnjh@come.1")
                .login("oooo")
                .birthday(LocalDate.of(1997, 07, 05))
                .build();
        userStorage.createUser(user);
        Film film1 = filmStorage.addLikeFilm(1L, 1L);
        boolean isDeleteLikeFromFilm = filmStorage.deleteLikeFilm(1L, 1L);

        assertEquals(true, isDeleteLikeFromFilm);
    }

    @Test
    void sortPopularFilm() throws ValidationException {
        List<Film> sortFilm = filmStorage.sortPopularFilm(2);
        assertEquals("Аватар: Путь воды", sortFilm.get(0).getName());
    }

    @Test
    void getAllPopular() {

    }
}