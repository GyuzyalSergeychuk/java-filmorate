package ru.filmogram.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.filmogram.model.Mpa;
import ru.filmogram.services.MpaService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
public class MpaController {

    private MpaService mpaService;

    @Autowired
    public MpaController(MpaService mpaService) {
        this.mpaService = mpaService;
    }

    @GetMapping()
    public List<Mpa> findAll() {
        return mpaService.findAll();
    }

    @GetMapping("{id}")
    public Mpa getMpaId(@PathVariable("id") Long id) {
        return mpaService.getId(id);
    }
}
