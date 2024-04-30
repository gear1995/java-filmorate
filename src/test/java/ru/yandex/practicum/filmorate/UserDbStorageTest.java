package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@JdbcTest // указываем, о необходимости подготовить бины для работы с БД
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {
    private final JdbcTemplate jdbcTemplate;

    @Test
    public void testFindUserById() {
        User newUser = new User("user@email.ru", "vanya123", "1990-01-01", "Ivan Petrov", null, null);
        User secondUser = new User("secondUser@email.ru", "second123", "1990-01-01", "Second name", null, null);

        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);
        userStorage.createUser(newUser);
        userStorage.createUser(secondUser);

        User savedUser = userStorage.findUserById(1);
        User savedSecondUser = userStorage.findUserById(2);
        userStorage.addFriend(savedUser.getId(), savedSecondUser.getId());

        assertNotNull(savedUser);
        assertEquals(newUser.getName(), savedUser.getName());
        assertEquals(newUser.getLogin(), savedUser.getLogin());
        assertEquals(newUser.getBirthday(), savedUser.getBirthday());
        assertEquals(newUser.getName(), savedUser.getName());

//        userStorage.addFriend(savedUser.getId(), savedSecondUser.getId());
        assertEquals(savedUser.getFriends(), savedSecondUser.getFriends());
    }
}