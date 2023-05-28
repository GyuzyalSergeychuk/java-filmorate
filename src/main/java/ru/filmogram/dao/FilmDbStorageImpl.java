package ru.filmogram.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.Film;
import ru.filmogram.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
@Primary
public class FilmDbStorageImpl implements FilmStorage {

    @Autowired
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorageImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> findAllFilm() {
        ArrayList<Film> films = new ArrayList<>();

        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(
                "SELECT f.film_id," +
                        "f.film_name, " +
                        "f.description," +
                        "f.releaseDate," +
                        "f.duration," +
                        "GROUP_CONCAT(g.name) AS genreFilm," +
                        "r.name, " +
                        "GROUP_CONCAT(l.user_id) AS listOfUsersLike " +
                    "FROM film AS f " +
                    " LEFT JOIN genre_film AS gf ON f.film_id = gf.film_id" +
                    " LEFT JOIN genre AS g ON gf.genre_id = g.genre_id" +
                    " LEFT JOIN rating AS r ON f.rating_id = r.rating_id" +
                    " LEFT JOIN likes AS l ON f.film_id = l.film_id");
        while (filmRows.next()) {
            Film film= Film.builder()
                    .id(filmRows.getLong("film_id"))
                    .name(filmRows.getString("film_name"))
                    .description(filmRows.getString("description"))
                    .releaseDate(LocalDate.parse(filmRows.getString("releaseDate")))
                    .genre(Set.of(filmRows.getString("genreFilm").split(",")))
                    .rating(filmRows.getString("name"))
                    .likes(Stream.of(filmRows.getString("listOfUsersLike").split(","))
                            .map(Long::parseLong)
                            .collect(Collectors.toSet()))
                    .build();
            films.add(film);
        }
        return films;
    }

    @Override
    public Film createFilm(Film film) throws ValidationException {
        jdbcTemplate.update(
                "MERGE INTO rating (" +
                        "name) " +
                        "KEY(name)" +
                        "VALUES (?) ",
                film.getRating());
        for(String genre : film.getGenre()) {
            jdbcTemplate.update(
                    "MERGE INTO genre (" +
                            "name) " +
                            "KEY(name)" +
                            "VALUES (?) ",
                    genre);
        }
        jdbcTemplate.update(
                "INSERT INTO film (" +
                    "film_name," +
                    "description," +
                    "releaseDate," +
                    "duration)" +
                    "VALUES (?, ?, ?, ?)",
                film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration());
        return film;
    }

    @Override
    public Film updateFilm(Film film) throws ValidationException {
        // Обновление рейтинга
        jdbcTemplate.update(
                "MERGE INTO rating (name) KEY(name) VALUES (?)",
                film.getRating());

        // Обновление или вставка жанров
        for (String genreName : film.getGenre()) {
            // Проверяем, существует ли жанр с данным названием в базе данных
            Integer genreId = jdbcTemplate.queryForObject(
                    "SELECT genre_id FROM genre WHERE name = ?",
                    Integer.class,
                    genreName);

            // Если жанр не найден, вставляем новый жанр и получаем его genre_id
            if (genreId == null) {
                jdbcTemplate.update(
                        "INSERT INTO genre (name) VALUES (?)",
                        genreName);
                genreId = jdbcTemplate.queryForObject(
                        "SELECT genre_id FROM genre WHERE name = ?",
                        Integer.class,
                        genreName);
            }

            // Вставляем запись в таблицу genre_film
            jdbcTemplate.update(
                    "INSERT INTO genre_film (film_id, genre_id) VALUES (?, ?)",
                    film.getId(), genreId);
        }

        // Обновление информации о фильме
        jdbcTemplate.update(
                "UPDATE film SET film_name = ?, description = ?, releaseDate = ?, duration = ? WHERE film_id = ?",
                film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getId());

        return film;
    }

    @Override
    public Film addLikeFilm(Long filmId, Long userId) throws ValidationException {
        // Проверяем, существует ли уже запись с такими film_id и user_id
        Integer existingRecordCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?",
                Integer.class,
                filmId, userId);

        //TODO убрать это и сделать логирование, а так же сделать return с помощью селекта этого фильма
        if (existingRecordCount != null && existingRecordCount > 0) {
            // Запись уже существует, можно выполнить дополнительные действия или выбросить исключение
            // в зависимости от требований вашего приложения
            throw new ValidationException("Like record already exists.");
        }

        // Вставляем новую запись
        jdbcTemplate.update(
                "INSERT INTO likes (film_id, user_id) VALUES (?, ?)",
                filmId, userId);

