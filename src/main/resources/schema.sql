CREATE TABLE IF NOT EXISTS genre (
    genre_id INTEGER PRIMARY KEY,
    name VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS rating (
    rating_id INTEGER PRIMARY KEY,
    name VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS film (
    film_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    film_name VARCHAR(64) NOT NULL,
    description VARCHAR(200) NOT NULL,
    releaseDate DATE,
    duration INTEGER,
    rating_id INTEGER REFERENCES rating (rating_id),
    CONSTRAINT film_chk_releaseDate CHECK (releaseDate > '1985-12-28'),
    CONSTRAINT film_chk_duration CHECK (duration > 0)
);

CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_name VARCHAR(64) NOT NULL,
    email VARCHAR(64) NOT NULL,
    login VARCHAR(64) NOT NULL,
    birthday DATE NOT NULL,
    CONSTRAINT user_chk_birthday CHECK (birthday < CAST(now() AS DATE))
);

CREATE TABLE IF NOT EXISTS friends (
    friend_one_id INTEGER REFERENCES users (user_id),
    friend_two_id INTEGER REFERENCES users (user_id),
    status BOOLEAN DEFAULT 0 NOT NULL
);

CREATE TABLE IF NOT EXISTS likes (
    film_id INTEGER REFERENCES film (film_id),
    user_id INTEGER REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS genre_film (
    film_id INTEGER REFERENCES film (film_id),
    genre_id INTEGER REFERENCES genre (genre_id)
);