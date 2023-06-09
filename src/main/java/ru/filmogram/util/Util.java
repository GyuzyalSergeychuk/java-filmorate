package ru.filmogram.util;

import ru.filmogram.model.Genre;
import ru.filmogram.model.Mpa;

import java.util.List;

public class Util {

    public static List<Genre> checkNoGenre(String genreFilm) {
        if (genreFilm == null) {
            return List.of();
        }
        return null;
    }

    public static Mpa makeMpa(Long rating_id, String rating_name) {
        return Mpa.builder()
                .id(rating_id)
                .name(rating_name)
                .build();
    }
}
