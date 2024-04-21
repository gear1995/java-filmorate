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
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from USERS where USER_ID = ?", id);

        SqlRowSet friendsRows = jdbcTemplate.queryForRowSet("SELECT SECOND_USER_ID" +
                " FROM FRIENDS WHERE FIRST_USER_ID = ?", id);

        Set<Integer> friendsSet = new HashSet<>();
        if (friendsRows.next()) {
            friendsSet.add(friendsRows.getInt("SECOND_USER_ID"));
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

    private User buildUser(SqlRowSet userRows, Set<Integer> friendsSet) {
        return new User(
                Objects.requireNonNull(userRows.getString("EMAIL") != null
                        ? userRows.getString("EMAIL")
                        : null),
                Objects.requireNonNull(userRows.getString("LOGIN") != null
                        ? userRows.getString("LOGIN")
                        : null),
                Objects.requireNonNull(userRows.getString("BIRTHDAY") != null
                        ? userRows.getString("BIRTHDAY")
                        : null),
                userRows.getString("NAME"),
                userRows.getInt("USER_ID"),
                friendsSet
        );
    }

    @Override
    public List<User> findAllUsers() {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from USERS");
        List<User> usersList = new ArrayList<>();

        if (userRows.next()) {
            SqlRowSet friendsRows = jdbcTemplate.queryForRowSet("SELECT SECOND_USER_ID" +
                    " FROM FRIENDS WHERE FIRST_USER_ID = ?", userRows.getString("USER_ID"));

            Set<Integer> friendsSet = new HashSet<>();
            if (friendsRows.next()) {
                friendsSet.add(friendsRows.getInt("SECOND_USER_ID"));
            }

            usersList.add(buildUser(userRows, friendsSet));
        }
        return usersList;
    }

    @Override
    public User createUser(User user) {
        Set<Integer> userFriends = user.getFriends();

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("USERS")
                .usingGeneratedKeyColumns("USER_ID");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("EMAIL", user.getEmail());
        parameters.put("NAME", user.getName());
        parameters.put("BIRTHDAY", user.getBirthday());
        parameters.put("LOGIN", user.getLogin());

        Number userId = simpleJdbcInsert.executeAndReturnKey(parameters);

        if (userFriends.size() > 0) {
            userFriends.forEach(friendId ->
                    jdbcTemplate.update("INSERT INTO FRIENDS (FIRST_USER_ID, SECOND_USER_ID, FRIENDSHIP_STATUS)" +
                            " VALUES (?, ?, ?)", userId, friendId, "CONFIRMED")
            );
        }

        log.debug("Добавлен фильм: {}", user.getName());
        return user;
    }

    @Override
    public User updateUser(User user) {
        validateUserExist(user.getId());

        Set<Integer> userFriends = user.getFriends();

        if (userFriends.size() > 0) {
            userFriends.forEach(friendId ->
                    jdbcTemplate.update("INSERT INTO FRIENDS (FIRST_USER_ID, SECOND_USER_ID, FRIENDSHIP_STATUS)" +
                            " VALUES (?, ?, ?)", user.getId(), friendId, "CONFIRMED")
            );
        }

        String sqlQuery = "UPDATE USERS SET EMAIL = ?, NAME = ?, BIRTHDAY = ?, LOGIN = ? WHERE USER_ID = ?";

        jdbcTemplate.update(sqlQuery,
                user.getEmail(),
                user.getName(),
                user.getBirthday(),
                user.getLogin(),
                user.getId()
        );

        log.debug("Обновлен пользователь: {}", user.getName());
        return user;
    }


    @Override
    public void addFriend(Integer userId, Integer friendId) {
        validateUserExist(userId);
        validateUserExist(friendId);

        jdbcTemplate.update("INSERT INTO FRIENDS (FIRST_USER_ID, SECOND_USER_ID, FRIENDSHIP_STATUS)" +
                " VALUES (?, ?, ?)", userId, friendId, "CONFIRMED");
    }

    @Override
    public void validateUserExist(Integer userId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select USER_ID from USERS where USER_ID = ?", userId);
        if (!userRows.next()) {
            log.error("User with this id {} wasn't found", userId);
            throw new UserNotFoundException(userId);
        }
    }

    @Override
    public void deleteFriend(Integer userId, Integer friendId) {
        validateUserExist(userId);
        validateUserExist(friendId);

        jdbcTemplate.update("DELETE FROM FRIENDS WHERE FIRST_USER_ID = ?", userId);
        jdbcTemplate.update("DELETE FROM FRIENDS WHERE FIRST_USER_ID = ?", friendId);
    }

    @Override
    public List<User> getFriends(Integer userId) {
        validateUserExist(userId);
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT SECOND_USER_ID FROM FRIENDS WHERE FIRST_USER_ID = ?",
                userId);
        List<User> userList = new ArrayList<>();
        if (userRows.next()) {
            userList.add(findUserById(userRows.getInt("SECOND_USER_ID")));
        }
        return userList;
    }

    @Override
    public List<User> getCommonFriends(Integer userId, Integer otherUserId) {
        List<User> userFriends = getFriends(userId);
        List<User> otherUserFriends = getFriends(otherUserId);
        return userFriends.stream().filter(otherUserFriends::contains).collect(Collectors.toList());
    }
}
