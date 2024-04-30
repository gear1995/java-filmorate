package ru.yandex.practicum.filmorate.exeption;

public class MpaNotFoundException extends RuntimeException {
    public MpaNotFoundException(Integer mpaId) {
        super(String.format("Рейтинг с таким id %s не найден", mpaId));
    }
}