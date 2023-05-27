package ru.filmogram.storage.user.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.User;
import ru.filmogram.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Repository
@Primary
public class UserDbStorageImpl implements UserStorage {

    @Autowired
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorageImpl(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    @Override
    public List<User> findAllUser() {
        ArrayList<User> users = new ArrayList<>();

        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT u.user_id,\n" +
                "u.name," +
                "u.email," +
                "u.login," +
                "u.birthday," +
                "FROM users AS u");
        while (userRows.next()) {
            User user = User.builder()
                    .name(userRows.getString("name"))
                    .email(userRows.getString("email"))
                    .login(userRows.getString("login"))
                    .birthday(LocalDate.parse(Objects.requireNonNull(userRows.getString("birthday"))))
                    .id(Long.valueOf(Objects.requireNonNull(userRows.getString("user_id"))))
                    .build();
            users.add(user);
        }
        return users;
    }

    @Override
    public User createUser(User user) throws ValidationException {
        jdbcTemplate.queryForRowSet("INSERT INTO users (" +
                "name," +
                "email," +
                "login," +
                "birthday)" +
                "VALUES ( ?, ?, ?, ?) DO NOTHING", user.getName(), user.getEmail(), user.getLogin(), user.getBirthday());
        return User.builder()
                .id(user.getId())
                .name(user.getName())
                .login(user.getLogin())
                .birthday(user.getBirthday())
                .build();
    }

    @Override
    public User updateUser(User user) throws ValidationException {
        jdbcTemplate.queryForRowSet(
                "INSERT INTO users (" +
                        "user_id,name, " +
                        "email, " +
                        "login, " +
                        "birthday) " +
                "VALUES ( ?, ?, ?, ?) " +
                        "ON CONFLICT (user_id) " +
                        "DO UPDATE SET name = EXCLUDED.name, " +
                        "email = EXCLUDED.email, " +
                        "login = EXCLUDED.login, " +
                        "birthday = EXCLUDED.birthday",
                user.getName(), user.getEmail(), user.getLogin(), user.getBirthday());
        return User.builder()
                .id(user.getId())
                .name(user.getName())
                .login(user.getLogin())
                .birthday(user.getBirthday())
                .build();
    }

    @Override
    public User getUserId(Long id) throws ValidationException {
        User user = null;
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE user_id = ?", id);
        if (id != null) {
            if (userRows.next()) {
                user = User.builder()
                        .name(userRows.getString("name"))
                        .email(userRows.getString("email"))
                        .login(userRows.getString("login"))
                        .birthday(LocalDate.parse(Objects.requireNonNull(userRows.getString("birthday"))))
                        .id(Long.valueOf((Objects.requireNonNull(userRows.getString("user_id")))))
                        .build();
            } else {
                throw new ValidationException("Вызван endpount Put, но данный пользователь отсутствует");
            }
        }
        return user;
    }

    @Override
    public User addFriend(Long id, Long friendId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("INSERT INTO friends (friend_one_id, friend_two_id, status) " +
                "VALUES ( ?, ?, ?)", id, friendId, 1);
        return User.builder()
                .id(id)
                .friends(Collections.singleton(friendId))
                .status(userRows.getBoolean(1))
                .build();
    }

    @Override
    public void deleteFriend(Long id, Long friendId) {
        jdbcTemplate.queryForRowSet( "delete from friends where friend_one_id = ?,AND friend_two_id = ?", id, friendId);
    }


    @Override
    public List<User> getFriends(Long id) {
        ArrayList<User> users = new ArrayList<>();

        SqlRowSet userRows = jdbcTemplate.queryForRowSet(
                "SELECT u.user_id," +
                    " u.name, " +
                    "u.email, " +
                    "u.login, " +
                    "f.status " +
                    "FROM users AS u " +
                    "LEFT JOIN friends AS f ON u.user_id = f.friend_wto_id " +
                    "LEFT JOIN friends AS f ON u.user_id = f.friend_one_id " +
                    "WHERE f.friend_one_id = ? " +
                    "OR f.friend_wto_id = ?", id);
        while (userRows.next()) {
            User user = User.builder()
                    .name(userRows.getString("name"))
                    .email(userRows.getString("email"))
                    .login(userRows.getString("login"))
                    .birthday(LocalDate.parse(Objects.requireNonNull(userRows.getString("birthday"))))
                    .id(Long.valueOf(Objects.requireNonNull(userRows.getString("user_id"))))
                    .build();
            users.add(user);
        }
        return users;
    }

    @Override
    public List<User> getCommonFriends(Long id, Long otherId) {
        List<User> listCommonFriends = new ArrayList<>();

        SqlRowSet userRows = jdbcTemplate.queryForRowSet(
                "SELECT u.user_id, " +
                        "u.name, " +
                        "u.email, " +
                        "u.login, " +
                        "FROM users AS u " +
                        "LEFT JOIN friends AS f ON u.user_id = f.friend_wto_id " +
                        "LEFT JOIN friends AS f ON u.user_id = f.friend_one_id " +
                        "WHERE f.status = 1 " +
                        "AND f.friend_one_id = ? " +
                        "OR f.friend_wto_id = ? " +
                        "INTERSECT" +
                        " SELECT u.user_id," +
                        " u.name," +
                        " u.email," +
                        " u.login," +
                        " FROM users AS u " +
                        "LEFT JOIN friends AS f ON u.user_id = f.friend_wto_id " +
                        "LEFT JOIN friends AS f ON u.user_id = f.friend_one_id " +
                        "WHERE f.status = 1" +
                        "AND f.friend_one_id = ? " +
                        "OR f.friend_wto_id = ?", id, otherId);

        while (userRows.next()) {
            User user = User.builder()
                    .name(userRows.getString("name"))
                    .email(userRows.getString("email"))
                    .login(userRows.getString("login"))
                    .birthday(LocalDate.parse(Objects.requireNonNull(userRows.getString("birthday"))))
                    .id(Long.valueOf(Objects.requireNonNull(userRows.getString("user_id"))))
                    .build();
            listCommonFriends.add(user);
        }
        return listCommonFriends;
    }
}
