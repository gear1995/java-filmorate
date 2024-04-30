package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exeption.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User findUserById(Integer id) {
        validateUserExist(id);
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE user_id = ?", id);

        SqlRowSet friendsRows = jdbcTemplate.queryForRowSet("SELECT second_user_id" +
                " FROM friends WHERE first_user_id = ?", id);

        Set<Integer> friendsSet = new HashSet<>();
        while (friendsRows.next()) {
            friendsSet.add(friendsRows.getInt("second_user_id"));
        }
        User user = null;
        if (userRows.next()) {
            user = buildUser(userRows, friendsSet);
            log.info("Найден пользователь: {} {}", user.getId(), user.getName());
        } else {
            log.info("Пользователь с идентификатором {} не найден.", id);
        }
        return user;
    }

    @Override
    public List<User> findAllUsers() {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from users");
        List<User> usersList = new ArrayList<>();

        while (userRows.next()) {
            SqlRowSet friendsRows = jdbcTemplate.queryForRowSet("SELECT second_user_id" +
                    " FROM friends WHERE first_user_id = ?", userRows.getString("user_id"));

            Set<Integer> friendsSet = new HashSet<>();
            while (friendsRows.next()) {
                friendsSet.add(friendsRows.getInt("second_user_id"));
            }

            usersList.add(buildUser(userRows, friendsSet));
        }
        return usersList;
    }

    @Override
    public User createUser(User user) {
        Set<Integer> userFriends = user.getFriends();

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", user.getEmail());
        parameters.put("name", user.getName());
        parameters.put("birthday", user.getBirthday());
        parameters.put("login", user.getLogin());

        Number userId = simpleJdbcInsert.executeAndReturnKey(parameters);

        if (userFriends.size() > 0) {
            userFriends.forEach(friendId ->
                    jdbcTemplate.update("MERGE INTO friends (first_user_id, second_user_id, friendship_status)" +
                            " VALUES (?, ?, ?)", userId, friendId, "CONFIRMED")
            );
        }

        log.debug("Добавлен фильм: {}", user.getName());

        return findUserById(userId.intValue());
    }

    @Override
    public User updateUser(User user) {
        validateUserExist(user.getId());

        Set<Integer> userFriends = user.getFriends();

        if (userFriends.size() > 0) {
            userFriends.forEach(friendId ->
                    jdbcTemplate.update("MERGE INTO friends (first_user_id, second_user_id, FRIENDSHIP_STATUS)" +
                            " VALUES (?, ?, ?)", user.getId(), friendId, "CONFIRMED")
            );
        }

        String sqlQuery = "UPDATE users SET email = ?, name = ?, birthday = ?, login = ? WHERE user_id = ?";

        jdbcTemplate.update(sqlQuery,
                user.getEmail(),
                user.getName(),
                user.getBirthday(),
                user.getLogin(),
                user.getId()
        );

        log.debug("Обновлен пользователь: {}", user.getName());

        return findUserById(user.getId());
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) {
        validateUserExist(userId);
        validateUserExist(friendId);

        jdbcTemplate.update("MERGE INTO friends (first_user_id, second_user_id, FRIENDSHIP_STATUS)" +
                " VALUES (?, ?, ?)", userId, friendId, "CONFIRMED");
    }

    @Override
    public void validateUserExist(Integer userId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT user_id FROM users WHERE user_id = ?", userId);
        if (!userRows.next()) {
            log.error("User with this id {} wasn't found", userId);
            throw new UserNotFoundException(userId);
        }
    }

    @Override
    public void deleteFriend(Integer userId, Integer friendId) {
        validateUserExist(userId);
        validateUserExist(friendId);

        jdbcTemplate.update("DELETE FROM friends WHERE first_user_id = ? AND  second_user_id = ?", userId, friendId);
    }

    @Override
    public List<User> getFriends(Integer userId) {
        validateUserExist(userId);
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT second_user_id FROM friends WHERE first_user_id = ?",
                userId);
        List<User> userList = new ArrayList<>();
        while (userRows.next()) {
            userList.add(findUserById(userRows.getInt("second_user_id")));
        }
        return userList;
    }

    @Override
    public List<User> getCommonFriends(Integer userId, Integer otherUserId) {
        List<User> userFriends = getFriends(userId);
        List<User> otherUserFriends = getFriends(otherUserId);
        return userFriends.stream().filter(otherUserFriends::contains).collect(Collectors.toList());
    }

    private User buildUser(SqlRowSet userRows, Set<Integer> friendsSet) {
        return new User(
                Objects.requireNonNull(userRows.getString("email") != null
                        ? userRows.getString("email")
                        : null),
                Objects.requireNonNull(userRows.getString("login") != null
                        ? userRows.getString("login")
                        : null),
                Objects.requireNonNull(userRows.getString("birthday") != null
                        ? userRows.getString("birthday")
                        : null),
                userRows.getString("name"),
                userRows.getInt("user_id"),
                friendsSet
        );
    }
}
