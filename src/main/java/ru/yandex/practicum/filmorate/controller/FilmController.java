package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("films")
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

    @PutMapping("{filmId}/like/{userId}")
    public Optional<Film> setLike(@PathVariable Integer filmId, @PathVariable Integer userId) {
        return filmService.setLike(filmId, userId);
    }

    @DeleteMapping("{filmId}/like/{userId}")
    public Optional<Film> deleteLike(@PathVariable Integer filmId, @PathVariable Integer userId) {
        return filmService.deleteLike(filmId, userId);
    }

    @GetMapping("popular")
    public List<Film> getPopularFilms(@RequestParam(required = false) @Positive Integer count) {
        return filmService.getPopularFilms(count);
    }
    @GetMapping("{id}")
    public Optional<Film> getFilmById(@PathVariable Integer id) {
        return filmService.getFilmById(id);
    }
}
