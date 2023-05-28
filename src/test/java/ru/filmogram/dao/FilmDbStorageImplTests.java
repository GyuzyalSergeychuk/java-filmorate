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
import ru.filmogram.model.User;
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
    User user;
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
                    "VALUES ('������: ���� ����', '����� �������� ������ ������� ������', '2022-12-06', 192, 1)");
        jdbcTemplate.execute(
                "INSERT INTO genre (" +
                        "name)" +
                        "VALUES ('�����������')");
        jdbcTemplate.execute(
                "INSERT INTO genre (" +
                        "name)" +
                        "VALUES ('����������')");
        jdbcTemplate.execute(
                "INSERT INTO genre (" +
                        "name)" +
                        "VALUES ('������')");
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
                        "VALUES ('���', 'nnjh@come.ru', 'nnn', '1985-05-13')");
        jdbcTemplate.execute(
                "INSERT INTO users (" +
                        "name," +
                        "email," +
                        "login," +
                        "birthday)" +
                        "VALUES ('���2', 'nnjh@come.r2', 'nnn2', '1985-05-13')");
        jdbcTemplate.execute(
                "INSERT INTO likes (" +
                        "film_id," +
                        "user_id)" +
                        "VALUES (1, 1)");

        film = Film.builder()
                .id(1L)
                .description("����� �������� ������ ������� ������ ����� ����� ���������� ������������� ������ ��'��. " +
                        "����� �� ������� ������������ �� ����� ����������� �������, ����� ����� ���� �� �����.")
                .duration(192L)
                .name("������: ���� ����")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .rating("GP-13")
                .genre(Set.of("�����������", "������"))
                .likes(Set.of(1L))
                .build();

        User user;
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

        assertEquals(1, film.getId());
        assertEquals(Set.of("������","�����������"), film.getGenre());
        assertEquals("GP-13", film.getRating());
    }

    @Test
    void updateFilm() throws ValidationException {
        Film expectedFilm = Film.builder()
                .id(1L)
                .description("����� �������� ������ ������� ������ ����� ����� ���������� ������������� ������ ��'��. " +
                        "����� �� ������� ������������ �� ����� ����������� �������, �����rrrrrr ����� ���� �� �����.")
                .duration(192L)
                .name("������: ���� ����")
                .releaseDate(LocalDate.of(2022, 12, 06))
                .rating("GP-13")
                .genre(Set.of("�����������", "������", "����������"))
                .likes(Set.of(1L))
                .build();

        Film actualFilm = filmStorage.updateFilm(expectedFilm);

        assertEquals(1, actualFilm.getId());
        assertEquals(expectedFilm.getGenre(), actualFilm.getGenre());
    }

    @Test
    void addLikeFilm() throws ValidationException {
        // ���������� ������ ��� �����
        Long filmId = 1L;
        Long userId = 2L;

        // ���������� ������
        Film resultFilm = filmStorage.addLikeFilm(filmId, userId);

        // �������� ����������
        assertEquals(filmId, resultFilm.getId());
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