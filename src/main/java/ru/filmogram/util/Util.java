package ru.filmogram.util;

import ru.filmogram.model.Genre;

import java.util.List;

public class Util {

    public static List<Genre> checkNoGenre(String genreFilm) {
        if (genreFilm == null) {
            return List.of();
        }
//        List<Genre> unicalGenres = new HashSet<String>(List.of(genreFilm.split(",")));
        return null;
    }
}
