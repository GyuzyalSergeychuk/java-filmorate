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
import java.util.List;
import java.util.Objects;

@Repository
@Primary
public class UserDbStorageImpl implements UserStorage {

    @Autowired
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorageImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    @Override
    public List<User> findAllUser() {
        ArrayList<User> users = new ArrayList<>();

        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT u.user_id,\n" +
                "       u.name,\n" +
                "       u.email,\n" +
                "       u.login\n" +
                "FROM user");
        while(userRows.next()) {
            User user = User.builder()
                    .name(userRows.getString("name"))
                    .email(userRows.getString("email"))
                    .login(userRows.getString("login"))
                    .birthday(LocalDate.parse(Objects.requireNonNull(userRows.getString("birthday"))))
                    .id(Long.valueOf(Objects.requireNonNull(userRows.getString("id"))))
                    .build();
            users.add(user);
        }
        var a = 0;
        return users;
    }

    @Override
    public User createUser(User user) throws ValidationException {
        return null;
    }

    @Override
    public User updateUser(User user) throws ValidationException {
        return null;
    }

    @Override
    public User getUserId(Long id) {
        return null;
    }

    @Override
    public User addFriend(Long id, Long friendId) {
        return null;
    }

    @Override
    public void deleteFriend(Long id, Long friendId) {

    }

    @Override
    public List<User> getFriends(Long id) {
        return null;
    }

    @Override
    public List<User> getCommonFriends(Long id, Long otherId) {
        return null;
    }
}
