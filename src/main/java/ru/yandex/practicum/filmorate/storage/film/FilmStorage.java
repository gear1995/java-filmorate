package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    List<Film> findAllFilms();

    Film createFilm(Film film);

    Film updateFilm(Film film);

    HashMap<String, Integer> getGenresList();

    HashMap<String, Integer> getFilmGenres(Integer filmId);

    Optional<Film> setLike(Integer filmId, Integer userId);

    Optional<Film> findFilmById(Integer filmId);

    Optional<Film> deleteLike(Integer filmId, Integer userId);

    List<Film> getPopularFilms(Integer count);

    HashMap<String, Integer> getMpaById(Integer id);

    HashMap<String, Integer> getMpa();
}
