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
import ru.filmogram.model.Genre;
import ru.filmogram.model.Mpa;
import ru.filmogram.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.filmogram.util.Util.makeMpa;

@Repository
@Primary
public class FilmDbStorageImpl implements FilmStorage {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private GenreDbStorageImpl genreDbStorage;

    @Autowired
    private MpaDbStorageImpl mpaDbStorage;

    public FilmDbStorageImpl(JdbcTemplate jdbcTemplate,
                             GenreDbStorageImpl genreDbStorage,
                             MpaDbStorageImpl mpaDbStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreDbStorage = genreDbStorage;
        this.mpaDbStorage = mpaDbStorage;
    }

    @Override
    public Film createFilm(Film film) throws ValidationException {

        Mpa mpa = mpaDbStorage.getMpaId(film.getRating().getId());

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("film")
                .usingGeneratedKeyColumns("film_id");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("film_name", film.getName());
        parameters.put("description", film.getDescription());
        parameters.put("releaseDate", film.getReleaseDate());
        parameters.put("duration", film.getDuration());
        parameters.put("rating_id", mpa.getId());
        parameters.put("rate", film.getRate());

        Long filmId = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();

        List<Genre> genres = new ArrayList<>();
        for (Genre genre : film.getGenres()) {
            genres.add(genreDbStorage.getGenreId(genre.getId()));
            jdbcTemplate.update("INSERT INTO genre_film (film_id, genre_id) VALUES (?, ?)",
                    filmId, genre.getId());
        }

        String query = "SELECT f.film_id," +
                "              f.film_name," +
                "              f.description," +
                "              f.releaseDate," +
                "              f.duration," +
                "              f.rate," +
                "              FROM film AS f" +
                "              WHERE f.film_id = ?";
        Film finalFilm = jdbcTemplate.queryForObject(
                query, new Object[]{filmId}, new FilmMapper());
        finalFilm.setRating(mpa);
        finalFilm.setGenres(genres);
        finalFilm.setRate(film.getRate());
        return finalFilm;
    }

    @Override
    public Film getFilmId(Long id) {

        Film finalFilm = null;
        try {
            SqlRowSet filmRows = jdbcTemplate.queryForRowSet(
                    "          SELECT f.film_id," +
                    "              f.film_name," +
                    "              f.description," +
                    "              f.releaseDate," +
                    "              f.duration," +
                    "              f.rate," +
                    "              r.rating_name," +
                    "              r.rating_id," +
                    "              r.rating_name," +
                    "              FROM film AS f" +
                    "              LEFT JOIN genre_film AS gf ON f.film_id = gf.film_id" +
                    "              LEFT JOIN genre AS g ON gf.genre_id = g.genre_id" +
                    "              LEFT JOIN rating AS r ON f.rating_id = r.rating_id" +
                    "              WHERE f.film_id = ?", id);
            if (filmRows.next()) {
                finalFilm = Film.builder()
                        .id(filmRows.getLong("film_id"))
                        .name(filmRows.getString("film_name"))
                        .description(filmRows.getString("description"))
                        .duration(Long.parseLong(filmRows.getString("duration")))
                        .releaseDate(LocalDate.parse(filmRows.getString("releaseDate")))
                        .rate(filmRows.getInt("rate"))
                        .rating(makeMpa(filmRows.getLong("rating_id"),  filmRows.getString("rating_name")))
                        .build();
            }

            List<Long> genresId = jdbcTemplate.queryForList(
                    "SELECT genre_id FROM genre_film WHERE film_id = ?", Long.class, id);

            List<Genre> genres = new ArrayList<>();
            if (genresId.get(0) != null) {
                for (Long genreId : genresId) {
                    genres.add(genreDbStorage.getGenreId(genreId));
                }
            }
            finalFilm.setGenres(genres);

        } catch (DataAccessException e) {
            throw new ObjectNotFoundException(String.format("Фильм {} не найден", id));
        }
        return finalFilm;
    }

