package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final HashMap<Integer, User> users = new HashMap<>();
    private static int ID = 1;

    @GetMapping
    public ArrayList<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User create(@RequestBody @Valid User user) {
        user.setId(ID);
        ID++;
        users.put(user.getId(), user);
        log.debug("Добавлен пользователь: {}", user.getName());
        return user;
    }

    @PutMapping
    public User update(@RequestBody @Valid User user) {
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            log.debug("Обновлен пользователь: {}", user.getName());
            return user;
        } else {
            log.error("User with this id {} wasn't found", user.getId());
            throw new ValidationException(String.format("User with this id \"%s\" wasn't found", user.getId()));
        }
    }
}
