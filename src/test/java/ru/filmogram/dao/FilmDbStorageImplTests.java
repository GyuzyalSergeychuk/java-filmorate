package ru.filmogram.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.Film;
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
    @Autowired
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    FilmStorage filmStorage;
    @Autowired
    UserStorage userStorage;

    @BeforeEach
    public void beforeEach() throws ValidationException {

        film = Film.builder()
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
    void createFilm() throws ValidationException {
        // Вызов метода createFilm
        Film createdFilm = filmStorage.createFilm(film);

        // Проверка, что фильм успешно создан
        Assertions.assertNotNull(createdFilm.getId());

        // Проверка, что данные фильма сохранены в базе данных
        String query = "SELECT * FROM film WHERE film_id = ?";
        Object[] params = {createdFilm.getId()};
        Film savedFilm = jdbcTemplate.queryForObject(query, params, new BeanPropertyRowMapper<>(Film.class));
        Assertions.assertEquals(film.getName(), savedFilm.getName());
        Assertions.assertEquals(film.getDescription(), savedFilm.getDescription());
        Assertions.assertEquals(film.getReleaseDate(), savedFilm.getReleaseDate());
        Assertions.assertEquals(film.getDuration(), savedFilm.getDuration());
        Assertions.assertEquals(film.getRating(), savedFilm.getRating());
        Assertions.assertEquals(film.getGenre(), savedFilm.getGenre());
    };

    @Test
    void findAllFilm() {
        List<Film> films = filmStorage.findAllFilm();

        assertEquals(1, films.get(0).getId());
        assertEquals("GP-13", films.get(0).getRating());
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

        Film actualFilm = filmStorage.updateFilm(expectedFilm);

        assertEquals(1, actualFilm.getId());
        assertEquals(expectedFilm.getGenre(), actualFilm.getGenre());
    }

    @Test
    void addLikeFilm() throws ValidationException {
        // Подготовка данных для теста
        Long filmId = 1L;
        Long userId = 2L;

        // Выполнение метода
        Film resultFilm = filmStorage.addLikeFilm(filmId, userId);

        // Проверка результата
        assertEquals(filmId, resultFilm.getId());
    }

    @Test
    void deleteLikeFilm() {
        // Подготовка данных для теста
        Long filmId = 1L;
        Long userId = 1L;

        // Выполнение метода
        boolean result = filmStorage.deleteLikeFilm(filmId, userId);

        // Проверка результата
        assertEquals(true, filmStorage.deleteLikeFilm(filmId, userId));
    }

    @Test
    void sortPopularFilm() throws ValidationException {
        List<Film> sortFilm = filmStorage.sortPopularFilm(2);
        assertEquals("Аватар: Путь воды", sortFilm.get(0).getName());
    }

    @Test
    void getAllPopular() {

    }

    @Test
    void getFilmId() {
        Long filmId = 1L;

        assertEquals(filmId, filmStorage.getFilmId(1l));
    }
}