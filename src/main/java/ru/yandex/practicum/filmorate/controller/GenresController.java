package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.FilmData;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import java.util.ArrayList;

@RestController
@RequestMapping("genres")
public class GenresController {
    private final FilmService filmService;

    public GenresController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping("{id}")
    public ArrayList<FilmData> getGenresById(@PathVariable Integer id) {
        return filmService.getGenresById(id);
    }

    @GetMapping
    public ArrayList<FilmData> getGenres() {
        return filmService.getGenresList();
    }
}
