package ru.filmogram.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.filmogram.exceptions.ObjectNotFoundException;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.mapper.FilmMapper;
import ru.filmogram.model.Film;
import ru.filmogram.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
@Primary
public class FilmDbStorageImpl implements FilmStorage {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorageImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film createFilm(Film film) throws ValidationException {
        Integer lastRatingId = 0;
        List<String> ratingNames = jdbcTemplate.query(
                "SELECT name FROM rating WHERE name = ?",
                (resultSet, rowNum) -> resultSet.getString("name"),
                film.getRating());
        if (ratingNames.isEmpty()) {
            SimpleJdbcInsert simpleJdbcInsertRating = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("rating")
                    .usingGeneratedKeyColumns("rating_id");
            Map<String, Object> parametersRating = new HashMap<String, Object>();
            parametersRating.put("name", film.getRating());
            lastRatingId = (Integer) simpleJdbcInsertRating.executeAndReturnKey(parametersRating);
        }

        for (String genre : film.getGenre()) {
            List<String> genreFilm = jdbcTemplate.query(
                    "SELECT name FROM genre WHERE name = ?",
                    (resultSet, rowNum) -> resultSet.getString("genre"),
                    genre);
            if (genreFilm.isEmpty()) {
                String queryGenre = "INSERT INTO genre (name) VALUES (?)";
                jdbcTemplate.update(queryGenre, genre);
            }
        }
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("film")
                .usingGeneratedKeyColumns("film_id");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("film_name", film.getName());
        parameters.put("description", film.getDescription());
        parameters.put("releaseDate", film.getReleaseDate());
        parameters.put("duration", film.getDuration());
        parameters.put("rating_id", lastRatingId);

        Long lastFilmId = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();

        for (String genre : film.getGenre()) {
            jdbcTemplate.update("INSERT INTO genre_film (film_id, genre_id) VALUES (?, " +
                    "(SELECT genre_id FROM genre WHERE name = ?))", lastFilmId, genre);
        }

        String query = "SELECT * FROM FILM WHERE film_id = ?";
//        String query = "SELECT f.film_id,"  +
//                "              f.film_name," +
//                "              f.description," +
//                "              f.releaseDate," +
//                "              f.duration," +
//                "              GROUP_CONCAT(g.name) AS genreFilm," +
//                "              r.name" +
//                "              FROM film AS f" +
//                "              LEFT JOIN genre_film AS gf ON f.film_id = gf.film_id" +
//                "              LEFT JOIN genre AS g ON gf.genre_id = g.genre_id" +
//                "              LEFT JOIN rating AS r ON f.rating_id = r.rating_id" +
//                "              WHERE film_id = ?";
        Film finalFilm = jdbcTemplate.queryForObject(
                query, new Object[] { lastFilmId }, new FilmMapper());

        return finalFilm;
            }

    @Override
    public Film getFilmId(Long id) {

        Film finalFilm = null;
        try {
            //TODO вот этот запрос как буд-то неправильный. Похоже нет джоина с GENRE-FILM
            finalFilm = jdbcTemplate.queryForObject(
                    "SELECT f.film_id, " +
                            "f.name, " +
                            "f.description, " +
                            "f.releaseDate, " +
                            "g.genre, " +
                            "r.rating, " +
                            "GROUP_CONCAT(l.name) AS listOfUsersLike " +
                            "(SELECT COUNT (user_id) AS popular " +
                            "FROM like " +
                            "GROUP BY film_id " +
                            "ORDER BY popular DESC) AS like " +
                            "FROM film AS f " +
                            "LEFT JOIN genre AS g ON gf.genre_id = g.genre_id " +
                            "LEFT JOIN rating AS r ON f.rating = r.rating " +
                            "LEFT JOIN like AS l ON f.film_id = l.film_id " +
                            "WHERE f.film_id = ?; ",
                    Film.class,
                    id);
        } catch (DataAccessException e) {
            throw new ObjectNotFoundException(String.format("Фильм {} не найден", id));
        }

        return finalFilm;
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
                        " LEFT JOIN likes AS l ON f.film_id = l.film_id" +
                        " GROUP BY f.film_name" +
                        " ORDER BY f.film_id ASC");
        while (filmRows.next()) {
            Film film = Film.builder()
                    .id(filmRows.getLong("film_id"))
                    .name(filmRows.getString("film_name"))
                    .description(filmRows.getString("description"))
                    .releaseDate(LocalDate.parse(filmRows.getString("releaseDate")))
                    .genre(checkNoGenre(filmRows.getString("genreFilm")))
                    .rating(filmRows.getString("name"))
                    .likes(Stream.of(Objects.requireNonNull(filmRows.getString("listOfUsersLike")).split(","))
                            .map(Long::parseLong)
                            .collect(Collectors.toSet()))
                    .build();
            films.add(film);
        }
        return films;
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
            log.info("Like фильма {} пользователем {} уже ранее был осуществлен", filmId, userId);
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
    public boolean deleteLikeFilm(Long filmId, Long userId) {

        Integer existingRecordCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?",
                Integer.class,
                filmId, userId);