        return Film.builder()
                .id(filmId)
                .build();
    }

    @Override
    public void deleteLikeFilm(Long filmId, Long userId) {
        jdbcTemplate.queryForRowSet( "delete from likes where film_id = ?, AND user_id = ?", filmId, userId);
    }

    @Override
    public List<Film> sortPopularFilm(Integer count) throws ValidationException {
        ArrayList<Film> films = new ArrayList<>();

        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(
                "SELECT f.film_id," +
                        "f.name," +
                        "f.description," +
                        "f.releaseDate," +
                        "GROUP_CONCAT(gf.name) AS genreFilm," +
                        "r.rating," +
                        "GROUP_CONCAT(l.user_id) AS listOfUsersLike" +
                        "(SELECT COUNT (user_id) AS popular" +
                                "FROM like" +
                                " GROUP BY film_id" +
                                "ORDER BY popular DESC) AS like" +
                        "FROM film AS f" +
                        "LEFT JOIN genre AS g ON gf.genre_id = g.genre_id" +
                        "LEFT JOIN genre_film AS gf ON f.genre_id = gf.genre_id" +
                        "LEFT JOIN rating AS r ON f.rating = r.rating" +
                        "LEFT JOIN like AS l ON f.film_id = l.film_id" +
                        "GROUP BY film_id" +
                        "ORDER BY like DESC" +
                        "LIMIT x?", count);
        while (filmRows.next()) {
            Film film= Film.builder()
                    .name(filmRows.getString("name"))
                    .description(filmRows.getString("description"))
                    .releaseDate(LocalDate.parse(filmRows.getString("releaseDate")))
                    .genre(Set.of(filmRows.getString("genre").split(",")))
                    .rating(filmRows.getString("rating"))
                    .likes(Stream.of(filmRows.getString("likes").split(","))
                            .map(Long::parseLong)
                            .collect(Collectors.toSet()))
                    .build();
            films.add(film);
        }
        return films;
    }

    @Override
    public List<Film> getAllPopular() {
        ArrayList<Film> films = new ArrayList<>();

        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(
                "SELECT f.film_id," +
                        "f.name," +
                        "f.description," +
                        "f.releaseDate," +
                        "GROUP_CONCAT(gf.name) AS genreFilm," +
                        "r.rating" +
                        "GROUP_CONCAT(l.user_id) AS listOfUsersLike" +
                        "(SELECT COUNT (user_id) AS popular" +
                        "FROM like" +
                        "GROUP BY film_id" +
                        "ORDER BY popular DESC) AS like " +
                        "FROM film AS f" +
                        "LEFT JOIN genre AS g ON gf.genre_id = g.genre_id" +
                        "LEFT JOIN genre_film AS gf ON f.genre_id = gf.genre_id" +
                        "LEFT JOIN rating AS r ON f.rating = r.rating" +
                        "LEFT JOIN like AS l ON f.film_id = l.film_id");
        while (filmRows.next()) {
            Film film= Film.builder()
                    .name(filmRows.getString("name"))
                    .description(filmRows.getString("description"))
                    .releaseDate(LocalDate.parse(filmRows.getString("releaseDate")))
                    .genre(Set.of(filmRows.getString("genre").split(",")))
                    .rating(filmRows.getString("rating"))
                    .likes(Stream.of(filmRows.getString("likes").split(","))
                            .map(Long::parseLong)
                            .collect(Collectors.toSet()))
                    .build();
            films.add(film);
        }
        return films;
    }

    @Override
    public Film getFilmId(Long id) {
        Film film = null;

        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(
                "SELECT f.film_id," +
                        "f.name," +
                        "f.description," +
                        "f.releaseDate," +
                        "g.genre," +
                        "r.rating," +
                        "GROUP_CONCAT(l.name) AS listOfUsersLike" +
                        "(SELECT COUNT (user_id) AS popular" +
                                "FROM like" +
                                "GROUP BY film_id" +
                                "ORDER BY popular DESC) AS like" +
                        "FROM film AS f" +
                        "LEFT JOIN genre AS g ON gf.genre_id = g.genre_id" +
                        "LEFT JOIN rating AS r ON f.rating = r.rating" +
                        "LEFT JOIN like AS l ON f.film_id = l.film_id" +
                        "WHERE f.film_id = ?;", id);
        if (filmRows.next()) {
            film = Film.builder()
                    .name(filmRows.getString("name"))
                    .description(filmRows.getString("description"))
                    .releaseDate(LocalDate.parse(filmRows.getString("releaseDate")))
                    .genre(Set.of(filmRows.getString("genre").split(",")))
                    .rating(filmRows.getString("rating"))
                    .likes(Stream.of(filmRows.getString("likes").split(","))
                            .map(Long::parseLong)
                            .collect(Collectors.toSet()))
                    .build();
        }
        return film;
    }
}
