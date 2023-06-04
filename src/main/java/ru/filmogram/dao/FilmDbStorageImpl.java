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
import ru.filmogram.mapper.FilmMapperLikes;
import ru.filmogram.model.Film;
import ru.filmogram.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.filmogram.util.Util.checkNoGenre;

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
        } else {
            List<Integer> ratingId =jdbcTemplate.query(
                    "SELECT rating_id FROM rating WHERE name = ?",
                    (resultSet, rowNum) -> resultSet.getInt("rating_id"),
                    film.getRating());
            lastRatingId = ratingId.get(0);
        }

        for (String genre : film.getGenre()) {
            List<String> genreFilm = jdbcTemplate.query(
                    "SELECT name FROM genre WHERE name = ?",
                    (resultSet, rowNum) -> resultSet.getString("name"),
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

        String query = "SELECT f.film_id," +
                "              f.film_name," +
                "              f.description," +
                "              f.releaseDate," +
                "              f.duration," +
                "              GROUP_CONCAT(g.name) AS genreFilm," +
                "              r.name" +
                "              FROM film AS f" +
                "              LEFT JOIN genre_film AS gf ON f.film_id = gf.film_id" +
                "              LEFT JOIN genre AS g ON gf.genre_id = g.genre_id" +
                "              LEFT JOIN rating AS r ON f.rating_id = r.rating_id" +
                "              WHERE f.film_id = ?";
        Film finalFilm = jdbcTemplate.queryForObject(
                query, new Object[]{lastFilmId}, new FilmMapper());
        return finalFilm;
    }

    @Override
    public Film getFilmId(Long id) {

        Film finalFilm = null;
        try {
            String query = "SELECT f.film_id," +
                    "              f.film_name," +
                    "              f.description," +
                    "              f.releaseDate," +
                    "              f.duration," +
                    "              GROUP_CONCAT(g.name) AS genreFilm," +
                    "              r.name" +
                    "              FROM film AS f" +
                    "              LEFT JOIN genre_film AS gf ON f.film_id = gf.film_id" +
                    "              LEFT JOIN genre AS g ON gf.genre_id = g.genre_id" +
                    "              LEFT JOIN rating AS r ON f.rating_id = r.rating_id" +
                    "              WHERE f.film_id = ?";
            finalFilm = jdbcTemplate.queryForObject(
                    query, new Object[]{id}, new FilmMapper());
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
                    .duration(Long.parseLong(filmRows.getString("duration")))
                    .releaseDate(LocalDate.parse(filmRows.getString("releaseDate")))
                    .genre(checkNoGenre(filmRows.getString("genreFilm")))
                    .rating(filmRows.getString("name"))
                    .build();
            films.add(film);
        }
        return films;
    }

    @Override
    public Film updateFilm(Film film) throws ValidationException {

        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT film_id FROM film WHERE film_id = ?", film.getId());

        if (filmRows == null) {
            log.info("Фильм {} не найден", film.getId());
            return null;
        } else {
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
                simpleJdbcInsertRating.execute(parametersRating);
            }

            for (String genre : film.getGenre()) {
                List<String> genreFilm = jdbcTemplate.query(
                        "SELECT name FROM genre WHERE name = ?",
                        (resultSet, rowNum) -> resultSet.getString("name"),
                        genre);
                if (genreFilm.isEmpty()) {
                    String queryGenre = "INSERT INTO genre (name) VALUES (?)";
                    jdbcTemplate.update(queryGenre, genre);
                }
            }

            ArrayList<String> genreFinal = new ArrayList<>();
            ArrayList<String> genreOldList = new ArrayList<>();

            List<String> genreListId = jdbcTemplate.queryForList(
                    "SELECT genre_id FROM genre_film WHERE film_id = ?", String.class, film.getId());
            if (!genreListId.isEmpty()) {
                for (String id : genreListId) {
                    List<String> genreName = jdbcTemplate.queryForList(
                            "SELECT name FROM genre WHERE genre_id = ?", String.class, id);
                    if (!genreName.isEmpty()) {
                        genreOldList.add(genreName.get(0));
                    }
                    for (String genre : film.getGenre()) {
                        if (genreOldList.contains(genre)) {
                            genreFinal.add(genre);
                        } else {
                            List<String> genreFilm = jdbcTemplate.query(
                                    "SELECT name FROM genre WHERE name = ?",
                                    (resultSet, rowNum) -> resultSet.getString("name"),
                                    genre);
                            if (genreFilm.isEmpty()) {
                                String queryGenre = "INSERT INTO genre (name) VALUES (?)";
                                jdbcTemplate.update(queryGenre, genre);
                            }
                            jdbcTemplate.update("INSERT INTO genre_film (film_id, genre_id) VALUES (?, " +
                                    "(SELECT genre_id FROM genre WHERE name = ?))", film.getId(), genre);
                            genreFinal.add(genre);
                        }
                    }
                    for (String genreOld : genreOldList) {
                        if (!genreFinal.contains(genreOld)) {
                            List<Integer> genreid = jdbcTemplate.queryForList(
                                    "SELECT genre_id FROM genre WHERE name = ?",
                                    Integer.class, genreOld);
                            jdbcTemplate.update(
                                    "delete from genre_film where film_id = ? AND genre_id = ?",
                                    film.getId(), genreid.get(0));
                        }
                    }
                }
                List<Integer> ratingId = jdbcTemplate.queryForList(
                        "SELECT rating_id FROM rating WHERE name = ?", Integer.class, film.getRating());
                jdbcTemplate.update(
                        "UPDATE film SET film_name = ?, " +
                                "description = ?, " +
                                "releaseDate = ?, " +
                                "duration = ?, " +
                                "rating_id =? " +
                                "WHERE film_id = ?",
                        film.getName(),
                        film.getDescription(),
                        film.getReleaseDate(),
                        film.getDuration(),
                        ratingId.get(0),
                        film.getId());
            }
        }
        Film finalFilm = null;
        try {
            String query =
                    "              SELECT f.film_id," +
                            "              f.film_name," +
                            "              f.description," +
                            "              f.releaseDate," +
                            "              f.duration," +
                            "              GROUP_CONCAT(g.name) AS genreFilm," +
                            "              r.name" +
                            "              FROM film AS f" +
                            "              LEFT JOIN genre_film AS gf ON f.film_id = gf.film_id" +
                            "              LEFT JOIN genre AS g ON gf.genre_id = g.genre_id" +
                            "              LEFT JOIN rating AS r ON f.rating_id = r.rating_id" +
                            "              WHERE f.film_id = ?";
            finalFilm = jdbcTemplate.queryForObject(
                    query, new Object[]{film.getId()}, new FilmMapper());
        } catch (DataAccessException e) {
            throw new ObjectNotFoundException(String.format("Фильм {} не найден", film.getId()));
        }
        return finalFilm;
    }

    @Override
    public Film addLikeFilm(Long filmId, Long userId) throws ValidationException {

        try {
            Integer existingRecordCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?",
                    Integer.class,
                    filmId, userId);
            if (existingRecordCount != null && existingRecordCount > 0) {
                log.info("Like фильма {} пользователем {} уже ранее был осуществлен", filmId, userId);
                String query =
                                " SELECT f.film_id," +
                                " f.film_name," +
                                " f.description," +
                                " f.releaseDate," +
                                " f.duration," +
                                " GROUP_CONCAT(g.name) AS genreFilm," +
                                " r.name," +
                                " GROUP_CONCAT(u.user_id) AS listOfUsersLike" +
                                " FROM film AS f" +
                                " LEFT JOIN genre_film AS gf ON f.film_id = gf.film_id" +
                                " LEFT JOIN genre AS g ON gf.genre_id = g.genre_id" +
                                " LEFT JOIN rating AS r ON f.rating_id = r.rating_id" +
                                " LEFT JOIN likes AS l ON f.film_id = l.film_id" +
                                " LEFT JOIN users AS u ON l.user_id = u.user_id" +
                                " WHERE f.film_id = ?";
                Film finalFilm = jdbcTemplate.queryForObject(
                        query, new Object[]{filmId}, new FilmMapperLikes());
                return finalFilm;
            }

            jdbcTemplate.update(
                    "INSERT INTO likes (film_id, user_id) VALUES (?, ?)",
                    filmId, userId);

                String query =
                        "  SELECT f.film_id," +
                                " f.film_name," +
                                " f.description," +
                                " f.releaseDate," +
                                " f.duration," +
                                " GROUP_CONCAT(g.name) AS genreFilm," +
                                " r.name," +
                                " GROUP_CONCAT(u.user_id) AS listOfUsersLike" +
                                " FROM film AS f" +
                                " LEFT JOIN genre_film AS gf ON f.film_id = gf.film_id" +
                                " LEFT JOIN genre AS g ON gf.genre_id = g.genre_id" +
                                " LEFT JOIN rating AS r ON f.rating_id = r.rating_id" +
                                " LEFT JOIN likes AS l ON f.film_id = l.film_id" +
                                " LEFT JOIN users AS u ON l.user_id = u.user_id" +
                                " WHERE f.film_id = ?";
                Film finalFilm = jdbcTemplate.queryForObject(
                        query, new Object[]{filmId}, new FilmMapperLikes());
                return finalFilm;
            } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
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
            return true;
        } else {
            log.info("like фильма {} пользователя {} не найден", filmId, userId);
            return false;
        }
    }

    @Override
    public List<Film> sortPopularFilm(Integer count) throws ValidationException {

        Integer realCount = count;
        ArrayList<Film> films = new ArrayList<>();
        if (count == null || count == 0) {
            realCount = 10;
        } else if (count < 0) {
            throw new ValidationException("Значение не может быть отрицательным");
        }

        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(
                "SELECT f.film_id, " +
                        " f.film_name, " +
                        " f.description, " +
                        " f.duration, " +
                        " f.releaseDate, " +
                        " GROUP_CONCAT(g.name) AS genreFilm, " +
                        " r.name, " +
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
                    .duration(Long.parseLong(filmRows.getString("duration")))
                    .releaseDate(LocalDate.parse(filmRows.getString("releaseDate")))
                    .genre(checkNoGenre(filmRows.getString("genreFilm")))
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
    public List<Film> getAllPopular() {
        ArrayList<Film> films = new ArrayList<>();

        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(
                "SELECT f.film_id, " +
                        " f.film_name, " +
                        " f.description, " +
                        " f.duration, " +
                        " f.releaseDate, " +
                        " GROUP_CONCAT(g.name) AS genreFilm, " +
                        " r.name, " +
                        " GROUP_CONCAT(l.user_id) AS listOfUsersLike, " +
                        " COUNT(lk.user_id) AS likes " +
                        " FROM film AS f " +
                        " LEFT JOIN genre_film AS gf ON f.film_id = gf.film_id" +
                        " LEFT JOIN genre AS g ON gf.genre_id = g.genre_id" +
                        " LEFT JOIN rating AS r ON f.rating_id = r.rating_id" +
                        " LEFT JOIN likes AS l ON f.film_id = l.film_id" +
                        " LEFT JOIN likes AS lk ON f.film_id = lk.film_id" +
                        " GROUP BY f.film_id " +
                        " ORDER BY likes DESC ");
        while (filmRows.next()) {
            Film film = Film.builder()
                    .id(filmRows.getLong("film_id"))
                    .name(filmRows.getString("film_name"))
                    .description(filmRows.getString("description"))
                    .duration(Long.parseLong(filmRows.getString("duration")))
                    .releaseDate(LocalDate.parse(filmRows.getString("releaseDate")))
                    .genre(checkNoGenre(filmRows.getString("genreFilm")))
                    .rating(filmRows.getString("name"))
                    .likes(Stream.of(filmRows.getString("listOfUsersLike").split(","))
                            .map(Long::parseLong)
                            .collect(Collectors.toSet()))
                    .build();
            films.add(film);
        }
        return films;
    }

    public void deleteAllTables(){
        jdbcTemplate.update("delete from friends");
        jdbcTemplate.update("delete from likes");
        jdbcTemplate.update("delete from genre_film");
        jdbcTemplate.update("delete from film");
        jdbcTemplate.update("delete from users");
        jdbcTemplate.update("delete from genre");
        jdbcTemplate.update("delete from rating");

    }
}
