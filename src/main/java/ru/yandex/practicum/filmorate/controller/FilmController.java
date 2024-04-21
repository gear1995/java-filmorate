package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
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
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film update(@RequestBody @Valid Film film) {
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
    public List<String> getGenres() {
        return filmService.getGenresList();
    }

    @GetMapping(value = "genres/{id}")
    public List<String> getFilmGenres(@PathVariable Integer id) {
        return filmService.getFilmGenres(id);
    }
}
