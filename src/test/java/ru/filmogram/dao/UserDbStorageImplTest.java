package ru.filmogram.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
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

    //убрать
    User user;
    //TODO добавить чистку базы

    @Autowired
    UserStorage userStorage;

    @Test
    void findAllUser() throws ValidationException {
        user = User.builder()
                .name("Том4")
                .email("nnjh@come.4")
                .login("oooo")
                .birthday(LocalDate.of(1997, 07, 05))
                .build();
        userStorage.createUser(user);

        user = User.builder()
                .name("Том14")
                .email("nnjh@come.1")
                .login("11111")
                .birthday(LocalDate.of(2001, 07, 05))
                .build();
        userStorage.createUser(user);
        List<User> users = userStorage.findAllUser();

        assertEquals(1, users.get(0).getId());
        assertEquals("Том4", users.get(0).getName());
        assertEquals(2, users.get(1).getId());
    }

    @Test
    void createUser() throws ValidationException {
        user = User.builder()
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
                .name("Том14")
                .email("nnjh@come.1")
                .login("11111")
                .birthday(LocalDate.of(2001, 07, 05))
                .build();
        userStorage.createUser(user);
        user = User.builder()
                .id(1l)
                .name("ТомНовый")
                .email("nnjh@come.новый")
                .login("новый")
                .birthday(LocalDate.of(2001, 07, 05))
                .build();
        User actualUser = userStorage.updateUser(user);

        assertEquals(1, actualUser.getId());
        assertEquals("ТомНовый", actualUser.getName());
        assertEquals("nnjh@come.новый", actualUser.getEmail());
        assertEquals("новый", actualUser.getLogin());
    }

    @Test
    void getUserId() throws ValidationException {
        user = User.builder()
                .name("Том14")
                .email("nnjh@come.1")
                .login("11111")
                .birthday(LocalDate.of(2001, 07, 05))
                .build();
        userStorage.createUser(user);
        user = User.builder()
                .name("Том2")
                .email("nnjh@come.2")
                .login("2222222")
                .birthday(LocalDate.of(2001, 07, 05))
                .build();
        userStorage.createUser(user);

        assertEquals("Том14", userStorage.getUserId(1l).getName());
        assertEquals("Том2", userStorage.getUserId(2l).getName());
        assertEquals("2222222", userStorage.getUserId(2l).getLogin());
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