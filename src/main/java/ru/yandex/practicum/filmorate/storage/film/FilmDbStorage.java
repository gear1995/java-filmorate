package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exeption.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exeption.GenreNotFoundException;
import ru.yandex.practicum.filmorate.exeption.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmData;

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
                "SELECT F.FILM_ID, FILM_NAME, DESCRIPTION, RELEASE_DATE, DURATION, F.MPA_RATING_ID, MPA_RATING_NAME, FG.GENRE_ID" +
                        " FROM FILMS AS F" +
                        " LEFT JOIN FILM_GENRE FG ON F.FILM_ID = FG.FILM_ID" +
                        " LEFT JOIN GENRE G ON FG.GENRE_ID = G.GENRE_ID " +
                        " LEFT JOIN MPA_RATING MR on F.MPA_RATING_ID = MR.MPA_RATING_ID;"
        );

        List<Film> filmsList = new ArrayList<>();
        while (filmsRows.next()) {
            Integer filmId = filmsRows.getInt("FILM_ID");
            ArrayList<FilmData> genresList = getGenreIdMapByFilmId(filmId);
            Set<Integer> likeList = getLikesListByFilmId(filmId);
            FilmData mpaData = new FilmData(filmsRows.getInt("MPA_RATING_ID"),
                    filmsRows.getString("MPA_RATING_NAME"));

            filmsList.add(new Film(
                    filmId,
                    filmsRows.getString("FILM_NAME"),
                    filmsRows.getString("DESCRIPTION"),
                    filmsRows.getString("RELEASE_DATE"),
                    filmsRows.getInt("DURATION"),
                    genresList,
                    likeList,
                    mpaData
            ));
        }
        return filmsList;
    }

    private Set<Integer> getLikesListByFilmId(Integer filmId) {
        SqlRowSet likesRows = jdbcTemplate.queryForRowSet("SELECT * FROM FILM_LIKES WHERE FILM_ID = ?", filmId);
        Set<Integer> likeList = new HashSet<>();
        while (likesRows.next()) {
            likeList.add(likesRows.getInt("USER_ID"));
        }
        return likeList;
    }

    private ArrayList<FilmData> getGenreIdMapByFilmId(Integer filmId) {
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet("SELECT FG.GENRE_ID, G.GENRE_NAME FROM FILM_GENRE FG " +
                "LEFT JOIN GENRE G ON FG.GENRE_ID = G.GENRE_ID WHERE FILM_ID = ?", filmId);
        ArrayList<FilmData> genreList = new ArrayList<>();
        while (genreRows.next()) {
            genreList.add(new FilmData(genreRows.getInt("GENRE_ID"),
                    genreRows.getString("GENRE_NAME")));
        }
        return genreList;
    }

    @Override
    public Film createFilm(Film film) {
        Set<Integer> filmLikes = film.getLikes();
        ArrayList<FilmData> genresList = film.getGenres();

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("FILMS")
                .usingGeneratedKeyColumns("FILM_ID");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("FILM_NAME", film.getName());
        parameters.put("DESCRIPTION", film.getDescription());
        parameters.put("RELEASE_DATE", film.getReleaseDate());
        parameters.put("DURATION", film.getDuration());
        parameters.put("MPA_RATING_ID", film.getMpa().getId());
        Number filmId = simpleJdbcInsert.executeAndReturnKey(parameters);

        if (filmLikes != null) {
            String sqlQuery = "MERGE INTO FILM_LIKES (FILM_ID, USER_ID) VALUES (?, ?)";
            filmLikes.forEach(user -> jdbcTemplate.update(sqlQuery, filmId, user));
        }

        if (genresList != null) {
            String sqlQuery = "MERGE INTO FILM_GENRE KEY (FILM_ID, GENRE_ID) VALUES (?, ?)";
            genresList.forEach(filmData -> jdbcTemplate.update(sqlQuery, filmId, filmData.getId()));
        }

        log.debug("Добавлен фильм: {}", film.getName());
        return this.getFilmById(filmId.intValue()).get();
    }

    @Override
    public Film updateFilm(Film film) {
        Integer filmId = film.getId();
        validateFilmExist(film.getId());
        Set<Integer> filmLikes = film.getLikes();
        ArrayList<FilmData> genresIdMap = film.getGenres();
        jdbcTemplate.update("DELETE FROM FILM_LIKES WHERE FILM_ID = ?", filmId);
        jdbcTemplate.update("DELETE FROM FILM_GENRE WHERE FILM_ID = ?", filmId);
        deleteFilmById(filmId);

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("FILMS");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("FILM_ID", filmId);
        parameters.put("FILM_NAME", film.getName());
        parameters.put("DESCRIPTION", film.getDescription());
        parameters.put("RELEASE_DATE", film.getReleaseDate());
        parameters.put("DURATION", film.getDuration());
        parameters.put("MPA_RATING_ID", film.getMpa().getId());
        simpleJdbcInsert.execute(parameters);

        if (filmLikes != null) {
            String addLikesQuery = "MERGE INTO FILM_LIKES (FILM_ID, USER_ID) VALUES (?, ?)";
            filmLikes.forEach(user -> jdbcTemplate.update(addLikesQuery, filmId, user));
        }
        if (genresIdMap != null) {
            String addGenresQuery = "MERGE INTO FILM_GENRE (FILM_ID, GENRE_ID) VALUES (?, ?)";
            genresIdMap.forEach(filmData -> jdbcTemplate.update(addGenresQuery, filmId, filmData.getId()));
        }

        log.debug("Обновлен фильм: {}", film.getName());
        return this.getFilmById(film.getId()).get();
    }

    private void deleteFilmById(Integer filmId) {
        jdbcTemplate.update("DELETE FROM FILMS WHERE FILM_ID =?", filmId);
    }

    @Override
    public ArrayList<FilmData> getAllGenres() {
        SqlRowSet genresRows = jdbcTemplate.queryForRowSet("SELECT GENRE_ID, GENRE_NAME FROM GENRE ORDER BY GENRE_ID");
        ArrayList<FilmData> genreList = new ArrayList<>();
        while (genresRows.next()) {
            genreList.add(new FilmData(genresRows.getInt("GENRE_ID"), genresRows.getString("GENRE_NAME")));
        }
        return genreList;
    }

    @Override
    public FilmData getGenresById(Integer genreId) {
        SqlRowSet genresRows = jdbcTemplate.queryForRowSet("SELECT GENRE_ID, GENRE_NAME FROM GENRE WHERE GENRE_ID = ?",
                genreId);
        if (genresRows.next()) {
            return new FilmData(genresRows.getInt("GENRE_ID"), genresRows.getString("GENRE_NAME"));
        }
        log.error("Genre with this id {} wasn't found", genreId);
        throw new GenreNotFoundException(genreId);
    }

    @Override
    public Optional<Film> setLike(Integer filmId, Integer userId) {
        validateFilmExist(filmId);
        jdbcTemplate.update("MERGE INTO FILM_LIKES (FILM_ID, USER_ID) VALUES (?, ?)", filmId, userId);
        return this.getFilmById(filmId);
    }

    @Override
    public Optional<Film> getFilmById(Integer filmId) {
        validateFilmExist(filmId);
        SqlRowSet filmsRows = jdbcTemplate.queryForRowSet("SELECT * FROM FILMS " +
                "LEFT JOIN MPA_RATING ON FILMS.MPA_RATING_ID = MPA_RATING.MPA_RATING_ID WHERE FILM_ID = ?", filmId);
        if (filmsRows.next()) {
            ArrayList<FilmData> genresIdMap = getGenreIdMapByFilmId(filmId);
            Set<Integer> likeList = getLikesListByFilmId(filmId);
            FilmData filmData = new FilmData(filmsRows.getInt("MPA_RATING_ID"), filmsRows.getString("MPA_RATING_NAME"));

            return Optional.of(new Film(
                    filmsRows.getInt("FILM_ID"),
                    filmsRows.getString("FILM_NAME"),
                    filmsRows.getString("DESCRIPTION"),
                    filmsRows.getString("RELEASE_DATE"),
                    filmsRows.getInt("DURATION"),
                    genresIdMap,
                    likeList,
                    filmData
            ));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Film> deleteLike(Integer filmId, Integer userId) {
        validateFilmExist(filmId);
        jdbcTemplate.update("DELETE FROM FILM_LIKES WHERE USER_ID = ?", userId);
        return this.getFilmById(filmId);
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
    public FilmData getMpaById(Integer mpaId) {
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT MPA_RATING_ID, MPA_RATING_NAME " +
                "FROM MPA_RATING WHERE MPA_RATING_ID = ?", mpaId);

        if (mpaRows.next()) {
            return new FilmData(mpaRows.getInt("MPA_RATING_ID"),
                    mpaRows.getString("MPA_RATING_NAME"));
        }
        log.error("MPA with this id {} wasn't found", mpaId);
        throw new MpaNotFoundException(mpaId);
    }

    @Override
    public ArrayList<FilmData> getMpa() {
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT MPA_RATING_ID, MPA_RATING_NAME " +
                "FROM MPA_RATING ORDER BY MPA_RATING_ID");
        ArrayList<FilmData> mpaList = new ArrayList<>();
        while (mpaRows.next()) {
            mpaList.add(new FilmData(mpaRows.getInt("MPA_RATING_ID"),
                    mpaRows.getString("MPA_RATING_NAME")));
        }
        return mpaList;
    }

    private void validateFilmExist(Integer filmId) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT FILM_ID FROM FILMS WHERE FILM_ID = ?", filmId);
        if (!filmRows.next()) {
            log.error("Film with this id {} wasn't found", filmId);
            throw new FilmNotFoundException(filmId);
        }
    }
}
