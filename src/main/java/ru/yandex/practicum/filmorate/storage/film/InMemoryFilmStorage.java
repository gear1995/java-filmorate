package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exeption.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final HashMap<Integer, Film> films = new HashMap<>();
    private static int ID = 1;

    @Override
    public List<Film> findAllFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film createFilm(Film film) {
        film.setId(ID);
        ID++;
        films.put(film.getId(), film);
        log.debug("Добавлен фильм: {}", film.getName());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.debug("Обновлен фильм: {}", film.getName());
            return film;
        } else {
            log.error("Film with this id {} wasn't found", film.getId());
            throw new FilmNotFoundException(film.getId());
        }
    }

    @Override
    public List<String> getGenresList() {
        return null;
    }

    @Override
    public List<String> getFilmGenres(Integer filmId) {
        return null;
    }

    @Override
    public Optional<Film> setLike(Integer filmId, Integer userId) {
        return Optional.empty();
    }

    @Override
    public Optional<Film> findFilmById(Integer filmId) {
        return Optional.empty();
    }

    @Override
    public Optional<Film> deleteLike(Integer filmId, Integer userId) {
        return Optional.empty();
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {
        return null;
    }
}
