package ru.filmogram.util;

import ru.filmogram.model.Mpa;

public class Util {

    public static Mpa makeMpa(Long ratingId, String ratingName) {
        return Mpa.builder()
                .id(ratingId)
                .name(ratingName)
                .build();
    }
}
