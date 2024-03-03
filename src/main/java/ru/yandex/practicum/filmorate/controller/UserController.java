package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class UserController {
    private List<User> users = new ArrayList<>();
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @GetMapping("/users")
    public List<User> findAll() {
        return users;
    }

    @PostMapping(value = "/users")
    public User create(@RequestBody @Valid User user) {
        validateUser(user);
        users.add(user);
        log.debug("Добавлен пользователь: {}", user.getName());
        return user;
    }

    @PutMapping(value = "/users")
    public User update(@RequestBody @Valid User user) {
        validateUser(user);
        users = users.stream()
                .filter(savedUser -> savedUser.getId() != user.getId())
                .collect(Collectors.toList());
        users.add(user);
        log.debug("Обновлен пользователь: {}", user.getName());
        return user;
    }

    private void validateUser(User user) {
        if (user.getLogin().contains(" ")) {
            log.error("Login {} contains space", user.getLogin());
            throw new ValidationException(String.format("Login \"%s\" contains space", user.getLogin()));
        }
        if (LocalDate.parse(user.getBirthday(), dateTimeFormatter).isAfter(LocalDate.now())) {
            log.error("Birthday date {} is after than now", user.getBirthday());
            throw new ValidationException(String.format("Birthday date \"%s\" is after than now: %s", user.getLogin(), LocalDateTime.now()));
        }
    }
}
