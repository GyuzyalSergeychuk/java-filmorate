package ru.filmogram.dao;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class FilmDbStorageImpl implements FilmStorage {


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

        Mpa mpa = mpaDbStorage.getMpaId(film.getMpa().getId());

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("film")
                .usingGeneratedKeyColumns("film_id");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("film_name", film.getName());
        parameters.put("description", film.getDescription());
        parameters.put("releaseDate", film.getReleaseDate());
        parameters.put("duration", film.getDuration());
        parameters.put("rating_id", mpa.getId());
        if (film.getRate() != null) {
            parameters.put("rate", film.getRate());
        }

        Long filmId = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();

        List<Genre> genres = new ArrayList<>();
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                if (!genres.contains(genre)) {
                    genres.add(genreDbStorage.getGenre(genre.getId()));
                    jdbcTemplate.update("INSERT INTO genre_film (film_id, genre_id) VALUES (?, ?)",
                            filmId, genre.getId());
                }
            }
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
        finalFilm.setMpa(mpa);
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
                        .mpa(makeMpa(filmRows.getLong("rating_id"), filmRows.getString("rating_name")))
                        .build();
            } else {
                throw new ObjectNotFoundException(String.format("Фильм %d не найден", id));
            }

            List<Long> genresId = jdbcTemplate.queryForList(
                    "SELECT genre_id FROM genre_film WHERE film_id = ?", Long.class, id);

            List<Genre> genres = new ArrayList<>();
            if (genresId.size() > 0) {
                for (Long genreId : genresId) {
                    genres.add(genreDbStorage.getGenre(genreId));
                }
            }
            finalFilm.setGenres(genres);

        } catch (DataAccessException e) {
            throw new ObjectNotFoundException(String.format("Фильм %d не найден", id));
        }
        return finalFilm;
    }

    @Override
    public List<Film> findAllFilm() {
        ArrayList<Film> films = new ArrayList<>();

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
                            "              GROUP BY f.film_name" +
                            "              ORDER BY f.film_id ASC");
            while (filmRows.next()) {
                finalFilm = Film.builder()
                        .id(filmRows.getLong("film_id"))
                        .name(filmRows.getString("film_name"))
                        .description(filmRows.getString("description"))
                        .duration(Long.parseLong(filmRows.getString("duration")))
                        .releaseDate(LocalDate.parse(filmRows.getString("releaseDate")))
                        .rate(filmRows.getInt("rate"))
                        .mpa(makeMpa(filmRows.getLong("rating_id"), filmRows.getString("rating_name")))
                        .build();

                List<Long> genresId = jdbcTemplate.queryForList(
                        "SELECT genre_id FROM genre_film WHERE film_id = ?", Long.class, finalFilm.getId());

                List<Genre> genres = new ArrayList<>();
                if (genresId.size() != 0) {
                    for (Long genreId : genresId) {
                        genres.add(genreDbStorage.getGenre(genreId));
                    }
                }
                finalFilm.setGenres(genres);
                films.add(finalFilm);
            }
        } catch (DataAccessException e) {
            throw new ObjectNotFoundException("Фильм не найден");
        }
        return films;
    }

    @Override
    public Film updateFilm(Film film) throws ValidationException {

        List<Integer> filmId = jdbcTemplate.queryForList(
                "SELECT film_id FROM film WHERE film_id = ?", Integer.class, film.getId());

        if (filmId.size() == 0) {
            log.info("Фильм не найден");
            throw new ObjectNotFoundException("Фильм не найден");
        }

        // проводит все логику работы с жанрами
        ArrayList<Genre> genreFinal = getGenreFinalList(film);

        Mpa mpa = mpaDbStorage.getMpaId(film.getMpa().getId());

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
            finalFilm.setMpa(mpa);
            finalFilm.setGenres(genreFinal);
        } catch (DataAccessException e) {
            throw new ObjectNotFoundException("Фильм не найден");
        }
        return finalFilm;
    }

    /*
     * Метод getGenreFinalList проводи всю работу с жанрами для метода updateFilm
     */
    private ArrayList<Genre> getGenreFinalList(Film film) {

        // хранит список старых жанров
        ArrayList<Genre> genresOldList = new ArrayList<>();
        // сюда будем класть список новых фильмов
        ArrayList<Genre> genreFinal = new ArrayList<>();

        // получаем список всех старых genre_id фильма из базы
        List<Long> genreListId = jdbcTemplate.queryForList(
                "SELECT genre_id FROM genre_film WHERE film_id = ?", Long.class, film.getId());
        if (genreListId.size() > 0 && genreListId.get(0) != null) {
            for (Long genreId : genreListId) {
                genresOldList.add(genreDbStorage.getGenre(genreId));
            }
        }

        if (!(film.getGenres() == null)) {
            for (Genre genre : film.getGenres()) {
                Genre newGenre = genreDbStorage.getGenre(genre.getId());
                if (!genreFinal.contains(genre)) {
                    genreFinal.add(genreDbStorage.getGenre(genre.getId()));
                }
                if (!genresOldList.contains(newGenre)) {
                    jdbcTemplate.update("INSERT INTO genre_film (film_id, genre_id) VALUES (?, ?)",
                            film.getId(), genre.getId());
                }
            }
        }

        if (genresOldList.size() > 0) {
            for (Genre oldGenre : genresOldList) {
                if (!genreFinal.contains(oldGenre)) {
                    jdbcTemplate.update(
                            "delete from genre_film where film_id = ? AND genre_id = ?",
                            film.getId(), oldGenre.getId());
                }
            }
        }
        return genreFinal;
    }

    @Override
    public boolean addLikeFilm(Long filmId, Long userId) throws ValidationException {
        Film finalFilm = null;
        try {
            Integer existingRecordCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?",
                    Integer.class,
                    filmId, userId);
            if (existingRecordCount != null && existingRecordCount > 0) {
                log.info("Like фильма {} пользователем {} уже ранее был осуществлен", filmId, userId);
            } else {
                jdbcTemplate.update(
                        "INSERT INTO likes (film_id, user_id) VALUES (?, ?)",
                        filmId, userId);
            }
        } catch (DataAccessException e) {
            throw new ObjectNotFoundException(String.format("Фильм %d не найден", filmId));
        }
        return true;
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
            throw new ObjectNotFoundException(String.format("like фильма %d не найден", filmId));
        }
    }

    @Override
    public List<Film> sortPopularFilm(Integer count) throws ValidationException {

        Film finalFilm = null;
        Integer realCount = count;
        ArrayList<Film> films = new ArrayList<>();

        if (count == null || count == 0) {
            realCount = 10;
        } else if (count < 0) {
            throw new ValidationException("Значение не может быть отрицательным");
        }

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
                        "              GROUP_CONCAT(l.user_id) AS listOfUsersLike, " +
                        "              COUNT(lk.user_id) AS likes " +
                        "              FROM film AS f" +
                        "              LEFT JOIN genre_film AS gf ON f.film_id = gf.film_id" +
                        "              LEFT JOIN genre AS g ON gf.genre_id = g.genre_id" +
                        "              LEFT JOIN rating AS r ON f.rating_id = r.rating_id" +
                        "              LEFT JOIN likes AS l ON f.film_id = l.film_id " +
                        "              LEFT JOIN likes AS lk ON f.film_id = lk.film_id " +
                        "              GROUP BY f.film_id " +
                        "              ORDER BY likes DESC " +
                        "              LIMIT ?", realCount);
        while (filmRows.next()) {
            finalFilm = Film.builder()
                    .id(filmRows.getLong("film_id"))
                    .name(filmRows.getString("film_name"))
                    .description(filmRows.getString("description"))
                    .duration(Long.parseLong(filmRows.getString("duration")))
                    .releaseDate(LocalDate.parse(filmRows.getString("releaseDate")))
                    .rate(filmRows.getInt("rate"))
                    .mpa(makeMpa(filmRows.getLong("rating_id"), filmRows.getString("rating_name")))
                    .build();

            List<Long> genresId = jdbcTemplate.queryForList(
                    "SELECT genre_id FROM genre_film WHERE film_id = ?", Long.class, finalFilm.getId());

            List<Genre> genres = new ArrayList<>();
            if (genresId.size() != 0) {
                for (Long genreId : genresId) {
                    genres.add(genreDbStorage.getGenre(genreId));
                }
            }
            finalFilm.setGenres(genres);
            films.add(finalFilm);
        }
        return films;
    }

    @Override
    public List<Film> getAllPopular() {
        ArrayList<Film> films = new ArrayList<>();

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
                        "              GROUP_CONCAT(l.user_id) AS listOfUsersLike, " +
                        "              COUNT(lk.user_id) AS likes " +
                        "              FROM film AS f" +
                        "              LEFT JOIN genre_film AS gf ON f.film_id = gf.film_id" +
                        "              LEFT JOIN genre AS g ON gf.genre_id = g.genre_id" +
                        "              LEFT JOIN rating AS r ON f.rating_id = r.rating_id" +
                        "              LEFT JOIN likes AS l ON f.film_id = l.film_id " +
                        "              LEFT JOIN likes AS lk ON f.film_id = lk.film_id " +
                        "              GROUP BY f.film_id " +
                        "              ORDER BY likes DESC ");
        while (filmRows.next()) {
            Film finalFilm = Film.builder()
                    .id(filmRows.getLong("film_id"))
                    .name(filmRows.getString("film_name"))
                    .description(filmRows.getString("description"))
                    .duration(Long.parseLong(filmRows.getString("duration")))
                    .releaseDate(LocalDate.parse(filmRows.getString("releaseDate")))
                    .rate(filmRows.getInt("rate"))
                    .mpa(makeMpa(filmRows.getLong("rating_id"), filmRows.getString("rating_name")))
                    .build();

            List<Long> genresId = jdbcTemplate.queryForList(
                    "SELECT genre_id FROM genre_film WHERE film_id = ?", Long.class, finalFilm.getId());

            List<Genre> genres = new ArrayList<>();
            if (genresId.size() != 0) {
                for (Long genreId : genresId) {
                    genres.add(genreDbStorage.getGenre(genreId));
                }
            }
            finalFilm.setGenres(genres);
            films.add(finalFilm);
        }
        return films;
    }
}
