package ru.filmogram.storage.film;

import ru.filmogram.model.Mpa;

import java.util.List;

public interface MpaStorage {

    List<Mpa> findAllMpa();

    Mpa getMpaId(Long id);
}
