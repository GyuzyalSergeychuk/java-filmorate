package ru.filmogram.services;

import org.springframework.stereotype.Service;
import ru.filmogram.model.Mpa;
import ru.filmogram.storage.film.MpaStorage;

import java.util.List;

@Service
public class MpaService {

    private MpaStorage mpaStorage;

    public List<Mpa> findAll() {
        return mpaStorage.findAllMpa();
    }

    public Mpa getId(Long id) {
        return mpaStorage.getMpaId(id);
    }
}
