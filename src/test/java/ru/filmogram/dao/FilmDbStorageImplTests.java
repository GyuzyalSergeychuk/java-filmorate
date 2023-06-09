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
    void testCreateFilm() throws ValidationException {
        Film film = Film.builder()
                .description("????? ???????")
                .duration(192L)
                .name("??????: ???? ????")
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

        assertEquals(film.getName(), createdFilm.getName(), "Название фильма успешно добавлено");
        assertEquals(film.getDuration(), createdFilm.getDuration(), "Продолжительность фильма добавлена");
        assertEquals(film.getReleaseDate(), createdFilm.getReleaseDate(), "Дата релиза добавлена");
        assertEquals(film.getDescription(), createdFilm.getDescription(), "Описание добавлено");
        assertEquals(film.getGenres().get(0).getId(), createdFilm.getGenres().get(0).getId(), "Жанры добавлены");
        assertEquals(film.getRate(), createdFilm.getRate(), "rate добавлено");
        assertEquals(film.getMpa().getId(), createdFilm.getMpa().getId(), "Mpa добавлена");
    }

    @Test
    void testGetFilmId() throws ValidationException {
        Film film = Film.builder()
                .description("????? ???????")
                .duration(192L)
                .name("??????: ???? ????")
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
                .name("??????111: ???? ????")
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
    void testFindAllFilm() throws ValidationException {
        Film film = Film.builder()
                .description("????? ???????")
                .duration(192L)
                .name("??????: ???? ????")
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
                .description("????? ???????222")
                .duration(192L)
                .name("??????: 222")
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
    void testUpdateFilm() throws ValidationException {
        Film film = Film.builder()
                .description("????? ???????")
                .duration(192L)
                .name("??????: ???? ????")
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
                .description("?????")
                .duration(500L)
                .name("????? ??????")
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
        assertEquals("PG-13", actualFilm.getMpa().getName());
        assertEquals(expectedFilm.getDuration(), actualFilm.getDuration());
        assertEquals(expectedFilm.getRate(), actualFilm.getRate());
    }

    @Test
    void testDeleteLikeFilm() throws ValidationException {
        Film film = Film.builder()
                .description("фв фв")
                .duration(192L)
                .name("фв: фв фв")
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
                .name("вы1")
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
    void testSortPopularFilm() throws ValidationException {
        Film film = Film.builder()
                .description("aaaaa")
                .duration(192L)
                .name("вы: вы вы")
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
                .description("вы ыыыыы")
                .duration(192L)
                .name("ы ыыыы")
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
                .name("c1")
                .email("nnjh@come.0")
                .login("oooo")
                .birthday(LocalDate.of(1997, 07, 05))
                .build();
        User baseUser = userStorage.createUser(user);
        User user1 = User.builder()
                .name("???2")
                .email("nnjh@come.1")
                .login("oooo")
                .birthday(LocalDate.of(1997, 07, 05))
                .build();
        User baseUser1 = userStorage.createUser(user1);
        User user2 = User.builder()
                .name("???3")
                .email("nnjh@come.2")
                .login("oooo")
                .birthday(LocalDate.of(1997, 07, 05))
                .build();
        User baseUser2 = userStorage.createUser(user2);

        filmStorage.addLikeFilm(baseFilm1.getId(), baseUser.getId());
        filmStorage.addLikeFilm(baseFilm.getId(), baseUser1.getId());
        filmStorage.addLikeFilm(baseFilm1.getId(), baseUser2.getId());

        List<Film> actualFilms = filmStorage.sortPopularFilm(0);

        assertEquals(2, actualFilms.size());
        assertEquals(baseFilm1.getId(), actualFilms.get(0).getId());
        assertEquals(baseFilm.getId(), actualFilms.get(1).getId());

        List<Film> actualFilms1 = filmStorage.sortPopularFilm(1);
        assertEquals(1, actualFilms1.size());

    }

    @Test
    void testGetAllPopular() throws ValidationException {
        Film film = Film.builder()
                .description("????? ???????")
                .duration(192L)
                .name("??????: ???? ????")
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
                .description("????? ???????1111")
                .duration(192L)
                .name("??????: 1111")
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
                .name("???1")
                .email("nnjh@come.0")
                .login("oooo")
                .birthday(LocalDate.of(1997, 07, 05))
                .build();
        User baseUser = userStorage.createUser(user);
        User user1 = User.builder()
                .name("???2")
                .email("nnjh@come.1")
                .login("oooo")
                .birthday(LocalDate.of(1997, 07, 05))
                .build();
        User baseUser1 = userStorage.createUser(user1);
        User user2 = User.builder()
                .name("???3")
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