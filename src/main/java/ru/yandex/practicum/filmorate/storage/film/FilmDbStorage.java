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
                "SELECT F.FILM_ID, FILM_NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_RATING_ID, FG.GENRE_ID" +
                        " FROM FILMS AS F" +
                        " LEFT JOIN FILM_GENRE FG ON F.FILM_ID = FG.FILM_ID" +
                        " LEFT JOIN GENRE G ON FG.GENRE_ID = G.GENRE_ID;"
        );

        List<Film> filmsList = new ArrayList<>();
        if (filmsRows.next()) {
            HashMap<String, Integer> genresMap = getGenreIdMapByFilmId(filmsRows);

            Set<Integer> likeList = getLikesListByFilmId(filmsRows);

            HashMap<String, Integer> mpaMap = new HashMap<>();
            mpaMap.put("id", filmsRows.getInt("MPA_RATING_ID"));
            filmsList.add(new Film(
                    filmsRows.getInt("FILM_ID"),
                    filmsRows.getString("FILM_NAME"),
                    filmsRows.getString("DESCRIPTION"),
                    filmsRows.getString("RELEASE_DATE"),
                    filmsRows.getInt("DURATION"),
                    genresMap,
                    likeList,
                    mpaMap
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

    private HashMap<String, String> getGenreNameMapByFilmId(SqlRowSet filmsRows) {
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet(
                "SELECT GENRE_NAME FROM GENRE " +
                        "LEFT JOIN FILM_GENRE FG on GENRE.GENRE_ID = FG.GENRE_ID WHERE FILM_ID = ?",
                filmsRows.getString("FILM_ID")
        );

        HashMap<String, String> genreList = new HashMap<>();
        if (genreRows.next()) {
            genreList.put("id", genreRows.getString("GENRE_NAME"));
        }
        return genreList;
    }

    private HashMap<String, Integer> getGenreIdMapByFilmId(SqlRowSet filmsRows) {
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet(
                "SELECT FG.GENRE_ID FROM GENRE " +
                        "LEFT JOIN FILM_GENRE FG on GENRE.GENRE_ID = FG.GENRE_ID WHERE FILM_ID = ?",
                filmsRows.getString("FILM_ID")
        );

        HashMap<String, Integer> genreList = new HashMap<>();
        if (genreRows.next()) {
            genreList.put("id", genreRows.getInt("GENRE_ID"));
        }
        return genreList;
    }

    @Override
    public Film createFilm(Film film) {
        Set<Integer> filmLikes = film.getLikes();
        HashMap<String, Integer> genresMap = film.getGenres();
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

        if (genresMap != null) {
            genresMap.forEach((str, value) ->
                    jdbcTemplate.update("INSERT INTO FILM_GENRE (FILM_ID, GENRE_ID) VALUES (?, ?)", filmId, value)
            );
        }

        log.debug("Добавлен фильм: {}", film.getName());
        return findFilmById(filmId.intValue()).get();
    }

    @Override
    public Film updateFilm(Film film) {
        validateFilmExist(film.getId());
        Set<Integer> filmLikes = film.getLikes();
        HashMap<String, Integer> genresIdMap = film.getGenres();

        if (filmLikes != null) {
            filmLikes.forEach(user ->
                    jdbcTemplate.update("INSERT INTO FILM_LIKES (FILM_ID, USER_ID) VALUES (?, ?)", film.getId(), user)
            );
        }

        if (genresIdMap != null) {
            genresIdMap.forEach((str, genreId) ->
                    jdbcTemplate.update("INSERT INTO FILM_GENRE (FILM_ID, GENRE_ID) VALUES (?, ?)",
                            film.getId(),
                            genreId)
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
        return findFilmById(film.getId()).get();
    }

    @Override
    public HashMap<String, Integer> getGenresList() {
        SqlRowSet genresRows = jdbcTemplate.queryForRowSet("SELECT GENRE_ID FROM GENRE");
        HashMap<String, Integer> genreList = new HashMap<>();
        if (genresRows.next()) {
            genreList.put("id", genresRows.getInt("GENRE_ID"));
        }
        return genreList;
    }

    @Override
    public HashMap<String, Integer> getFilmGenres(Integer filmId) {
        validateFilmExist(filmId);

        SqlRowSet genresRows = jdbcTemplate.queryForRowSet("SELECT FG.GENRE_ID FROM GENRE " +
                "LEFT JOIN FILM_GENRE FG ON GENRE.GENRE_ID = FG.GENRE_ID WHERE FILM_ID = ?", filmId);
        HashMap<String, Integer> genreList = new HashMap<>();
        if (genresRows.next()) {
            genreList.put("id", genresRows.getInt("FG.GENRE_ID"));
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
            HashMap<String, Integer> genresIdMap = getGenreIdMapByFilmId(filmsRows);

            Set<Integer> likeList = getLikesListByFilmId(filmsRows);

            HashMap<String, Integer> mpaMap = new HashMap<>();
            mpaMap.put("id", filmsRows.getInt("MPA_RATING_ID"));

            return Optional.of(new Film(
                    filmsRows.getInt("FILM_ID"),
                    filmsRows.getString("FILM_NAME"),
                    filmsRows.getString("DESCRIPTION"),
                    filmsRows.getString("RELEASE_DATE"),
                    filmsRows.getInt("DURATION"),
                    genresIdMap,
                    likeList,
                    mpaMap
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

    @Override
    public HashMap<String, Integer> getMpaById(Integer id) {
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT MPA_RATING_ID FROM MPA_RATING WHERE MPA_RATING_ID = ?", id);
        HashMap<String, Integer> mpaMap = new HashMap<>();
        if (mpaRows.next()) {
            mpaMap.put("id", mpaRows.getInt("MPA_RATING_ID"));
        }
        return mpaMap;
    }

    @Override
    public HashMap<String, Integer> getMpa() {
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT MPA_RATING_ID FROM MPA_RATING");
        HashMap<String, Integer> mpaRatingName = new HashMap<>();
        if (mpaRows.next()) {
            mpaRatingName.put("id", mpaRows.getInt("MPA_RATING_ID"));
        }
        return mpaRatingName;
    }

    private void validateFilmExist(Integer filmId) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT FILM_ID FROM FILMS WHERE FILM_ID = ?", filmId);
        if (!filmRows.next()) {
            log.error("Film with this id {} wasn't found", filmId);
            throw new FilmNotFoundException(filmId);
        }
    }
}
