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
                "SELECT F.film_id, film_name, description, release_date, duration, F.mpa_rating_id, mpa_rating_name, FG.genre_id" +
                        " FROM films AS F" +
                        " LEFT JOIN film_genre FG ON F.film_id = FG.film_id" +
                        " LEFT JOIN genre G ON FG.genre_id = G.genre_id " +
                        " LEFT JOIN mpa_rating MR on F.mpa_rating_id = MR.mpa_rating_id;"
        );

        List<Film> filmsList = new ArrayList<>();
        while (filmsRows.next()) {
            Integer filmId = filmsRows.getInt("film_id");
            ArrayList<FilmData> genresList = getGenreIdMapByFilmId(filmId);
            Set<Integer> likeList = getLikesListByFilmId(filmId);
            FilmData mpaData = new FilmData(filmsRows.getInt("mpa_rating_id"),
                    filmsRows.getString("mpa_rating_name"));

            filmsList.add(new Film(
                    filmId,
                    filmsRows.getString("film_name"),
                    filmsRows.getString("description"),
                    filmsRows.getString("release_date"),
                    filmsRows.getInt("duration"),
                    genresList,
                    likeList,
                    mpaData
            ));
        }
        return filmsList;
    }

    @Override
    public Film createFilm(Film film) {
        Set<Integer> filmLikes = film.getLikes();
        ArrayList<FilmData> genresList = film.getGenres();

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("film_name", film.getName());
        parameters.put("description", film.getDescription());
        parameters.put("release_date", film.getReleaseDate());
        parameters.put("duration", film.getDuration());
        parameters.put("mpa_rating_id", film.getMpa().getId());
        Number filmId = simpleJdbcInsert.executeAndReturnKey(parameters);

        if (filmLikes != null) {
            String sqlQuery = "MERGE INTO film_likes (film_id, user_id) VALUES (?, ?)";
            filmLikes.forEach(user -> jdbcTemplate.update(sqlQuery, filmId, user));
        }

        if (genresList != null) {
            String sqlQuery = "MERGE INTO film_genre KEY (film_id, genre_id) VALUES (?, ?)";
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
        jdbcTemplate.update("DELETE FROM film_likes WHERE film_id = ?", filmId);
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = ?", filmId);
        deleteFilmById(filmId);

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("films");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("film_id", filmId);
        parameters.put("film_name", film.getName());
        parameters.put("description", film.getDescription());
        parameters.put("release_date", film.getReleaseDate());
        parameters.put("duration", film.getDuration());
        parameters.put("mpa_rating_id", film.getMpa().getId());
        simpleJdbcInsert.execute(parameters);

        if (filmLikes != null) {
            String addLikesQuery = "MERGE INTO film_likes (film_id, user_id) VALUES (?, ?)";
            filmLikes.forEach(user -> jdbcTemplate.update(addLikesQuery, filmId, user));
        }
        if (genresIdMap != null) {
            String addGenresQuery = "MERGE INTO film_genre (film_id, genre_id) VALUES (?, ?)";
            genresIdMap.forEach(filmData -> jdbcTemplate.update(addGenresQuery, filmId, filmData.getId()));
        }

        log.debug("Обновлен фильм: {}", film.getName());
        return this.getFilmById(film.getId()).get();
    }

    @Override
    public ArrayList<FilmData> getAllGenres() {
        SqlRowSet genresRows = jdbcTemplate.queryForRowSet("SELECT genre_id, genre_name FROM genre ORDER BY genre_id");
        ArrayList<FilmData> genreList = new ArrayList<>();
        while (genresRows.next()) {
            genreList.add(new FilmData(genresRows.getInt("genre_id"), genresRows.getString("genre_name")));
        }
        return genreList;
    }

    @Override
    public FilmData getGenresById(Integer genreId) {
        SqlRowSet genresRows = jdbcTemplate.queryForRowSet("SELECT genre_id, genre_name FROM genre WHERE genre_id = ?",
                genreId);
        if (genresRows.next()) {
            return new FilmData(genresRows.getInt("genre_id"), genresRows.getString("genre_name"));
        }
        log.error("Genre with this id {} wasn't found", genreId);
        throw new GenreNotFoundException(genreId);
    }

    @Override
    public Optional<Film> setLike(Integer filmId, Integer userId) {
        validateFilmExist(filmId);
        jdbcTemplate.update("MERGE INTO film_likes (film_id, user_id) VALUES (?, ?)", filmId, userId);
        return this.getFilmById(filmId);
    }

    @Override
    public Optional<Film> getFilmById(Integer filmId) {
        validateFilmExist(filmId);
        SqlRowSet filmsRows = jdbcTemplate.queryForRowSet("SELECT * FROM films " +
                "LEFT JOIN mpa_rating ON films.mpa_rating_id = mpa_rating.mpa_rating_id WHERE film_id = ?", filmId);
        if (filmsRows.next()) {
            ArrayList<FilmData> genresIdMap = getGenreIdMapByFilmId(filmId);
            Set<Integer> likeList = getLikesListByFilmId(filmId);
            FilmData filmData = new FilmData(filmsRows.getInt("mpa_rating_id"), filmsRows.getString("mpa_rating_name"));

            return Optional.of(new Film(
                    filmsRows.getInt("film_id"),
                    filmsRows.getString("film_name"),
                    filmsRows.getString("description"),
                    filmsRows.getString("release_date"),
                    filmsRows.getInt("duration"),
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
        jdbcTemplate.update("DELETE FROM film_likes WHERE user_id = ?", userId);
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
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT mpa_rating_id, mpa_rating_name " +
                "FROM mpa_rating WHERE mpa_rating_id = ?", mpaId);

        if (mpaRows.next()) {
            return new FilmData(mpaRows.getInt("mpa_rating_id"),
                    mpaRows.getString("mpa_rating_name"));
        }
        log.error("MPA with this id {} wasn't found", mpaId);
        throw new MpaNotFoundException(mpaId);
    }

    @Override
    public ArrayList<FilmData> getMpa() {
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT mpa_rating_id, mpa_rating_name " +
                "FROM mpa_rating ORDER BY mpa_rating_id");
        ArrayList<FilmData> mpaList = new ArrayList<>();
        while (mpaRows.next()) {
            mpaList.add(new FilmData(mpaRows.getInt("mpa_rating_id"),
                    mpaRows.getString("mpa_rating_name")));
        }
        return mpaList;
    }

    private void validateFilmExist(Integer filmId) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT film_id FROM films WHERE film_id = ?", filmId);
        if (!filmRows.next()) {
            log.error("Film with this id {} wasn't found", filmId);
            throw new FilmNotFoundException(filmId);
        }
    }

    private void deleteFilmById(Integer filmId) {
        jdbcTemplate.update("DELETE FROM films WHERE film_id =?", filmId);
    }


    private Set<Integer> getLikesListByFilmId(Integer filmId) {
        SqlRowSet likesRows = jdbcTemplate.queryForRowSet("SELECT * FROM film_likes WHERE film_id = ?", filmId);
        Set<Integer> likeList = new HashSet<>();
        while (likesRows.next()) {
            likeList.add(likesRows.getInt("user_id"));
        }
        return likeList;
    }

    private ArrayList<FilmData> getGenreIdMapByFilmId(Integer filmId) {
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet("SELECT FG.genre_id, G.genre_name FROM film_genre FG " +
                "LEFT JOIN genre G ON FG.genre_id = G.genre_id WHERE film_id = ?", filmId);
        ArrayList<FilmData> genreList = new ArrayList<>();
        while (genreRows.next()) {
            genreList.add(new FilmData(genreRows.getInt("genre_id"),
                    genreRows.getString("genre_name")));
        }
        return genreList;
    }
}
