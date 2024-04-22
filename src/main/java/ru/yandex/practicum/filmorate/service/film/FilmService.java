package ru.yandex.practicum.filmorate.service.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;
import java.util.Optional;

@Service
public class FilmService {
    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Optional<Film> setLike(Integer filmId, Integer userId) {
        return filmStorage.setLike(filmId, userId);
    }

    public List<Film> findAllFilms() {
        return filmStorage.findAllFilms();
    }

    public Film createFilm(Film film) {
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public Optional<Film> deleteLike(Integer filmId, Integer userId) {
        return filmStorage.deleteLike(filmId, userId);
    }

    private Optional<Film> findFilmById(Integer filmId) {
        return filmStorage.findFilmById(filmId);
    }

    public List<Film> getPopularFilms(Integer count) {
        return filmStorage.getPopularFilms(count);
    }

    public List<String> getGenresList() {
        return filmStorage.getGenresList();
    }

    public List<String> getFilmGenres(Integer filmId) {
        return filmStorage.getFilmGenres(filmId);
    }

    public String getMpaById(Integer id) {
        return filmStorage.getMpaById(id);
    }

    public List<String> getMpa() {
        return filmStorage.getMpa();
    }
}
