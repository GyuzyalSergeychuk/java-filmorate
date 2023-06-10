package ru.filmogram.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.filmogram.exceptions.ObjectNotFoundException;
import ru.filmogram.model.Genre;
import ru.filmogram.storage.film.GenreStorage;

import java.util.ArrayList;
import java.util.List;

@Repository
public class GenreDbStorageImpl implements GenreStorage {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorageImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Genre getGenre(Long id) {
        Genre genre = null;

        SqlRowSet genreRows = jdbcTemplate.queryForRowSet(
                    "SELECT genre_id, " +
                            "genre_name " +
                            "FROM genre " +
                            "WHERE genre_id = ?", id);
            if (genreRows.next()) {
                genre = Genre.builder()
                        .id(genreRows.getLong("genre_id"))
                        .name(genreRows.getString("genre_name"))
                        .build();
                return genre;
            } else {
            throw new ObjectNotFoundException("Жанр не найден");
        }
    }

    @Override
    public List<Genre> findAllGenres() {
        ArrayList<Genre> genres = new ArrayList<>();

        SqlRowSet genreRows = jdbcTemplate.queryForRowSet(
                "SELECT genre_id, " +
                        "genre_name " +
                        "FROM genre ");
        while (genreRows.next()) {
            Genre genre = Genre.builder()
                    .id(genreRows.getLong("genre_id"))
                    .name(genreRows.getString("genre_name"))
                    .build();
            genres.add(genre);
        }
        return genres;
    }
}
