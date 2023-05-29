package ru.filmogram.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.User;
import ru.filmogram.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageImplTest {

    User user;
    @Autowired
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    UserStorage userStorage;

    @BeforeEach
    public void beforeEach() {
        jdbcTemplate.execute("INSERT INTO users (" +
                "name," +
                "email," +
                "login," +
                "birthday)" +
                "VALUES ('Том', 'nnjh@come.ru', 'nnn', '1985-05-13')");
        jdbcTemplate.execute("INSERT INTO users (" +
                "name," +
                "email," +
                "login," +
                "birthday)" +
                "VALUES ('Том2', 'nnjh@come.2', 'nnn2', '1985-05-12')");
        jdbcTemplate.execute("INSERT INTO users (" +
                "name," +
                "email," +
                "login," +
                "birthday)" +
                "VALUES ('Том3', 'nnjh@come.3', 'nnn3', '1985-05-15')");
        jdbcTemplate.execute(
        "INSERT INTO friends(" +
                "friend_one_id, " +
                "friend_two_id, " +
                "status) " +
                "VALUES ('1', '2', true)");
    }

    @Test
    void findAllUser() {
        List<User> users = userStorage.findAllUser();

        assertEquals(1, users.get(0).getId());
        assertEquals("Том", users.get(0).getName());
    }

    @Test
    void createUser() throws ValidationException {
        User user = User.builder()
                .id(4L)
                .name("Том4")
                .email("nnjh@come.4")
                .login("oooo")
                .birthday(LocalDate.of(1997, 07, 05))
                .build();
        User actualUser = userStorage.createUser(user);

        assertEquals("Том4", actualUser.getName());
    }

    @Test
    void updateUser() throws ValidationException {
        user = User.builder()
                .id(4L)
                .name("Том44")
                .email("nnjh@come.44")
                .login("oooo")
                .birthday(LocalDate.of(1997, 07, 05))
                .build();
        User actualUser = userStorage.updateUser(user);

        assertEquals("Том44", user.getName());
    }

    @Test
    void getUserId() throws ValidationException {
        assertEquals("Том", userStorage.getUserId(1l).getName());
    }

    @Test
    void addFriend() {
        User user1 = User.builder()
                .id(4L)
                .name("Том44")
                .email("nnjh@come.44")
                .login("oooo")
                .birthday(LocalDate.of(1997, 07, 05))
                .status(false)
                .build();

        User user2 = User.builder()
                .id(5L)
                .name("Том45")
                .email("nnjh@come.5")
                .login("ooo5")
                .birthday(LocalDate.of(1995, 07, 05))
                .status(false)
                .build();

        userStorage.addFriend(user1.getId(), user2.getId());

        assertEquals(user2.getId(), 5);
    }

    @Test
    void deleteFriend() {
    }

    @Test
    void getFriends() {
    }

    @Test
    void getCommonFriends() {
    }
}