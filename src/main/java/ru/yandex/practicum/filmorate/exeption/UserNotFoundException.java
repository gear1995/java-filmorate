package ru.yandex.practicum.filmorate.exeption;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Integer userId) {
        super(String.format("Пользователь с таким id %s не найден", userId));
    }
}
