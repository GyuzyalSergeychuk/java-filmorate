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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageImplTests {

    @Autowired
    FilmStorage filmStorage;
    @Autowired
    UserStorage userStorage;

    @AfterEach
    void init() {
        filmStorage.deleteAllTables();
    }

    @Test
    void createFilm() throws ValidationException {
        Film film = Film.builder()
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
        Film film = Film.builder()
                .description("текст хороший")
                .duration(192L)
                .name("Аватар: Путь воды")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .rating("GP-666")
                .genre(Set.of("приключение", "боевик"))
                .build();
        Film expectedFilm1 = filmStorage.createFilm(film);
        Film film2 = Film.builder()
                .description("текст хороший1111")
                .duration(200L)
                .name("Аватар: Путь воды2")
                .releaseDate(LocalDate.of(2022, 12, 02))
                .rating("GP")
                .genre(Set.of("приключение", "боевик", "фантастика"))
                .build();
        Film expectedFilm2 = filmStorage.createFilm(film2);
        assertEquals(expectedFilm1, filmStorage.getFilmId(expectedFilm1.getId()));
        assertEquals(expectedFilm2, filmStorage.getFilmId(expectedFilm2.getId()));
    }

    @Test
    void findAllFilm() throws ValidationException {
        Film film1 = Film.builder()
                .description("текст хороший")
                .duration(192L)
                .name("Аватар: Путь воды")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .rating("GP-666")
                .genre(Set.of("приключение", "боевик"))
                .build();
        Film expectedFilm1 = filmStorage.createFilm(film1);
        Film film2 = Film.builder()
                .description("текст хороший1111")
                .duration(200L)
                .name("Аватар: Путь воды2")
                .releaseDate(LocalDate.of(2022, 12, 02))
                .rating("GP")
                .genre(Set.of("приключение", "боевик", "фантастика"))
                .build();
        Film expectedFilm2 = filmStorage.createFilm(film2);
        List<Film> expectedFilms = new ArrayList<>(List.of(expectedFilm1, expectedFilm2));
        List<Film> actualFilms = filmStorage.findAllFilm();

        assertEquals(expectedFilms, actualFilms);
        assertEquals("GP-666", actualFilms.get(0).getRating());
        assertEquals(Set.of("приключение","боевик", "фантастика"), actualFilms.get(1).getGenre());
    }

    @Test
    void updateFilm() throws ValidationException {
        Film baseFilm = Film.builder()
                .description("текст хороший1111")
                .duration(200L)
                .name("Аватар: Путь воды2")
                .releaseDate(LocalDate.of(2022, 12, 02))
                .rating("GP")
                .genre(Set.of("приключение", "боевик", "фантастика"))
                .build();
        Film baseFilmWithId = filmStorage.createFilm(baseFilm);
        Film baseFilmWithUpdate = Film.builder()
                .id(baseFilmWithId.getId())
                .description("новый")
                .duration(196L)
                .name("Аватар: ПутьНОВЫЙ")
                .releaseDate(LocalDate.of(2022, 12, 02))
                .rating("R")
                .genre(Set.of("приключение", "фантастика"))
                .build();

        Film actualFilm = filmStorage.updateFilm(baseFilmWithUpdate);

        assertEquals(baseFilmWithUpdate.getId(), actualFilm.getId());
        assertEquals(baseFilmWithUpdate.getGenre(), actualFilm.getGenre());
        assertEquals(baseFilmWithUpdate.getRating(), actualFilm.getRating());
        assertEquals(baseFilmWithUpdate.getDuration(), actualFilm.getDuration());
    }

    @Test
    void addLikeFilm() throws ValidationException {
        Film film = Film.builder()
                .description("текст хороший1111")
                .duration(200L)
                .name("Аватар: Путь воды2")
                .releaseDate(LocalDate.of(2022, 12, 02))
                .rating("GP")
                .genre(Set.of("приключение", "боевик", "фантастика"))
                .build();
        Film baseFilm = filmStorage.createFilm(film);
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

        Film film1 = filmStorage.addLikeFilm(baseFilm.getId(), baseUser.getId());
        Film film2 = filmStorage.addLikeFilm(baseFilm.getId(), baseUser2.getId());

        assertEquals(Set.of(baseUser.getId(), baseUser2.getId()), film2.getLikes());
    }

    @Test
    void deleteLikeFilm() throws ValidationException {
        Film film = Film.builder()
                .description("текст хороший1111")
                .duration(200L)
                .name("Аватар: Путь воды2")
                .releaseDate(LocalDate.of(2022, 12, 02))
                .rating("GP")
                .genre(Set.of("приключение", "боевик", "фантастика"))
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
                .description("текст хороший111")
                .duration(200L)
                .name("Аватар: Путь воды2")
                .releaseDate(LocalDate.of(2022, 12, 02))
                .rating("GP")
                .genre(Set.of("приключение", "боевик", "фантастика"))
                .build();
        Film baseFilm = filmStorage.createFilm(film);
        Film film1 = Film.builder()
                .description("текст хороший222")
                .duration(200L)
                .name("Аватар: Путь воды2")
                .releaseDate(LocalDate.of(2022, 12, 02))
                .rating("GP")
                .genre(Set.of("приключение", "фантастика"))
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

        assertEquals(baseFilm1.getId(), actualFilms.get(0).getId());
        assertEquals(baseFilm.getId(), actualFilms.get(1).getId());

        List<Film> actualFilms1 = filmStorage.sortPopularFilm(1);
        assertEquals(1, actualFilms1.size());
    }

    @Test
    void getAllPopular() throws ValidationException {
        Film film = Film.builder()
                .description("текст хороший111")
                .duration(200L)
                .name("Аватар: Путь воды2")
                .releaseDate(LocalDate.of(2022, 12, 02))
                .rating("GP")
                .genre(Set.of("приключение", "боевик", "фантастика"))
                .build();
        Film baseFilm = filmStorage.createFilm(film);
        Film film1 = Film.builder()
                .description("текст хороший222")
                .duration(200L)
                .name("Аватар: Путь воды2")
                .releaseDate(LocalDate.of(2022, 12, 02))
                .rating("GP")
                .genre(Set.of("приключение", "фантастика"))
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

        List<Film> actualFilms = filmStorage.getAllPopular();

        assertEquals(baseFilm1.getId(), actualFilms.get(0).getId());
        assertEquals(baseFilm.getId(), actualFilms.get(1).getId());
        assertEquals(2, actualFilms.size());
    }
}