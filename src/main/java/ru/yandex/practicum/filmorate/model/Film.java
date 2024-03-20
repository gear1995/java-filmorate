package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exeption.ValidationException;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

/**
 * Film.
 */
@Data
@Slf4j
public class Film {
    private Integer id = 1;
    @NotBlank
    private String name;
    @Size(max = 200)
    private String description;
    @NotBlank
    private String releaseDate;
    @Positive
    private int duration;
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private Set<Integer> likes = new HashSet<>();

    public Film(@NotBlank String name, @Size(max = 200) String description, @NotBlank String releaseDate, @Positive int duration, Integer id) {
        validateFilmData(releaseDate);
        this.name = name.trim();
        this.description = description.trim();
        this.releaseDate = releaseDate.trim();
        this.duration = duration;
        if (id != null) {
            this.id = id;
        }
    }

    private void validateFilmData(String releaseDate) {
        if (LocalDate.parse(releaseDate, dateTimeFormatter).isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Release date {} is before than 28.12.1895", releaseDate);
            throw new ValidationException(String.format("Release date \"%s\" is before than 28.12.1895", releaseDate));
        }
    }

    public void setLike(Integer userId) {
        likes.add(userId);
    }

    public void deleteLike(Integer userId) {
        likes.remove(userId);
    }
}
