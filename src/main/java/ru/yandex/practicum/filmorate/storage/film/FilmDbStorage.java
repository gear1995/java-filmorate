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
            Integer filmId = filmsRows.getInt("FILM_ID");
            ArrayList<HashMap<String, Integer>> genresMap = getGenreIdMapByFilmId(filmId);
            Set<Integer> likeList = getLikesListByFilmId(filmId);
            HashMap<String, Integer> mpaMap = new HashMap<>();
            mpaMap.put("id", filmsRows.getInt("MPA_RATING_ID"));

            filmsList.add(new Film(
                    filmId,
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

    private Set<Integer> getLikesListByFilmId(Integer filmId) {
        SqlRowSet likesRows = jdbcTemplate.queryForRowSet("SELECT * FROM FILM_LIKES WHERE FILM_ID = ?", filmId);
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

    private ArrayList<HashMap<String, Integer>> getGenreIdMapByFilmId(Integer filmId) {
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet("SELECT GENRE_ID FROM FILM_GENRE WHERE FILM_ID = ?", filmId);
        ArrayList<HashMap<String, Integer>> genreList = new ArrayList<>();
        if (genreRows.next()) {
            HashMap<String, Integer> genreMap = new HashMap<>();
            genreMap.put("id", genreRows.getInt("GENRE_ID"));
            genreList.add(genreMap);
        }
        return genreList;
    }

    @Override
    public Film createFilm(Film film) {
        Set<Integer> filmLikes = film.getLikes();
        ArrayList<HashMap<String, Integer>> genresMapList = film.getGenres();

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("FILMS")
                .usingGeneratedKeyColumns("FILM_ID");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("FILM_NAME", film.getName());
        parameters.put("DESCRIPTION", film.getDescription());
        parameters.put("RELEASE_DATE", film.getReleaseDate());
        parameters.put("DURATION", film.getDuration());
        parameters.put("MPA_RATING_ID", film.getMpa().get("id"));
        Number filmId = simpleJdbcInsert.executeAndReturnKey(parameters);

        if (filmLikes != null) {
            String sqlQuery = "MERGE INTO FILM_LIKES KEY(FILM_ID, USER_ID) VALUES (?, ?)";
            filmLikes.forEach(user -> jdbcTemplate.update(sqlQuery, film.getId(), user));
        }

        if (genresMapList != null) {
            String sqlQuery = "MERGE INTO FILM_GENRE KEY(FILM_ID, GENRE_ID) VALUES (?, ?)";
            genresMapList.forEach(map -> jdbcTemplate.update(sqlQuery, film.getId(), map.get("id")));
        }

        log.debug("Добавлен фильм: {}", film.getName());

        return findFilmById(filmId.intValue()).get();
    }

    @Override
    public Film updateFilm(Film film) {
        Integer filmId = film.getId();
        validateFilmExist(film.getId());
        Set<Integer> filmLikes = film.getLikes();
        ArrayList<HashMap<String, Integer>> genresIdMap = film.getGenres();

        jdbcTemplate.update("DELETE FROM FILM_LIKES WHERE FILM_ID = ?", filmId);
        if (filmLikes != null) {
            String sqlQuery = "MERGE INTO FILM_LIKES (FILM_ID, USER_ID) VALUES (?, ?)";
            filmLikes.forEach(user -> jdbcTemplate.update(sqlQuery, filmId, user));
        }
        jdbcTemplate.update("DELETE FROM FILM_GENRE WHERE FILM_ID = ?", filmId);
        if (genresIdMap != null) {
            String sqlQuery = "MERGE INTO FILM_GENRE (FILM_ID, GENRE_ID) VALUES (?, ?)";
            genresIdMap.forEach(map -> jdbcTemplate.update(sqlQuery, film.getId(), map.get("id")));
        }

        String sqlQuery = "UPDATE FILMS SET " +
                "FILM_NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ?, MPA_RATING_ID = ?" +
                "WHERE FILM_ID = ?";

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
        jdbcTemplate.update("MERGE INTO FILM_LIKES (FILM_ID, USER_ID) VALUES (?, ?)", filmId, userId);
        return findFilmById(filmId);
    }

    public Optional<Film> findFilmById(Integer filmId) {
        validateFilmExist(filmId);
        SqlRowSet filmsRows = jdbcTemplate.queryForRowSet("SELECT * FROM FILMS WRERE WHERE FILM_ID = ?", filmId);
        if (filmsRows.next()) {
            ArrayList<HashMap<String, Integer>> genresIdMap = getGenreIdMapByFilmId(filmId);
            Set<Integer> likeList = getLikesListByFilmId(filmId);

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
