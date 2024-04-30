package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.FilmData;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import java.util.ArrayList;

@RestController
@RequestMapping("mpa")
public class MpaController {
    private final FilmService filmService;

    public MpaController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public ArrayList<FilmData> getMpa() {
        return filmService.getMpa();
    }

    @GetMapping("{id}")
    public FilmData getMpaById(@PathVariable Integer id) {
        return filmService.getMpaById(id);
    }
}
