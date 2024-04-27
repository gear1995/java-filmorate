package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmData;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "films")
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<Film> findAll() {
        return filmService.findAllFilms();
    }

    @PostMapping
    public Film create(@RequestBody @Valid Film film) {
        film.validateFilmData(film.getReleaseDate());
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film update(@RequestBody @Valid Film film) {
        film.validateFilmData(film.getReleaseDate());
        return filmService.updateFilm(film);
    }

    @PutMapping(value = "{filmId}/like/{userId}")
    public Optional<Film> setLike(@PathVariable Integer filmId, @PathVariable Integer userId) {
        return filmService.setLike(filmId, userId);
    }

    @DeleteMapping(value = "{filmId}/like/{userId}")
    public Optional<Film> deleteLike(@PathVariable Integer filmId, @PathVariable Integer userId) {
        return filmService.deleteLike(filmId, userId);
    }

    @GetMapping(value = "popular")
    public List<Film> getPopularFilms(@RequestParam(required = false) @Positive Integer count) {
        return filmService.getPopularFilms(count);
    }

    @GetMapping(value = "genres")
    public ArrayList<FilmData> getGenres() {
        return filmService.getGenresList();
    }

    @GetMapping(value = "genres/{id}")
    public ArrayList<FilmData> getGenresById(@PathVariable Integer id) {
        return filmService.getGenresById(id);
    }

    @GetMapping(value = "{id}")
    public Optional<Film> getFilmById(@PathVariable Integer id) {
        return filmService.getFilmById(id);
    }

    @GetMapping(value = "mpa/{id}")
    public FilmData getMpaById(@PathVariable Integer id) {
        return filmService.getMpaById(id);
    }

    @GetMapping(value = "mpa")
    public ArrayList<FilmData> getMpa() {
        return filmService.getMpa();
    }
}
