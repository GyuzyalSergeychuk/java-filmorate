package ru.filmogram.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.Film;
import ru.filmogram.services.FilmService;
import ru.filmogram.storage.film.FilmStorage;

import java.util.List;

@RestController
@RequestMapping("/films")
public class FilmController {

    private FilmStorage filmStorage;
    private FilmService filmService;

    @Autowired
    public FilmController(FilmStorage filmStorage, FilmService filmService) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
    }

    public FilmController(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    @GetMapping()
    public List<Film> findAll() {
        return filmStorage.findAllFilm();
    }

    @PostMapping()
    public Film create(@RequestBody Film film) throws ValidationException {
        return filmStorage.createFilm(film);
    }

    @PutMapping()
    public Film update(@RequestBody Film film) throws ValidationException {
        return filmStorage.updateFilm(film);
    }

    @PutMapping("{id}/like/{userId}")
    public Film updateLikeFilm(@PathVariable("id") Long id, @PathVariable("userId") Long userId){
        return filmService.addLike(id, userId);
    }

    @DeleteMapping("{id}/like/{userId}")
    public void deleteLikeFilm(@PathVariable("id") Long id, @PathVariable("userId") Long userId){
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> allPopular() throws ValidationException {
        return filmService.allPopularFilms();
    }

    @GetMapping(value = "/popular", params = "count")
    public List<Film> sortPopularCountFilm(@RequestParam Integer count) throws ValidationException {
        return filmService.sortFilmCount(count);
    }

    @GetMapping("{id}")
    public Film getFilm(@PathVariable("id") Long id) {
        return filmService.getIdFilm(id);
    }
}
