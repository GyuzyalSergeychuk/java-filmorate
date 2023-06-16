package ru.filmogram.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.Genre;
import ru.filmogram.storage.film.GenreStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GenreDbStorageImplTest {

    @Autowired
    GenreStorage genreStorage;

    @Test
    void testGetGenreId() throws ValidationException {
        Genre genre = genreStorage.getGenre(3L);

        assertEquals("Мультфильм", genre.getName());
    }

    @Test
    void testFindAllGenres() throws ValidationException {
        List<Genre> allGenre = genreStorage.findAllGenres();

        assertEquals(6, allGenre.size(), "Количество жанров соответствует");
    }
}