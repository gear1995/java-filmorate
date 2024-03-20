package ru.yandex.practicum.filmorate.exeption;

public class FilmNotFoundException extends RuntimeException {
    public FilmNotFoundException(Integer filmId) {
        super(String.format("Фильм с таким id %s не найден", filmId));
    }
}
