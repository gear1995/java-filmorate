create table IF NOT EXISTS USERS
(
    USER_ID  INTEGER AUTO_INCREMENT NOT NULL,
    EMAIL    CHARACTER VARYING(100) NOT NULL UNIQUE,
    NAME     CHARACTER VARYING(50),
    BIRTHDAY DATE                   NOT NULL,
    LOGIN    CHARACTER VARYING(50)  NOT NULL unique,
    constraint USERS_PK
        primary key (USER_ID)
);

create table IF NOT EXISTS MPA_RATING
(
    MPA_RATING_ID   INTEGER               NOT NULL AUTO_INCREMENT,
    MPA_RATING_NAME CHARACTER VARYING(10) NOT NULL UNIQUE,
    constraint MPA_RATING_PK
        primary key (MPA_RATING_ID)
);


create table IF NOT EXISTS FILMS
(
    FILM_ID       INTEGER AUTO_INCREMENT NOT NULL,
    FILM_NAME     CHARACTER VARYING(100) NOT NULL,
    DESCRIPTION   CHARACTER VARYING(200),
    RELEASE_DATE  DATE                   NOT NULL,
    DURATION      INTEGER,
    MPA_RATING_ID INTEGER,
    constraint FILMS_PK
        primary key (FILM_ID),
    constraint FILMS_MPA_RATING_MPA_RATING_ID_FK
        foreign key (MPA_RATING_ID) references MPA_RATING
);

create table IF NOT EXISTS FILM_LIKES
(
    FILM_ID INTEGER NOT NULL,
    USER_ID INTEGER NOT NULL,
    constraint FILM_LIKES_PK
        primary key (FILM_ID, USER_ID),
    constraint FILM_LIKES_FILMS_FILM_ID_FK
        foreign key (FILM_ID) references FILMS (FILM_ID),
    constraint FILM_LIKES_USERS_USER_ID_FK
        foreign key (USER_ID) references USERS (USER_ID)
);

create table IF NOT EXISTS FRIENDS
(
    FIRST_USER_ID     INTEGER               NOT NULL,
    SECOND_USER_ID    INTEGER               NOT NULL,
    FRIENDSHIP_STATUS CHARACTER VARYING(50) NOT NULL,
    constraint FRIENDS_PK
        primary key (FIRST_USER_ID, SECOND_USER_ID),
    constraint FRIENDS_USERS_USER_ID_FK
        foreign key (FIRST_USER_ID) references USERS (USER_ID),
    constraint FRIENDS_USERS_USER_ID_FK_2
        foreign key (SECOND_USER_ID) references USERS (USER_ID)
);

create table IF NOT EXISTS GENRE
(
    GENRE_ID   INTEGER AUTO_INCREMENT NOT NULL,
    GENRE_NAME CHARACTER VARYING(100) UNIQUE,
    constraint GENRE_PK
        primary key (GENRE_ID)
);

create table IF NOT EXISTS FILM_GENRE
(
    FILM_ID  INTEGER NOT NULL,
    GENRE_ID INTEGER NOT NULL,
    constraint FILM_GENRE_PK
        primary key (FILM_ID, GENRE_ID),
    constraint FILM_GENRE_FILMS_FILM_ID_FK
        foreign key (FILM_ID) references FILMS (FILM_ID),
    constraint FILM_GENRE_GENRE_GENRE_ID_FK
        foreign key (GENRE_ID) references GENRE (GENRE_ID)
);

MERGE INTO GENRE (GENRE_ID, GENRE_NAME)
    VALUES (1, 'HORROR'),
           (2, 'DETECTIVE'),
           (3, 'CASUAL'),
           (4, 'CASUAL1'),
           (5, 'CASUAL2'),
           (6, 'CASUAL3');

MERGE INTO MPA_RATING (MPA_RATING_ID, MPA_RATING_NAME)
    VALUES (1, 'PG-13'),
           (2, 'PG-17'),
           (3, 'PG-21'),
           (4, 'PG-22'),
           (5, 'PG-23');