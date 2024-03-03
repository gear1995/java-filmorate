package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Data
public class User {
    private static int ID = 1;
    private final int id;
    @Email
    @NonNull
    @NotBlank
    private String email;
    @NonNull
    @NotBlank
    private String login;
    private String name;
    @NonNull
    private String birthday;

    public User(@NonNull String email, @NonNull String login, @NonNull String birthday, String name, Integer id) {
        this.email = email.trim();
        this.login = login.trim();
        this.birthday = birthday.trim();
        if (name == null || name.isBlank()) {
            this.name = login;
        } else {
            this.name = name.trim();
        }
        this.id = Objects.requireNonNullElseGet(id, () -> ID++);
    }
}
