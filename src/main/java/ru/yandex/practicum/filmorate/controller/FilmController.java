package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping(value = "/films")
public class FilmController {
    private final HashMap<Integer, Film> films = new HashMap<>();
    private static int ID = 1;

    @GetMapping
    public ArrayList<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film create(@RequestBody @Valid Film film) {
        film.setId(ID);
        ID++;
        films.put(film.getId(), film);
        log.debug("Добавлен фильм: {}", film.getName());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody @Valid Film film) {
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.debug("Обновлен фильм: {}", film.getName());
            return film;
        } else {
            log.error("Film with this id {} wasn't found", film.getId());
            throw new ValidationException(String.format("Film with this id \"%s\" wasn't found", film.getId()));
        }
    }
}
