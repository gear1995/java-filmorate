create table IF NOT EXISTS users
(
    user_id  INTEGER AUTO_INCREMENT NOT NULL,
    email    CHARACTER VARYING(100) NOT NULL UNIQUE,
    name     CHARACTER VARYING(50),
    birthday DATE                   NOT NULL,
    login    CHARACTER VARYING(50)  NOT NULL unique,
    constraint users_PK
        primary key (user_id)
);

create table IF NOT EXISTS mpa_rating
(
    mpa_rating_id   INTEGER               NOT NULL AUTO_INCREMENT,
    mpa_rating_name CHARACTER VARYING(10) NOT NULL UNIQUE,
    constraint mpa_rating_PK
        primary key (mpa_rating_id)
);


create table IF NOT EXISTS films
(
    film_id       INTEGER AUTO_INCREMENT NOT NULL,
    film_name     CHARACTER VARYING(100) NOT NULL,
    description   CHARACTER VARYING(200),
    release_date  DATE                   NOT NULL,
    duration      INTEGER,
    mpa_rating_id INTEGER,
    constraint films_PK
        primary key (film_id),
    constraint films_mpa_rating_mpa_rating_id_FK
        foreign key (mpa_rating_id) references mpa_rating
);

create table IF NOT EXISTS film_likes
(
    film_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    constraint film_likes_PK
        primary key (film_id, user_id),
    constraint film_likes_films_film_id_FK
        foreign key (film_id) references films (film_id),
    constraint film_likes_users_user_id_fk
        foreign key (user_id) references users (user_id)
);

create table IF NOT EXISTS friends
(
    first_user_id     INTEGER               NOT NULL,
    second_user_id    INTEGER               NOT NULL,
    friendship_status CHARACTER VARYING(50) NOT NULL,
    constraint friends_PK
        primary key (first_user_id, second_user_id),
    constraint friends_users_user_id_FK
        foreign key (first_user_id) references users (user_id),
    constraint friends_users_user_id_FK_2
        foreign key (SECOND_USER_ID) references users (user_id)
);

create table IF NOT EXISTS genre
(
    genre_id   INTEGER AUTO_INCREMENT NOT NULL,
    genre_name CHARACTER VARYING(100) UNIQUE,
    constraint genre_PK
        primary key (genre_id)
);

create table IF NOT EXISTS film_genre
(
    film_id  INTEGER NOT NULL,
    genre_id INTEGER NOT NULL,
    constraint film_genre_PK
        primary key (film_id, genre_id),
    constraint film_genre_films_film_id_FK
        foreign key (film_id) references films (film_id),
    constraint film_genre_genre_genre_id_FK
        foreign key (genre_id) references genre (genre_id)
);

MERGE INTO genre (genre_id, genre_name)
    VALUES (1, 'Комедия'),
           (2, 'Драма'),
           (3, 'Мультфильм'),
           (4, 'Триллер'),
           (5, 'Документальный'),
           (6, 'Боевик');

MERGE INTO mpa_rating (mpa_rating_id, mpa_rating_name)
    VALUES (1, 'G'),
           (2, 'PG'),
           (3, 'PG-13'),
           (4, 'R'),
           (5, 'NC-17');