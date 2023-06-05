package ru.filmogram.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.mapper.UserMapper;
import ru.filmogram.model.User;
import ru.filmogram.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;

@Repository
@Primary
public class UserDbStorageImpl implements UserStorage {

    private Logger log = LoggerFactory.getLogger(getClass());

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
                "u.user_name," +
                "u.email," +
                "u.login," +
                "u.birthday " +
                "FROM users AS u");
        while (userRows.next()) {
            User user = User.builder()
                    .id(Long.valueOf(Objects.requireNonNull(userRows.getString("user_id"))))
                    .name(userRows.getString("user_name"))
                    .email(userRows.getString("email"))
                    .login(userRows.getString("login"))
                    .birthday(LocalDate.parse(Objects.requireNonNull(userRows.getString("birthday"))))
                    .build();
            users.add(user);
        }
        return users;
    }

    @Override
    public User createUser(User user) throws ValidationException {

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("user_name", user.getName());
        parameters.put("email", user.getEmail());
        parameters.put("login", user.getLogin());
        parameters.put("birthday", user.getBirthday());

        Long lastUserId = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();

        String query = "SELECT user_id," +
                "              user_name," +
                "              email," +
                "              login," +
                "              birthday" +
                "              FROM users" +
                "              WHERE user_id = ?";
        User finalUser = jdbcTemplate.queryForObject(
                query, new Object[]{lastUserId}, new UserMapper());
        log.info("Пользователь успешно добавлен");
        return finalUser;
    }

    @Override
    public User updateUser(User user) throws ValidationException {

        Integer userId = jdbcTemplate.queryForRowSet(
                "SELECT user_id FROM users WHERE user_id = ? ",
                user.getId()).getRow();

        if (userId == 0) {
            log.info("Пользователь {} не существует", userId);
            return user;
        } else if (userId > 0) {
            jdbcTemplate.update(
                    "UPDATE user SET " +
                            "name = ?, " +
                            "email = ?, " +
                            "login = ?, " +
                            "birthday = ? " +
                            "WHERE id = ?",
                    user.getName(), user.getEmail(), user.getLogin(), user.getBirthday(), user.getId());
            return user;
        } else if (userId < 0) {
            log.info("Невкрно указан {} id пользователя", userId);
            return null;
        }
        return user;
    }

    @Override
    public User getUserId(Long id) throws ValidationException {

        List<Integer> userId = jdbcTemplate.queryForObject(
                "SELECT user_id FROM users WHERE user_id = ? ",
                List.class,
                id);

        if (userId.get(0) == null) {
            log.info("Данный {} пользователь не найден", userId);
            return null;
        }
        String query = "SELECT user_id," +
                "              user_name," +
                "              email," +
                "              login," +
                "              birthday" +
                "              FROM users" +
                "              WHERE user_id = ?";
        User finalUser = jdbcTemplate.queryForObject(
                query, new Object[]{userId.get(0)}, new UserMapper());
        return finalUser;
    }

    @Override
    public boolean addFriend(Long userId, Long friendId) {

        // проверяем есть ли такая запись
        Integer checkForFriend1 = jdbcTemplate.queryForObject(
                "SELECT COUNT(friend_one_id) FROM friends WHERE friend_one_id = ? AND friend_two_id = ? ",
                Integer.class,
                userId, friendId);

        // если есть, значит запрос уже был направлен
        if (checkForFriend1 > 0) {
            log.info("Вы уже направили запрос в друзья между пользователями {} и {}", userId, friendId);
            return false;
        }

        // проверяем обратную запись, не кидал ли  друг уже заявку в друзья нашему юзеру
        Integer checkForFriend2 = jdbcTemplate.queryForObject(
                "SELECT COUNT(friend_two_id) FROM friends WHERE friend_two_id = ? AND friend_one_id = ? ",
                Integer.class,
                userId, friendId);

        // условие что второй юзер уже отправил ему заявку в друзья то проверяем ее статус
        if(checkForFriend2 > 0) {
            boolean checkStatus = Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                    "SELECT status FROM friends WHERE friend_two_id = ? AND friend_one_id = ? ",
                    Boolean.class,
                    userId, friendId));
            // если статус true то ничего не делаем так как они уже друзья
            if(checkStatus) {
                log.info("Пользователи уже друзья: {} и {}", userId, friendId);
                return false;
            // а если статут false то меняем статус на true
            } else {
                jdbcTemplate.update(
                        "" +
                                "UPDATE friends SET " +
                                "status = ? " +
                                "WHERE " +
                                "friend_one_id = ? " +
                                "AND " +
                                "friend_two_id = ?",
                                true, friendId, userId);
                return true;
            }
        }
        //если везде все чисто и никаких записей нет, мы просто создаем запись (статут по дефолту встанет false)
        else  {
            jdbcTemplate.update(
                    "INSERT INTO friends(" +
                            "friend_one_id, " +
                            "friend_two_id) " +
                            "VALUES (?, ?)", userId, friendId);
            return true;
        }
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        jdbcTemplate.queryForRowSet("delete from friends where friend_one_id = ?,AND friend_two_id = ?", userId, friendId);
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