        if (existingRecordCount != null && existingRecordCount > 0) {
            jdbcTemplate.update("delete from likes where film_id = ? AND user_id = ?", filmId, userId);
            log.info("like фильма {} пользователя {} был удален", filmId, userId);
        } else {
            log.info("like фильма {} пользователя {} не найден", filmId, userId);
        }
        return true;
    }

    @Override
    public List<Film> sortPopularFilm(Integer count) throws ValidationException {

        Integer realCount = count;
        ArrayList<Film> films = new ArrayList<>();
        if (count == null || count == 0) {
            realCount = 10;
        }
        else if (count < 0) {
            throw new ValidationException("Значение не может быть отрицательным");
        }

        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(
                    "SELECT f.film_id, " +
                            " f.film_name, " +
                            " f.description, " +
                            " f.releaseDate, " +
                            " GROUP_CONCAT(g.name) AS genreFilm, " +
                            " r.rating_id, " +
                            " GROUP_CONCAT(l.user_id) AS listOfUsersLike, " +
                            " COUNT(lk.user_id) AS likes " +
                            " FROM film AS f " +
                            " LEFT JOIN genre_film AS gf ON f.film_id = gf.film_id" +
                            " LEFT JOIN genre AS g ON gf.genre_id = g.genre_id" +
                            " LEFT JOIN rating AS r ON f.rating_id = r.rating_id" +
                            " LEFT JOIN likes AS l ON f.film_id = l.film_id" +
                            " LEFT JOIN likes AS lk ON f.film_id = lk.film_id" +
                            " GROUP BY f.film_id " +
                            " ORDER BY likes DESC " +
                            " LIMIT ?", realCount);
        while (filmRows.next()) {
                Film film = Film.builder()
                        .id(filmRows.getLong("film_id"))
                        .name(filmRows.getString("film_name"))
                        .description(filmRows.getString("description"))
                        .releaseDate(LocalDate.parse(filmRows.getString("releaseDate")))
                        .genre(checkNoGenre(filmRows.getString("genreFilm")))
                        .rating(Optional.ofNullable(filmRows.getString("name")).orElse(null))
                        .likes(Stream.of(Objects.requireNonNull(filmRows.getString("listOfUsersLike")).split(","))
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
                "SELECT f.film_id, " +
                        "f.film_name, " +
                        "f.description, " +
                        "f.releaseDate, " +
                        "GROUP_CONCAT(gf.name) AS genreFilm, " +
                        "r.rating_id " +
                        "GROUP_CONCAT(l.user_id) AS listOfUsersLike " +
                        "(SELECT COUNT (user_id) AS popular " +
                        "FROM likes " +
                        "GROUP BY film_id " +
                        "ORDER BY popular DESC) AS likes " +
                        "FROM film AS f " +
                        "LEFT JOIN genre_film AS gf ON f.genre_id = gf.genre_id " +
                        "LEFT JOIN genre AS g ON gf.genre_id = g.genre_id " +
                        "LEFT JOIN rating AS r ON f.rating = r.rating " +
                        "LEFT JOIN like AS l ON f.film_id = l.film_id");
        while (filmRows.next()) {
            Film film = Film.builder()
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

    private Set<String> checkNoGenre(String genreFilm) {
        if (genreFilm == null) {
            return Set.of();
        }
        return Set.of(genreFilm.split(","));
    }
}
