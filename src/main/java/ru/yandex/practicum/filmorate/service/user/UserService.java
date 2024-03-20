package ru.yandex.practicum.filmorate.service.user;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.List;

@Service
public class UserService {
    private final InMemoryUserStorage inMemoryUserStorage;

    public UserService(InMemoryUserStorage inMemoryUserStorage) {
        this.inMemoryUserStorage = inMemoryUserStorage;
    }

    public List<User> findAllUsers() {
        return inMemoryUserStorage.findAllUsers();
    }

    public User createUser(User user) {
        return inMemoryUserStorage.createUser(user);
    }

    public User updateUser(User user) {
        return inMemoryUserStorage.updateUser(user);
    }

    public void addFriend(Integer id, Integer friendId) {
        inMemoryUserStorage.addFriend(id, friendId);
    }

    public void deleteFriend(Integer id, Integer friendId) {
        inMemoryUserStorage.deleteFriend(id, friendId);
    }

    public List<User> getFriends(Integer id) {
        return inMemoryUserStorage.getFriends(id);
    }

    public List<User> getCommonFriends(Integer id, Integer otherId) {
        return inMemoryUserStorage.getCommonFriends(id, otherId);
    }
}
