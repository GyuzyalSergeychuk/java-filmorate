package ru.filmogram.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.filmogram.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.filmogram.util.Util.checkNoGenre;

public class FilmMapperLikes implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {

        return Film.builder()
                .id(rs.getLong("film_id"))
                .name(rs.getString("film_name"))
                .description(rs.getString("description"))
                .duration(rs.getLong("duration"))
                .releaseDate(LocalDate.parse(rs.getString("releaseDate")))
                .rating(rs.getString("name"))
                .genre(checkNoGenre(rs.getString("genreFilm")))
                .likes(Stream.of(rs.getString("listOfUsersLike").split(","))
                        .map(Long::parseLong)
                        .collect(Collectors.toSet()))
                .build();
    }
}

