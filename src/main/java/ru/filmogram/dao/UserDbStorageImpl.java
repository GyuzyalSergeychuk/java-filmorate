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
import ru.filmogram.exceptions.ObjectNotFoundException;
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
    public User createUser(User user) {

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
    public User updateUser(User user) throws ObjectNotFoundException {

        List<Integer> userId = jdbcTemplate.queryForList(
                "SELECT user_id FROM users WHERE user_id = ? ",
                Integer.class, user.getId());

        if (userId.size() == 0) {
            log.info("Пользователь {} не существует", userId);
            throw new ObjectNotFoundException("{} пользователь не найден");
        } else if (userId.get(0) > 0) {
            jdbcTemplate.update(
                    "UPDATE users SET " +
                            "user_name = ?, " +
                            "email = ?, " +
                            "login = ?, " +
                            "birthday = ? " +
                            "WHERE user_id = ?",
                    user.getName(), user.getEmail(), user.getLogin(), user.getBirthday(), user.getId());
            return user;
        } else if (userId.get(0) < 0) {
            log.info("Неверно указан {} id пользователя", userId.get(0));
            return null;
        }
        return user;
    }

    @Override
    public User getUserId(Long id) throws ObjectNotFoundException {

        List<Integer> userId = jdbcTemplate.queryForList(
                "SELECT user_id FROM users WHERE user_id = ? ",
                Integer.class, id);

        if (userId.size() == 0) {
            log.info("Данный {} пользователь не найден", userId);
            throw new ObjectNotFoundException("id пользователя {} не существует");
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

        // проверяем обратную запись, не кидал ли друг уже заявку в друзья нашему юзеру
        Integer checkForFriend2 = jdbcTemplate.queryForObject(
                "SELECT COUNT(friend_two_id) FROM friends WHERE friend_two_id = ? AND friend_one_id = ? ",
                Integer.class,
                userId, friendId);

        // условие, что второй юзер уже отправил ему заявку в друзья то проверяем ее статус
        if (checkForFriend2 > 0) {
            boolean checkStatus = Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                    "SELECT status FROM friends WHERE friend_two_id = ? AND friend_one_id = ? ",
                    Boolean.class,
                    userId, friendId));
            // если статус true, то ничего не делаем так как они уже друзья
            if (checkStatus) {
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
        else {
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
        jdbcTemplate.update("delete from friends where friend_one_id = ? AND friend_two_id = ?",
                userId, friendId);
        jdbcTemplate.update("delete from friends where friend_two_id = ? AND friend_one_id = ? AND status = true",
                userId, friendId);
    }

    @Override
    public List<User> getFriends(Long id) {

        ArrayList<User> users = new ArrayList<>();

        List<Integer> userId = jdbcTemplate.queryForList(
                "SELECT user_id FROM users WHERE user_id = ? ",
                Integer.class, id);

        if (userId.get(0) != null) {
            SqlRowSet rs = jdbcTemplate.queryForRowSet(
                    "SELECT u.user_id, " +
                            "u.user_name, " +
                            "u.email, " +
                            "u.login, " +
                            "u.birthday " +
                            "FROM users AS u " +
                            "JOIN friends AS f ON u.user_id = f.friend_two_id " +
                            "WHERE f.friend_one_id = ?"
                    , userId.get(0));
            while (rs.next()) {
                User user = User.builder()
                        .id(rs.getLong("user_id"))
                        .name(rs.getString("user_name"))
                        .email(rs.getString("email"))
                        .login(rs.getString("login"))
                        .birthday(LocalDate.parse(rs.getString("birthday")))
                        .build();
                users.add(user);
            }

            SqlRowSet rs1 = jdbcTemplate.queryForRowSet(
                    "SELECT u.user_id, " +
                            "u.user_name, " +
                            "u.email, " +
                            "u.login, " +
                            "u.birthday " +
                            "FROM users AS u " +
                            "JOIN friends AS f ON u.user_id = f.friend_one_id " +
                            "WHERE f.friend_two_id = ? AND status = true "
                    , userId.get(0));
            while (rs1.next()) {
                User user1 = User.builder()
                        .id(rs1.getLong("user_id"))
                        .name(rs1.getString("user_name"))
                        .email(rs1.getString("email"))
                        .login(rs1.getString("login"))
                        .birthday(LocalDate.parse(rs1.getString("birthday")))
                        .build();
                users.add(user1);
            }
        } else if (userId.get(0) < 0) {
            log.info("id {} пользователя не может быть отрицательным", id);
        } else {
            log.info("Пользователя {} не существует", id);
        }
        return users;
    }

    @Override
    public List<User> getCommonFriends(Long id, Long otherId) {
        List<User> friendOne = new ArrayList<>();
        List<User> friendTwo = new ArrayList<>();
        List<User> commonFriends = new ArrayList<>();

        if (id > 0) {
            friendOne = getFriends(id);
        }

        if (otherId > 0) {
            friendTwo = getFriends(otherId);
        }

        if (id <= 0 || otherId <= 0) {
            log.info("Пользователь не существует");
        }

        for (User user : friendTwo) {
            if (friendOne.contains(user)) {
                commonFriends.add(user);
            }
        }
        return commonFriends;
    }
}
