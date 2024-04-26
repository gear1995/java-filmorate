package ru.yandex.practicum.filmorate.service.user;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
public class UserService {
    private final UserStorage userDbStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userDbStorage = userStorage;
    }

    public List<User> findAllUsers() {
        return userDbStorage.findAllUsers();
    }

    public User createUser(User user) {
        return userDbStorage.createUser(user);
    }

    public User updateUser(User user) {
        return userDbStorage.updateUser(user);
    }

    public void addFriend(Integer id, Integer friendId) {
        userDbStorage.addFriend(id, friendId);
    }

    public void deleteFriend(Integer id, Integer friendId) {
        userDbStorage.deleteFriend(id, friendId);
    }

    public List<User> getFriends(Integer id) {
        return userDbStorage.getFriends(id);
    }

    public List<User> getCommonFriends(Integer id, Integer otherId) {
        return userDbStorage.getCommonFriends(id, otherId);
    }

    public User findUserById(Integer id) {
        return userDbStorage.findUserById(id);
    }
}
