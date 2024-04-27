package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.FilmData;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    List<Film> findAllFilms();

    Film createFilm(Film film);

    Film updateFilm(Film film);

    ArrayList<FilmData> getAllGenres();

    ArrayList<FilmData> getGenresById(Integer genreId);

    Optional<Film> setLike(Integer filmId, Integer userId);

    Optional<Film> getFilmById(Integer filmId);

    Optional<Film> deleteLike(Integer filmId, Integer userId);

    List<Film> getPopularFilms(Integer count);

    FilmData getMpaById(Integer id);

    ArrayList<FilmData> getMpa();
}
