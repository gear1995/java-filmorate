package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;

/**
 * Film.
 */
@Data
public class Film {
    private int id = 1;
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
        if (id != null) {
            this.id = id;
        }
    }
}
