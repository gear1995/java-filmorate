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
        // Подготавливаем данные для теста
        User newUser = new User("user@email.ru", "vanya123", "1990-01-01", "Ivan Petrov", 1, null);
        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);
        userStorage.createUser(newUser);

//         вызываем тестируемый метод
        User savedUser = userStorage.findUserById(1);

        assertNotNull(savedUser);
        assertEquals(newUser, savedUser);
    }
}