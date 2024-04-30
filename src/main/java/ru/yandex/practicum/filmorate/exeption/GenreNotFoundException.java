package ru.yandex.practicum.filmorate.exeption;

public class GenreNotFoundException extends RuntimeException {
    public GenreNotFoundException(Integer genreId) {
        super(String.format("Жанр с таким id %s не найден", genreId));
    }
}
