package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    List<Film> findAllFilms();

    Film createFilm(Film film);

    Film updateFilm(Film film);

    List<String> getGenresList();

    List<String> getFilmGenres(Integer filmId);

    Optional<Film> setLike(Integer filmId, Integer userId);

    Optional<Film> findFilmById(Integer filmId);

    Optional<Film> deleteLike(Integer filmId, Integer userId);

    List<Film> getPopularFilms(Integer count);

    String getMpaById(Integer id);

    List<String> getMpa();
}
