package ru.filmogram.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.Mpa;
import ru.filmogram.storage.film.MpaStorage;

import java.util.List;

@Service
public class MpaService {

    @Autowired
    private MpaStorage mpaStorage;

    public List<Mpa> findAll() {
        return mpaStorage.findAllMpa();
    }

    public Mpa getId(Long id) throws ValidationException {
        if (id <= 0) {
            throw new ValidationException(String.format("id %d не может быть отрицательным", id));
        }
        return mpaStorage.getMpaId(id);
    }
}
