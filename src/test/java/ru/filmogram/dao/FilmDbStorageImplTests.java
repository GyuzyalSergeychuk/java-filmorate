package ru.filmogram.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.Film;
import ru.filmogram.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageImplTests {

    Film film;
    @Autowired
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    FilmStorage filmStorage;

    @BeforeEach
    public void beforeEach() {
        jdbcTemplate.execute(
                "INSERT INTO rating (" +
                        "name)" +
                        "VALUES ('GP-13')");
        jdbcTemplate.execute(
            "INSERT INTO film (" +
                    "film_name," +
                    "description," +
                    "releaseDate," +
                    "duration," +
                    "rating_id)" +
                    "VALUES ('Аватар: Путь воды', 'После принятия образа аватара солдат', '2022-12-06', 192, 1)");
        jdbcTemplate.execute(
                "INSERT INTO genre (" +
                        "name)" +
                        "VALUES ('приключение')");
        jdbcTemplate.execute(
                "INSERT INTO genre (" +
                        "name)" +
                        "VALUES ('фантастика')");
        jdbcTemplate.execute(
                "INSERT INTO genre (" +
                        "name)" +
                        "VALUES ('боевик')");
        jdbcTemplate.execute(
                "INSERT INTO genre_film (" +
                        "film_id," +
                        "genre_id)" +
                        "VALUES (1, 1)");
        jdbcTemplate.execute(
                "INSERT INTO genre_film (" +
                        "film_id," +
                        "genre_id)" +
                        "VALUES (1, 3)");
        jdbcTemplate.execute(
                "INSERT INTO users (" +
                        "name," +
                        "email," +
                        "login," +
                        "birthday)" +
                        "VALUES ('Том', 'nnjh@come.ru', 'nnn', '1985-05-13')");
        jdbcTemplate.execute(
                "INSERT INTO likes (" +
                        "film_id," +
                        "user_id)" +
                        "VALUES (1, 1)");

        film = Film.builder()
                .id(1L)
                .description("После принятия образа аватара солдат Джейк Салли становится предводителем народа на'ви. " +
                        "Когда на Пандору возвращаются до зубов вооруженные земляне, Джейк готов дать им отпор.")
                .duration(192L)
                .name("Аватар: Путь воды")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .rating("GP-13")
                .genre(Set.of("приключение", "боевик"))
                .likes(Set.of(1L))
                .build();
    }

    @Test
    void findAllFilm() {
        List<Film> films = filmStorage.findAllFilm();

        assertEquals(1, films.get(0).getId());
        assertEquals("GP-13", films.get(0).getRating());
    }

    @Test
    void createFilm() throws ValidationException {
        filmStorage.createFilm(film);

        assertEquals(film.getId(), 1);
        assertEquals(film.getGenre(), Set.of("боевик","приключение"));
        assertEquals(film.getRating(), "GP-13");
    }

    @Test
    void updateFilm() throws ValidationException {
        Film expectedFilm = Film.builder()
                .id(1L)
                .description("После принятия образа аватара солдат Джейк Салли становится предводителем народа на'ви. " +
                        "Когда на Пандору возвращаются до зубов вооруженные земляне, Джейкrrrrrr готов дать им отпор.")
                .duration(192L)
                .name("Аватар: Путь воды")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .rating("GP-13")
                .genre(Set.of("приключение", "боевик", "фантастика"))
                .likes(Set.of(1L))
                .build();

        filmStorage.updateFilm(expectedFilm);

        assertEquals(film.getId(), 1);
        assertEquals(expectedFilm.getGenre(), film.getGenre());

    }

    @Test
    void addLikeFilm() {
    }

    @Test
    void deleteLikeFilm() {
    }

    @Test
    void sortPopularFilm() {
    }

    @Test
    void getAllPopular() {
    }

    @Test
    void getFilmId() {
    }
}