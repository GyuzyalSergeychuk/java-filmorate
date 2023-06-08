package ru.filmogram.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.filmogram.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Set;

public class FilmMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {

        return Film.builder()
                .id(rs.getLong("film_id"))
                .name(rs.getString("film_name"))
                .description(rs.getString("description"))
                .duration(rs.getLong("duration"))
                .releaseDate(LocalDate.parse(rs.getString("releaseDate")))
                .rate(rs.getInt("rate"))
                .build();
    }

    private Set<String> checkNoLike(String likes) {
        if (likes == null) {
            return Set.of();
        }
        return Set.of(likes.split(","));
    }
}
