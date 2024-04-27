package ru.yandex.practicum.filmorate.model;

public class FilmData {
    private final Integer id;
    private String name;

    public FilmData(Integer id) {
        this.id = id;
    }

    public FilmData(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
