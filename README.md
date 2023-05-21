# java-filmorate
ER-diagram for Filmorate project

![ER-diagram](../java-filmorate/filmorate.jpg)

### Описание базы данных Filmorate

***film***

Содержит информацию о фильмах.

Таблица состоит из полей:

* первичный ключ **film_id** — идентификатор фильма;
* **name** — название фильма;
* **description** — описание фильма;
* **releaseDate** — дата выхода;
* **duration** — продолжительность фильма в минутах;
* внешний ключ **genre_id** (отсылает к таблице **genre**) — идентификатор жанра фильма;
* внешний ключ **rating_id** (отсылает к таблице **rating**) — идентификатор рейтинга фильма;

***friends***

Содержит информацию о друзьях пользователя.

Таблица включает поля:

* внешний ключ **friend_one_id** (отсылает к таблице **user**) — идентификатор пользователя;
* внешний ключ **friend_two_id** (отсылает к таблице **user**) — идентификатор пользователя;
* **status** — статус для связи «дружба» между двумя пользователями, например:

    * **false** — неподтверждённая — когда один пользователь отправил запрос на добавление другого пользователя в друзья;
    * **true** — подтверждённая — когда второй пользователь согласился на добавление.

***genre***

Содержит информацию о жанрах кино.

В таблицу входят поля:

* первичный ключ **genre_id** — идентификатор жанра;
* **name** — название жанра, например:

    * **comedy** — комедия;
    * **drama** — драма.

***like***

Содержит информацию о популярном кино.

Таблица состоит из полей:

* внешний ключ **film_id** (отсылает к таблице **film**) — идентификатор фильма;
* внешний ключ **user_id** (отсылает к таблице **user**) — идентификатор пользователя который оценил фильм;

***rating***

Содержит информацию о возрастном рейтинге.

В таблицу входят поля:

* первичный ключ **rating_id** — идентификатор рейтинга;
* **name** — возрастной рейтинг, например:
  * **G** — для всех возрастов.
  * **R** — не рекомендуется для детей младше 17 лет без сопровождения взрослых.

***user***

Содержит данные о пользователях.

Таблица включает поля:

* первичный ключ **user_id** — идентификатор пользователя;
* **name** — имя пользователя;
* **email** — электронная почта;
* **login** — идентификатор учётной записи пользователя;
* **birthday** — дата рождения пользователя.


**List<Film> findAll()**
```
SELECT f.film_id,
       f.name,
       f.description,
       f.releaseDate,
       g.genre,
       r.rating
       GROUP_CONCAT(l.user_id) AS listOfUsersLike
FROM film AS f
LEFT JOIN genre AS g ON f.genre_id = g.genre_id
LEFT JOIN rating AS r ON f.rating = r.rating
LEFT JOIN like AS l ON f.film_id = l.film_id;
```

**List<Film> allPopular()**
```
SELECT f.film_id,
       f.name,
       f.description,
       f.releaseDate,
       g.genre,
       r.rating
       GROUP_CONCAT(l.user_id) AS listOfUsersLike
       (SELECT COUNT (user_id) AS popular
       FROM like
       GROUP BY film_id
       ORDER BY popular DESC) AS like 
FROM film AS f
LEFT JOIN genre AS g ON f.genre_id = g.genre_id
LEFT JOIN rating AS r ON f.rating = r.rating
LEFT JOIN like AS l ON f.film_id = l.film_id;
```

**List<Film> sortPopularCountFilm()**
```
SELECT f.film_id,
       f.name,
       f.description,
       f.releaseDate,
       g.genre,
       r.rating
       GROUP_CONCAT(l.user_id) AS listOfUsersLike
       (SELECT COUNT (user_id) AS popular
       FROM like
       GROUP BY film_id
       ORDER BY popular DESC) AS like 
FROM film AS f
LEFT JOIN genre AS g ON f.genre_id = g.genre_id
LEFT JOIN rating AS r ON f.rating = r.rating
LEFT JOIN like AS l ON f.film_id = l.film_id
GROUP BY film_id
ORDER BY like DESC
LIMIT x;
```

**Film getFilm()**
```
SELECT f.film_id,
       f.name,
       f.description,
       f.releaseDate,
       g.genre,
       r.rating
       GROUP_CONCAT(l.user_id) AS listOfUsersLike
       (SELECT COUNT (user_id) AS popular
       FROM like
       GROUP BY film_id
       ORDER BY popular DESC) AS like 
FROM film AS f
LEFT JOIN genre AS g ON f.genre_id = g.genre_id
LEFT JOIN rating AS r ON f.rating = r.rating
LEFT JOIN like AS l ON f.film_id = l.film_id
WHERE f.film_id = x;
```

**List<User> userAll()**
```
SELECT u.user_id,
       u.name,
       u.email,
       u.login
FROM user 
```

**User getUser()**
```
SELECT u.user_id,
       u.name,
       u.email,
       u.login
FROM user AS u
WHERE u.user_id = x;
```

**List<User> getAllFriends()**
```
SELECT u.user_id,
       u.name,
       u.email,
       u.login,
       f.status
FROM user AS u
LEFT JOIN friends AS f ON u.user_id = f.friend_wto_id
LEFT JOIN friends AS f ON u.user_id = f.friend_one_id
WHERE f.friend_one_id = x 
      OR f.friend_wto_id = x;
```

**List<User> getAllСommonFriends()**
```
SELECT u.user_id,
       u.name,
       u.email,
       u.login,
FROM user AS u
LEFT JOIN friends AS f ON u.user_id = f.friend_wto_id
LEFT JOIN friends AS f ON u.user_id = f.friend_one_id
WHERE f.status = 'true'
      AND f.friend_one_id = x 
      OR f.friend_wto_id = x
INTERSECT    
SELECT u.user_id,
       u.name,
       u.email,
       u.login,
FROM user AS u
LEFT JOIN friends AS f ON u.user_id = f.friend_wto_id
LEFT JOIN friends AS f ON u.user_id = f.friend_one_id
WHERE f.status = 'true'
      AND f.friend_one_id = y 
      OR f.friend_wto_id = y;
```