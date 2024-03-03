package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class User {
    private int id;
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
        if (id != null) {
            this.id = id;
        }
    }
}
