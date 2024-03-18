package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;

public interface FilmStorage {
    ArrayList<Film> findAllFilms();

    Film createFilm(Film film);

    Film updateFilm(Film film);
}
