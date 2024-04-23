package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exeption.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final HashMap<Integer, User> users = new HashMap<>();
    private static int ID = 1;

    @Override
    public List<User> findAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User createUser(User user) {
        user.setId(ID);
        ID++;
        users.put(user.getId(), user);
        log.debug("Добавлен пользователь: {}", user.getName());
        return user;
    }

    @Override
    public User updateUser(User user) {
        validateUserExist(user.getId());
        users.put(user.getId(), user);
        log.debug("Обновлен пользователь: {}", user.getName());
        return user;
    }

    @Override
    public void addFriend(Integer id, Integer friendId) {
        validateUserExist(id);
        validateUserExist(friendId);
        users.get(id).addFriend(friendId);
        users.get(friendId).addFriend(id);
        log.debug("Пользователи {} и {} стали друзьями", id, friendId);
    }

    @Override
    public void validateUserExist(Integer userId) {
        if (!users.containsKey(userId)) {
            log.error("User with this id {} wasn't found", userId);
            throw new UserNotFoundException(userId);
        }
    }

    @Override
    public void deleteFriend(Integer id, Integer friendId) {
        validateUserExist(id);
        validateUserExist(friendId);
        users.get(id).deleteFriend(friendId);
        users.get(friendId).deleteFriend(id);
        log.debug("Пользователи {} и {} более не друзья((", id, friendId);
    }

    @Override
    public List<User> getFriends(Integer id) {
        validateUserExist(id);
        return users.get(id).getFriends().stream()
                .map(users::get)
                .collect(Collectors.toList());
    }

    @Override
    public Integer getCommonFriends(Integer id, Integer otherId) {
        validateUserExist(id);
        validateUserExist(otherId);
        return (int) users.get(id).getFriends().stream()
                .filter(friendId -> users.get(otherId).getFriends().contains(friendId))
                .map(users::get).count();
    }

    @Override
    public User findUserById(Integer id) {
        return null;
    }
}
