create table IF NOT EXISTS USERS
(
    USER_ID  INTEGER AUTO_INCREMENT not null,
    EMAIL    CHARACTER VARYING(100) not null UNIQUE,
    NAME     CHARACTER VARYING(50),
    BIRTHDAY DATE                   not null,
    LOGIN    CHARACTER VARYING(50)  not null unique,
    constraint USERS_PK
        primary key (USER_ID)
);
INSERT INTO USERS (USER_ID, EMAIL, NAME, BIRTHDAY, LOGIN)
VALUES ( 0, 'email', 'name', '1976-09-20', 'login');

create table IF NOT EXISTS MPA_RATING
(
    MPA_RATING_ID   INTEGER               NOT NULL AUTO_INCREMENT,
    MPA_RATING_NAME CHARACTER VARYING(10) not null UNIQUE,
    constraint MPA_RATING_PK
        primary key (MPA_RATING_ID)
);


create table IF NOT EXISTS FILMS
(
    FILM_ID       INTEGER AUTO_INCREMENT NOT NULL,
    FILM_NAME     CHARACTER VARYING(100) not null,
    DESCRIPTION   CHARACTER VARYING(200),
    RELEASE_DATE  DATE                   not null,
    DURATION      INTEGER,
    MPA_RATING_ID INTEGER,
    constraint FILMS_PK
        primary key (FILM_ID),
    constraint FILMS_MPA_RATING_MPA_RATING_ID_FK
        foreign key (MPA_RATING_ID) references MPA_RATING
);
INSERT INTO FILMS (FILM_NAME, RELEASE_DATE)
VALUES ( 'film name', '2020-08-08');

create table IF NOT EXISTS FILM_LIKES
(
    FILM_ID INTEGER not null,
    USER_ID INTEGER not null,
    constraint FILM_LIKES_PK
        primary key (FILM_ID, USER_ID),
    constraint FILM_LIKES_FILMS_FILM_ID_FK
        foreign key (FILM_ID) references FILMS (FILM_ID),
    constraint FILM_LIKES_USERS_USER_ID_FK
        foreign key (USER_ID) references USERS (USER_ID)
);

create table IF NOT EXISTS FRIENDS
(
    FIRST_USER_ID     INTEGER               not null,
    SECOND_USER_ID    INTEGER               not null,
    FRIENDSHIP_STATUS CHARACTER VARYING(50) not null,
    constraint FRIENDS_PK
        primary key (FIRST_USER_ID, SECOND_USER_ID),
    constraint FRIENDS_USERS_USER_ID_FK
        foreign key (FIRST_USER_ID) references USERS (USER_ID),
    constraint FRIENDS_USERS_USER_ID_FK_2
        foreign key (SECOND_USER_ID) references USERS (USER_ID)
);

create table IF NOT EXISTS GENRE
(
    GENRE_ID   INTEGER AUTO_INCREMENT not null,
    GENRE_NAME CHARACTER VARYING(100) UNIQUE,
    constraint GENRE_PK
        primary key (GENRE_ID)
);
INSERT INTO GENRE (GENRE_NAME)
VALUES ( 'genre name');

create table IF NOT EXISTS FILM_GENRE
(
    FILM_ID  INTEGER not null,
    GENRE_ID INTEGER not null,
    constraint FILM_GENRE_PK
        primary key (FILM_ID, GENRE_ID),
    constraint FILM_GENRE_FILMS_FILM_ID_FK
        foreign key (FILM_ID) references FILMS (FILM_ID),
    constraint FILM_GENRE_GENRE_GENRE_ID_FK
        foreign key (GENRE_ID) references GENRE (GENRE_ID)
);



