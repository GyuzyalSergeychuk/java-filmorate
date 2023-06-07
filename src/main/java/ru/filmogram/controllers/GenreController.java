package ru.filmogram.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.Genre;
import ru.filmogram.services.GenreService;

import java.util.List;

@RestController
@RequestMapping("/genres")
public class GenreController {

    private GenreService genreService;

    @Autowired
    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping()
    public List<Genre> findAll() {
        return genreService.findAll();
    }

    @GetMapping("{id}")
    public Genre getGenreId(@PathVariable("id") Long id) throws ValidationException {
        return genreService.getGenreId(id);
    }
}
