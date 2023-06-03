package ru.filmogram.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Util {

    public static Set<String> checkNoGenre(String genreFilm) {
        if (genreFilm == null) {
            return Set.of();
        }
        Set<String> unicalGenres = new HashSet<String>(List.of(genreFilm.split(",")));
        return unicalGenres;
    }
}