    @Override
    public List<Film> findAllFilm() {
        ArrayList<Film> films = new ArrayList<>();

//        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(
//                "SELECT f.film_id," +
//                        "f.film_name, " +
//                        "f.description," +
//                        "f.releaseDate," +
//                        "f.duration," +
//                        "GROUP_CONCAT(g.name) AS genreFilm," +
//                        "r.name, " +
//                        "GROUP_CONCAT(l.user_id) AS listOfUsersLike " +
//                        "FROM film AS f " +
//                        " LEFT JOIN genre_film AS gf ON f.film_id = gf.film_id" +
//                        " LEFT JOIN genre AS g ON gf.genre_id = g.genre_id" +
//                        " LEFT JOIN rating AS r ON f.rating_id = r.rating_id" +
//                        " LEFT JOIN likes AS l ON f.film_id = l.film_id" +
//                        " GROUP BY f.film_name" +
//                        " ORDER BY f.film_id ASC");
//        while (filmRows.next()) {
//            Film film = Film.builder()
//                    .id(filmRows.getLong("film_id"))
//                    .name(filmRows.getString("film_name"))
//                    .description(filmRows.getString("description"))
//                    .duration(Long.parseLong(filmRows.getString("duration")))
//                    .releaseDate(LocalDate.parse(filmRows.getString("releaseDate")))
//                    .genres(checkNoGenre(filmRows.getString("genreFilm")))
//                    .rating(filmRows.getString("name"))
//                    .build();
//            films.add(film);
//        }
//        return films;
        return null;
    }

