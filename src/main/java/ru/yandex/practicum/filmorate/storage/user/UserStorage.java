package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    List<User> findAllUsers();

    User createUser(User user);

    User updateUser(User user);

    void addFriend(Integer id, Integer friendId);

    void validateUserExist(Integer userId);

    void deleteFriend(Integer id, Integer friendId);

    List<User> getFriends(Integer id);

    Integer getCommonFriends(Integer id, Integer otherId);

    User findUserById(Integer id);
}
