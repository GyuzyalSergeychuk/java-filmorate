package ru.filmogram.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.Mpa;
import ru.filmogram.storage.film.FilmStorage;
import ru.filmogram.storage.film.MpaStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MpaDbStorageImplTest {

    @Autowired
    FilmStorage filmStorage;
    @Autowired
    MpaStorage mpaStorage;

    @Test
    void getMpaId() throws ValidationException {

        Mpa mpa = mpaStorage.getMpaId(1L);

        assertEquals("G", mpa.getName());
    }

    @Test
    void findAllMpa() throws ValidationException {

        List<Mpa> allMpa = mpaStorage.findAllMpa();

        assertEquals(5, allMpa.size());
    }
}