package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class FilmController {
    private List<Film> films = new ArrayList<>();
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @GetMapping(value = "/films")
    public List<Film> findAll() {
        return films;
    }

    @PostMapping(value = "/films")
    public Film create(@RequestBody @Valid Film film) {
        validateFilm(film);
        films.add(film);
        log.debug("Добавлен фильм: {}", film.getName());
        return film;
    }

    @PutMapping("/films")
    public Film update(@RequestBody @Valid Film film) {
        validateFilm(film);
        if (films.stream().anyMatch(savedFilm -> savedFilm.getId() == film.getId())) {
            films = films.stream()
                    .filter(savedFilm -> savedFilm.getId() != film.getId())
                    .collect(Collectors.toList());
            films.add(film);
            log.debug("Обновлен фильм: {}", film.getName());
            return film;
        } else throw new ValidationException(String.format("Film with this id \"%s\" wasn't found", film.getId()));
    }

    private void validateFilm(Film film) {
        if (film.getDescription().length() > 200) {
            log.error("Description {} length is longer than 200 symbols", film.getDescription());
            throw new ValidationException(String.format("Description \"%s\" length is longer than 200 symbols", film.getDescription()));
        }
        if (LocalDate.parse(film.getReleaseDate(), dateTimeFormatter).isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Release date {} is before than 28.12.1895", film.getReleaseDate());
            throw new ValidationException(String.format("Release date \"%s\" is before than 28.12.1895", film.getReleaseDate()));
        }
        if (film.getDuration() <= 0) {
            log.error("Duration {} must be a positive", film.getDuration());
            throw new ValidationException(String.format("Duration \"%s\" must be a positive", film.getDuration()));
        }
    }
}
