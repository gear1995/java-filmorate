package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exeption.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("filmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> findAllFilms() {
        SqlRowSet filmsRows = jdbcTemplate.queryForRowSet(
                "select F.FILM_ID, FILM_NAME, DESCRIPTION, RELEASE_DATE, DURATION, F.MPA_RATING_ID, GENRE_NAME" +
                        " from FILMS AS F" +
                        " left join MPA_RATING MR ON F.MPA_RATING_ID = MR.MPA_RATING_ID" +
                        " left join FILM_GENRE FG on F.FILM_ID = FG.FILM_ID" +
                        " left join GENRE G ON FG.GENRE_ID = G.GENRE_ID;");

        List<Film> filmsList = new ArrayList<>();
        if (filmsRows.next()) {
            ArrayList<String> genreList = getGenreListByFilmId(filmsRows);

            Set<Integer> likeList = getLikesListByFilmId(filmsRows);

            filmsList.add(new Film(
                    filmsRows.getInt("FILM_ID"),
                    filmsRows.getString("FILM_NAME"),
                    filmsRows.getString("DESCRIPTION"),
                    filmsRows.getString("RELEASE_DATE"),
                    filmsRows.getInt("DURATION"),
                    genreList,
                    likeList,
                    filmsRows.getString("MPA_RATING_ID")
            ));
        }
        return filmsList;
    }

    private Set<Integer> getLikesListByFilmId(SqlRowSet filmsRows) {
        SqlRowSet likesRows = jdbcTemplate.queryForRowSet(
                "SELECT * FROM FILM_LIKES WHERE FILM_ID = ?",
                filmsRows.getString("FILM_ID")
        );
        Set<Integer> likeList = new HashSet<>();
        if (likesRows.next()) {
            likeList.add(likesRows.getInt("USER_ID"));
        }
        return likeList;
    }

    private ArrayList<String> getGenreListByFilmId(SqlRowSet filmsRows) {
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet(
                "SELECT FILM_ID, GENRE_NAME FROM GENRE " +
                        "left join FILM_GENRE FG on GENRE.GENRE_ID = FG.GENRE_ID WHERE FILM_ID = ?",
                filmsRows.getString("FILM_ID")
        );

        ArrayList<String> genreList = new ArrayList<>();
        if (genreRows.next()) {
            genreList.add(genreRows.getString("GENRE_NAME"));
        }
        return genreList;
    }

    @Override
    public Film createFilm(Film film) {
        Set<Integer> filmLikes = film.getLikes();
        ArrayList<String> genreList = film.getGenres();
        SqlRowSet filmsRows = jdbcTemplate.queryForRowSet(
                "SELECT MPA_RATING_NAME FROM MPA_RATING WHERE MPA_RATING_ID = ?",
                film.getMpa());

        String mpaRatingName = null;
        if (filmsRows.next()) {
            mpaRatingName = filmsRows.getString("MPA_RATING_NAME");
        }

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("FILMS")
                .usingGeneratedKeyColumns("FILM_ID");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("FILM_NAME", film.getName());
        parameters.put("DESCRIPTION", film.getDescription());
        parameters.put("RELEASE_DATE", film.getReleaseDate());
        parameters.put("DURATION", film.getDuration());
        parameters.put("MPA_RATING_ID", mpaRatingName);
        Number filmId = simpleJdbcInsert.executeAndReturnKey(parameters);


        if (filmLikes != null) {
            filmLikes.forEach(userId ->
                    jdbcTemplate.update("INSERT INTO FILM_LIKES (FILM_ID, USER_ID) VALUES (?, ?)", filmId, userId)
            );
        }

        if (genreList != null) {
            genreList.forEach(genre -> {
                        SqlRowSet genreRowSet = jdbcTemplate.queryForRowSet("SELECT GENRE_ID FROM GENRE " +
                                "WHERE GENRE_NAME = ?", genre);
                        String genreId = genreRowSet.getString("GENRE_ID");
                        jdbcTemplate.update("INSERT INTO FILM_GENRE (FILM_ID, GENRE_ID) VALUES (?, ?)", filmId, genreId);
                    }
            );
        }

        log.debug("Добавлен фильм: {}", film.getName());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        validateFilmExist(film.getId());
        Set<Integer> filmLikes = film.getLikes();
        ArrayList<String> genreList = film.getGenres();

        if (filmLikes != null) {
            filmLikes.forEach(user ->
                    jdbcTemplate.update("INSERT INTO FILM_LIKES (FILM_ID, USER_ID) VALUES (?, ?)", film.getId(), user)
            );
        }

        if (genreList != null) {
            genreList.forEach(genre -> {
                        SqlRowSet genreRowSet = jdbcTemplate.queryForRowSet("SELECT GENRE_ID FROM GENRE " +
                                "WHERE GENRE_NAME = ?", genre);
                        String genreId = genreRowSet.getString("GENRE_ID");
                        jdbcTemplate.update("INSERT INTO FILM_GENRE (FILM_ID, GENRE_ID) VALUES (?, ?)",
                                film.getId(),
                                genreId);
                    }
            );
        }

        String sqlQuery = "update FILMS set " +
                "FILM_NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ?, MPA_RATING_ID = ?" +
                "where FILM_ID = ?";

        jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa(),
                film.getId());

        log.debug("Обновлен фильм: {}", film.getName());
        return film;
    }

    @Override
    public List<String> getGenresList() {
        SqlRowSet genresRows = jdbcTemplate.queryForRowSet("SELECT GENRE_NAME FROM GENRE");
        List<String> genreList = new ArrayList<>();
        if (genresRows.next()) {
            genreList.add(genresRows.getString("GENRE_NAME"));
        }
        return genreList;
    }

    @Override
    public List<String> getFilmGenres(Integer filmId) {
        validateFilmExist(filmId);

        SqlRowSet genresRows = jdbcTemplate.queryForRowSet("SELECT GENRE_NAME FROM GENRE " +
                "LEFT JOIN FILM_GENRE FG ON GENRE.GENRE_ID = FG.GENRE_ID WHERE FILM_ID = ?", filmId);
        List<String> genreList = new ArrayList<>();
        if (genresRows.next()) {
            genreList.add(genresRows.getString("GENRE_NAME"));
        }
        return genreList;
    }

    @Override
    public Optional<Film> setLike(Integer filmId, Integer userId) {
        validateFilmExist(filmId);
        jdbcTemplate.update("INSERT INTO FILM_LIKES (FILM_ID, USER_ID) VALUES (?, ?)", filmId, userId);
        return findFilmById(filmId);
    }

    public Optional<Film> findFilmById(Integer filmId) {
        validateFilmExist(filmId);
        SqlRowSet filmsRows = jdbcTemplate.queryForRowSet("SELECT * FROM FILMS WRERE WHERE FILM_ID = ?", filmId);
        if (filmsRows.next()) {
            ArrayList<String> genreList = getGenreListByFilmId(filmsRows);

            Set<Integer> likeList = getLikesListByFilmId(filmsRows);

            return Optional.of(new Film(
                    filmsRows.getInt("FILM_ID"),
                    filmsRows.getString("FILM_NAME"),
                    filmsRows.getString("DESCRIPTION"),
                    filmsRows.getString("RELEASE_DATE"),
                    filmsRows.getInt("DURATION"),
                    genreList,
                    likeList,
                    filmsRows.getString("MPA_RATING_ID")
            ));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Film> deleteLike(Integer filmId, Integer userId) {
        validateFilmExist(filmId);
        jdbcTemplate.update("DELETE FROM FILM_LIKES WHERE USER_ID = ?", userId);
        return findFilmById(filmId);
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {
        int currentFilmCount = 3;
        if (count != null) {
            currentFilmCount = count;
        }
        return findAllFilms().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .limit(currentFilmCount)
                .collect(Collectors.toList());
    }

    private void validateFilmExist(Integer filmId) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT FILM_ID FROM FILMS WHERE FILM_ID = ?", filmId);
        if (!filmRows.next()) {
            log.error("Film with this id {} wasn't found", filmId);
            throw new FilmNotFoundException(filmId);
        }
    }
}
