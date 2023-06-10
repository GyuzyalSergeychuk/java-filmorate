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
import ru.filmogram.model.Mpa;
import ru.filmogram.model.User;
import ru.filmogram.storage.film.FilmStorage;
import ru.filmogram.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FilmDbStorageImplTests {

    @Autowired
    FilmStorage filmStorage;
    @Autowired
    UserStorage userStorage;

    @Test
    void createFilm() throws ValidationException {
        Film film = Film.builder()
                .description("текст хороший")
                .duration(192L)
                .name("Аватар: Путь воды")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .mpa(Mpa.builder()
                        .id(1L)
                        .build())
                .genres(List.of(Genre.builder()
                        .id(1L)
                        .build()))
                .rate(1)
                .build();
        Film createdFilm = filmStorage.createFilm(film);

        assertEquals(film.getName(), createdFilm.getName());
        assertEquals(film.getDuration(), createdFilm.getDuration());
        assertEquals(film.getReleaseDate(), createdFilm.getReleaseDate());
        assertEquals(film.getDescription(), createdFilm.getDescription());
        assertEquals(film.getGenres().get(0).getId(), createdFilm.getGenres().get(0).getId());
        assertEquals(film.getRate(), createdFilm.getRate());
        assertEquals(film.getMpa().getId(), createdFilm.getMpa().getId());
    }

    @Test
    void getFilmId() throws ValidationException {
        Film film = Film.builder()
                .description("текст хороший")
                .duration(192L)
                .name("Аватар: Путь воды")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .mpa(Mpa.builder()
                        .id(1L)
                        .build())
                .genres(List.of(Genre.builder()
                        .id(1L)
                        .build(),
                        Genre.builder()
                                .id(1L)
                                .build()))
                .rate(1)
                .build();
        Film createdFilm = filmStorage.createFilm(film);
        Film film1 = Film.builder()
                .description("dnjjhjq")
                .duration(192L)
                .name("Аватар111: Путь воды")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .mpa(Mpa.builder()
                        .id(2L)
                        .build())
                .genres(List.of(Genre.builder()
                        .id(2L)
                        .build()))
                .rate(1)
                .build();
        Film createdFilm1 = filmStorage.createFilm(film1);
        assertEquals(createdFilm, filmStorage.getFilmId(createdFilm.getId()));
        assertEquals(1, createdFilm1.getGenres().size());
        assertEquals(createdFilm1, filmStorage.getFilmId(createdFilm1.getId()));
    }

    @Test
    void findAllFilm() throws ValidationException {
        Film film = Film.builder()
                .description("текст хороший")
                .duration(192L)
                .name("Аватар: Путь воды")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .mpa(Mpa.builder()
                        .id(3L)
                        .build())
                .genres(List.of(Genre.builder()
                        .id(1L)
                        .build()))
                .rate(1)
                .build();
        Film expectedFilm1 = filmStorage.createFilm(film);
        Film film2 = Film.builder()
                .description("текст хороший222")
                .duration(192L)
                .name("Аватар: 222")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .mpa(Mpa.builder()
                        .id(2L)
                        .build())
                .genres(List.of(Genre.builder()
                        .id(4L)
                        .build()))
                .rate(1)
                .build();
        Film expectedFilm2 = filmStorage.createFilm(film2);
        List<Film> expectedFilms = new ArrayList<>(List.of(expectedFilm1, expectedFilm2));
        List<Film> actualFilms = filmStorage.findAllFilm();

        assertEquals(expectedFilms.get(0).getMpa(), actualFilms.get(0).getMpa());
        assertEquals(expectedFilms.get(1).getGenres(), actualFilms.get(1).getGenres());
        assertEquals(expectedFilms.size(), actualFilms.size());
    }

    @Test
    void updateFilm() throws ValidationException {
        Film film = Film.builder()
                .description("текст хороший")
                .duration(192L)
                .name("Аватар: Путь воды")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .mpa(Mpa.builder()
                        .id(1L)
                        .build())
                .genres(List.of(Genre.builder()
                        .id(1L)
                        .build()))
                .rate(1)
                .build();
        Film baseFilmWithId = filmStorage.createFilm(film);
        Film expectedFilm = Film.builder()
                .id(1L)
                .description("НОВЫЙ")
                .duration(500L)
                .name("НОВЫЙ АВАТАР")
                .releaseDate(LocalDate.of(2023, 01, 06))
                .mpa(Mpa.builder()
                        .id(3L)
                        .build())
                .genres(List.of(Genre.builder().id(1L).build(),
                        Genre.builder().id(4L).build(),
                        Genre.builder().id(1L).build()))
                .rate(3)
                .build();

        Film actualFilm = filmStorage.updateFilm(expectedFilm);

        assertEquals(2, actualFilm.getGenres().size());
        assertEquals(expectedFilm.getId(), actualFilm.getId());
        assertEquals("Комедия", actualFilm.getGenres().get(0).getName());
        assertEquals("PG-13", actualFilm.getMpa().getName());
        assertEquals(expectedFilm.getDuration(), actualFilm.getDuration());
        assertEquals(expectedFilm.getRate(), actualFilm.getRate());
    }

    @Test
    void addLikeFilm() throws ValidationException {
        Film film = Film.builder()
                .description("текст хороший")
                .duration(192L)
                .name("Аватар: Путь воды")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .mpa(Mpa.builder()
                        .id(3L)
                        .build())
                .genres(List.of(Genre.builder()
                        .id(1L)
                        .build()))
                .rate(1)
                .build();
        Film baseFilm1 = filmStorage.createFilm(film);
        User user = User.builder()
                .name("Том1")
                .email("nnjh@come.1")
                .login("oooo")
                .birthday(LocalDate.of(1997, 07, 05))
                .build();
        User baseUser = userStorage.createUser(user);
        User user2 = User.builder()
                .name("Том2")
                .email("nnjh@come.2")
                .login("oooo")
                .birthday(LocalDate.of(1997, 07, 05))
                .build();
        User baseUser2 = userStorage.createUser(user2);

        Film film1 = filmStorage.addLikeFilm(baseFilm1.getId(), baseUser.getId());
        Film film2 = filmStorage.addLikeFilm(baseFilm1.getId(), baseUser2.getId());

        assertEquals(Set.of(baseUser.getId(), baseUser2.getId()), film2.getLikes());
    }

    @Test
    void deleteLikeFilm() throws ValidationException {
        Film film = Film.builder()
                .description("текст хороший")
                .duration(192L)
                .name("Аватар: Путь воды")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .mpa(Mpa.builder()
                        .id(3L)
                        .build())
                .genres(List.of(Genre.builder()
                        .id(1L)
                        .build()))
                .rate(1)
                .build();
        filmStorage.createFilm(film);
        User user = User.builder()
                .name("Том1")
                .email("nnjh@come.1")
                .login("oooo")
                .birthday(LocalDate.of(1997, 07, 05))
                .build();
        userStorage.createUser(user);
        filmStorage.addLikeFilm(1L, 1L);
        boolean isDeleteLikeFromFilm = filmStorage.deleteLikeFilm(1L, 1L);

        assertEquals(true, isDeleteLikeFromFilm);
    }

    @Test
    void sortPopularFilm() throws ValidationException {
        Film film = Film.builder()
                .description("текст хороший")
                .duration(192L)
                .name("Аватар: Путь воды")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .mpa(Mpa.builder()
                        .id(3L)
                        .build())
                .genres(List.of(Genre.builder()
                        .id(1L)
                        .id(3L)
                        .id(2L)
                        .build()))
                .rate(1)
                .build();
        Film baseFilm = filmStorage.createFilm(film);
        Film film1 = Film.builder()
                .description("текст хороший1111")
                .duration(192L)
                .name("Аватар: 1111")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .mpa(Mpa.builder()
                        .id(2L)
                        .build())
                .genres(List.of(Genre.builder()
                        .id(1L)
                        .id(2L)
                        .build()))
                .rate(1)
                .build();
        Film baseFilm1 = filmStorage.createFilm(film1);
        User user = User.builder()
                .name("Том1")
                .email("nnjh@come.0")
                .login("oooo")
                .birthday(LocalDate.of(1997, 07, 05))
                .build();
        User baseUser = userStorage.createUser(user);
        User user1 = User.builder()
                .name("Том2")
                .email("nnjh@come.1")
                .login("oooo")
                .birthday(LocalDate.of(1997, 07, 05))
                .build();
        User baseUser1 = userStorage.createUser(user1);
        User user2 = User.builder()
                .name("Том3")
                .email("nnjh@come.2")
                .login("oooo")
                .birthday(LocalDate.of(1997, 07, 05))
                .build();
        User baseUser2 = userStorage.createUser(user2);

        Film film2 = filmStorage.addLikeFilm(baseFilm1.getId(), baseUser.getId());
        Film film3 = filmStorage.addLikeFilm(baseFilm.getId(), baseUser1.getId());
        Film film4 = filmStorage.addLikeFilm(baseFilm1.getId(), baseUser2.getId());

        List<Film> actualFilms = filmStorage.sortPopularFilm(0);

        assertEquals(2, actualFilms.size());
        assertEquals(baseFilm1.getId(), actualFilms.get(0).getId());
        assertEquals(baseFilm.getId(), actualFilms.get(1).getId());

        List<Film> actualFilms1 = filmStorage.sortPopularFilm(1);
        assertEquals(1, actualFilms1.size());

    }

    @Test
    void getAllPopular() throws ValidationException {
        Film film = Film.builder()
                .description("текст хороший")
                .duration(192L)
                .name("Аватар: Путь воды")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .mpa(Mpa.builder()
                        .id(3L)
                        .build())
                .genres(List.of(Genre.builder()
                        .id(1L)
                        .id(3L)
                        .id(2L)
                        .build()))
                .rate(1)
                .build();
        Film baseFilm = filmStorage.createFilm(film);
        Film film1 = Film.builder()
                .description("текст хороший1111")
                .duration(192L)
                .name("Аватар: 1111")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .mpa(Mpa.builder()
                        .id(2L)
                        .build())
                .genres(List.of(Genre.builder()
                        .id(1L)
                        .id(2L)
                        .build()))
                .rate(1)
                .build();
        Film baseFilm1 = filmStorage.createFilm(film1);
        User user = User.builder()
                .name("Том1")
                .email("nnjh@come.0")
                .login("oooo")
                .birthday(LocalDate.of(1997, 07, 05))
                .build();
        User baseUser = userStorage.createUser(user);
        User user1 = User.builder()
                .name("Том2")
                .email("nnjh@come.1")
                .login("oooo")
                .birthday(LocalDate.of(1997, 07, 05))
                .build();
        User baseUser1 = userStorage.createUser(user1);
        User user2 = User.builder()
                .name("Том3")
                .email("nnjh@come.2")
                .login("oooo")
                .birthday(LocalDate.of(1997, 07, 05))
                .build();
        User baseUser2 = userStorage.createUser(user2);

        filmStorage.addLikeFilm(baseFilm1.getId(), baseUser.getId());
        filmStorage.addLikeFilm(baseFilm.getId(), baseUser1.getId());
        filmStorage.addLikeFilm(baseFilm1.getId(), baseUser2.getId());

        List<Film> actualFilms = filmStorage.getAllPopular();

        assertEquals(baseFilm1.getId(), actualFilms.get(0).getId());
        assertEquals(baseFilm.getId(), actualFilms.get(1).getId());
        assertEquals(2, actualFilms.size());
    }
}