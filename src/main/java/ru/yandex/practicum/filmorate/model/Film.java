package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exeption.ValidationException;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Film.
 */
@Data
@Slf4j
public class Film {
    private int id = 1;
    @NotBlank
    private String name;
    private String description;
    @NonNull
    private String releaseDate;
    private int duration;
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Film(@NotBlank String name, String description, @NonNull String releaseDate, int duration, Integer id) {
        validateFilmData(description, releaseDate, duration);
        this.name = name.trim();
        this.description = description.trim();
        this.releaseDate = releaseDate.trim();
        this.duration = duration;
        if (id != null) {
            this.id = id;
        }
    }

    private void validateFilmData(String description, String releaseDate, int duration) {
        if (description.length() > 200) {
            log.error("Description {} length is longer than 200 symbols", description);
            throw new ValidationException(String.format("Description \"%s\" length is longer than 200 symbols", description));
        }
        if (LocalDate.parse(releaseDate, dateTimeFormatter).isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Release date {} is before than 28.12.1895", releaseDate);
            throw new ValidationException(String.format("Release date \"%s\" is before than 28.12.1895", releaseDate));
        }
        if (duration <= 0) {
            log.error("Duration {} must be a positive", duration);
            throw new ValidationException(String.format("Duration \"%s\" must be a positive", duration));
        }
    }

}
