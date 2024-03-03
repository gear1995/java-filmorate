package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import java.util.Objects;

/**
 * Film.
 */
@Data
public class Film {
    private static int ID;
    private final int id;
    @NonNull
    @NotBlank
    private String name;
    private String description;
    private String releaseDate;
    private int duration;

    public Film(@NonNull String name, String description, String releaseDate, int duration, Integer id) {
        this.name = name.trim();
        this.description = description.trim();
        this.releaseDate = releaseDate.trim();
        this.duration = duration;
        this.id = Objects.requireNonNullElseGet(id, () -> ID++);
    }
}
