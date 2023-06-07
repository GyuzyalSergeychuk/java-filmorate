package ru.filmogram.services;

import org.springframework.stereotype.Service;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.Genre;
import ru.filmogram.storage.film.GenreStorage;

import java.util.List;

@Service
public class GenreService {

    private GenreStorage genreStorage;

    public List<Genre> findAll() {
        return genreStorage.findAllGenres();
    }

    public Genre getGenreId(Long id) throws ValidationException {
        if (id < 0 && id == null){
            throw new ValidationException(String.format("id жанра {} не может быть отрицательным", id));
        }
        return genreStorage.getGenreId(id);
    }
}
