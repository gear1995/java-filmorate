package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exeption.ValidationException;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

@Data
@Slf4j
public class User {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private int id;
    @Email
    private String email;
    @NotBlank
    private String login;
    private String name;
    @NotBlank
    private String birthday;
    private Set<Integer> friends = new HashSet<>();

    public User(@Email String email,
                @NotBlank String login,
                @NotBlank String birthday,
                String name,
                Integer id,
                Set<Integer> friends) {
        this.email = email.trim();
        this.login = login.trim();
        this.birthday = birthday.trim();
        if (name == null || name.isBlank()) {
            this.name = login;
        } else {
            this.name = name.trim();
        }
        if (id != null) {
            this.id = id;
        }
        if (friends != null) {
            this.friends = friends;
        }
    }

    public void validateUser(String login, String birthday) {
        if (login.contains(" ")) {
            log.error("Login {} contains space", login);
            throw new ValidationException(String.format("Login \"%s\" contains space", login));
        }
        if (LocalDate.parse(birthday, dateTimeFormatter).isAfter(LocalDate.now())) {
            log.error("Birthday date {} is after than now", birthday);
            throw new ValidationException(String.format("Birthday date \"%s\" is after than now: %s", birthday, LocalDateTime.now()));
        }
    }

    public void addFriend(Integer friendId) {
        friends.add(friendId);
    }

    public void deleteFriend(Integer friendId) {
        friends.remove(friendId);
    }
}
