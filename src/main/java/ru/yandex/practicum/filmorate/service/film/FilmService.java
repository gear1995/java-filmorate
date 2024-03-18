package ru.yandex.practicum.filmorate.service.film;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeption.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exeption.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final InMemoryFilmStorage inMemoryFilmStorage;
    private final InMemoryUserStorage inMemoryUserStorage;

    public Film setLike(Integer filmId, Integer userId) {
        Film returnedFilm = findFilmById(filmId);

        isUserExist(userId);

        returnedFilm.setLike(userId);
        return returnedFilm;
    }

    public FilmService(InMemoryFilmStorage inMemoryFilmStorage, InMemoryUserStorage inMemoryUserStorage) {
        this.inMemoryFilmStorage = inMemoryFilmStorage;
        this.inMemoryUserStorage = inMemoryUserStorage;
    }

    public ArrayList<Film> findAllFilms() {
        return inMemoryFilmStorage.findAllFilms();
    }

    public Film createFilm(Film film) {
        return inMemoryFilmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        return inMemoryFilmStorage.updateFilm(film);
    }

    public Film deleteLike(Integer filmId, Integer userId) {
        Film returnedFilm = findFilmById(filmId);

        isUserExist(userId);

        returnedFilm.deleteLike(userId);
        return returnedFilm;
    }

    private void isUserExist(Integer userId) {
        inMemoryUserStorage.findAllUsers().stream()
                .filter(user -> user.getId() == userId)
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Film findFilmById(Integer filmId) {
        return inMemoryFilmStorage.findAllFilms().stream()
                .filter(film -> film.getId().equals(filmId))
                .findFirst()
                .orElseThrow(() -> new FilmNotFoundException(filmId));
    }

    public List<Film> getPopularFilms(Integer count) {
        int currentFilmCount = 3;
        if (count != null) {
            currentFilmCount = count;
        }
        return inMemoryFilmStorage.findAllFilms().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .limit(currentFilmCount)
                .collect(Collectors.toList());
    }
}