    @Override
    public Film updateFilm(Film film) throws ValidationException {

        List<Integer> filmId = jdbcTemplate.queryForList(
                "SELECT film_id FROM film WHERE film_id = ?", Integer.class, film.getId());

        if (filmId.size() == 0) {
            log.info("Фильм не найден");
            throw new  ObjectNotFoundException("Фильм не найден");
        }

        ArrayList<Genre> genresOldList = new ArrayList<>();
        ArrayList<Genre> genreFinal = new ArrayList<>();

        List<Long> genreListId = jdbcTemplate.queryForList(
                "SELECT genre_id FROM genre_film WHERE film_id = ?", Long.class, film.getId());
        if (genreListId.size() > 0) {
            if (genreListId.get(0) != null) {
                for (Long genreId : genreListId) {
                    genresOldList.add(genreDbStorage.getGenreId(genreId));
                }
            }

        for (Genre genre : film.getGenres()) {
            if (!genresOldList.contains(genre)) {
                jdbcTemplate.update("INSERT INTO genre_film (film_id, genre_id) VALUES (?, ?)",
                        film.getId(), genre.getId());

            }
            genreFinal.add(genreDbStorage.getGenreId(genre.getId()));
        }

        for (Genre oldGenre : genresOldList) {
            if (!genreFinal.contains(oldGenre)) {
                jdbcTemplate.update(
                        "delete from genre_film where film_id = ? AND genre_id = ?",
                        film.getId(), oldGenre.getId());
            }
        }
        }

        Mpa mpa = mpaDbStorage.getMpaId(film.getRating().getId());

        jdbcTemplate.update(
                "UPDATE film SET film_name = ?, " +
                        "description = ?, " +
                        "releaseDate = ?, " +
                        "duration = ?, " +
                        "rating_id = ?, " +
                        "rate = ?" +
                        "WHERE film_id = ?",
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                mpa.getId(),
                film.getRate(),
                film.getId());

        Film finalFilm = null;
        try {
            String query = "SELECT f.film_id," +
                    "              f.film_name," +
                    "              f.description," +
                    "              f.releaseDate," +
                    "              f.duration," +
                    "              f.rate" +
                    "              FROM film AS f" +
                    "              WHERE f.film_id = ?";
            finalFilm = jdbcTemplate.queryForObject(
                    query, new Object[]{film.getId()}, new FilmMapper());
            finalFilm.setRating(mpa);
            finalFilm.setGenres(genreFinal);
        } catch (DataAccessException e) {
            throw new ObjectNotFoundException("Фильм {} не найден");
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

//        Integer realCount = count;
//        ArrayList<Film> films = new ArrayList<>();
//        if (count == null || count == 0) {
//            realCount = 10;
//        } else if (count < 0) {
//            throw new ValidationException("Значение не может быть отрицательным");
//        }
//
//        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(
//                "SELECT f.film_id, " +
//                        " f.film_name, " +
//                        " f.description, " +
//                        " f.duration, " +
//                        " f.releaseDate, " +
//                        " GROUP_CONCAT(g.name) AS genreFilm, " +
//                        " r.name, " +
//                        " GROUP_CONCAT(l.user_id) AS listOfUsersLike, " +
//                        " COUNT(lk.user_id) AS likes " +
//                        " FROM film AS f " +
//                        " LEFT JOIN genre_film AS gf ON f.film_id = gf.film_id" +
//                        " LEFT JOIN genre AS g ON gf.genre_id = g.genre_id" +
//                        " LEFT JOIN rating AS r ON f.rating_id = r.rating_id" +
//                        " LEFT JOIN likes AS l ON f.film_id = l.film_id" +
//                        " LEFT JOIN likes AS lk ON f.film_id = lk.film_id" +
//                        " GROUP BY f.film_id " +
//                        " ORDER BY likes DESC " +
//                        " LIMIT ?", realCount);
//        while (filmRows.next()) {
//            Film film = Film.builder()
//                    .id(filmRows.getLong("film_id"))
//                    .name(filmRows.getString("film_name"))
//                    .description(filmRows.getString("description"))
//                    .duration(Long.parseLong(filmRows.getString("duration")))
//                    .releaseDate(LocalDate.parse(filmRows.getString("releaseDate")))
//                    .genres(checkNoGenre(filmRows.getString("genreFilm")))
//                    .rating(filmRows.getString("name"))
//                    .likes(Stream.of(filmRows.getString("listOfUsersLike").split(","))
//                            .map(Long::parseLong)
//                            .collect(Collectors.toSet()))
//                    .build();
//            films.add(film);
//        }
//        return films;
        return null;
    }

    @Override
    public List<Film> getAllPopular() {
        ArrayList<Film> films = new ArrayList<>();

//        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(
//                "SELECT f.film_id, " +
//                        " f.film_name, " +
//                        " f.description, " +
//                        " f.duration, " +
//                        " f.releaseDate, " +
//                        " GROUP_CONCAT(g.name) AS genreFilm, " +
//                        " r.name, " +
//                        " GROUP_CONCAT(l.user_id) AS listOfUsersLike, " +
//                        " COUNT(lk.user_id) AS likes " +
//                        " FROM film AS f " +
//                        " LEFT JOIN genre_film AS gf ON f.film_id = gf.film_id" +
//                        " LEFT JOIN genre AS g ON gf.genre_id = g.genre_id" +
//                        " LEFT JOIN rating AS r ON f.rating_id = r.rating_id" +
//                        " LEFT JOIN likes AS l ON f.film_id = l.film_id" +
//                        " LEFT JOIN likes AS lk ON f.film_id = lk.film_id" +
//                        " GROUP BY f.film_id " +
//                        " ORDER BY likes DESC ");
//        while (filmRows.next()) {
//            Film film = Film.builder()
//                    .id(filmRows.getLong("film_id"))
//                    .name(filmRows.getString("film_name"))
//                    .description(filmRows.getString("description"))
//                    .duration(Long.parseLong(filmRows.getString("duration")))
//                    .releaseDate(LocalDate.parse(filmRows.getString("releaseDate")))
//                    .genres(checkNoGenre(filmRows.getString("genreFilm")))
//                    .rating(filmRows.getString("name"))
//                    .likes(Stream.of(filmRows.getString("listOfUsersLike").split(","))
//                            .map(Long::parseLong)
//                            .collect(Collectors.toSet()))
//                    .build();
//            films.add(film);
//        }
//        return films;
        return null;
    }
}
