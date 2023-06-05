package ru.filmogram.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.User;
import ru.filmogram.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserDbStorageImplTest {

    @Autowired
    UserStorage userStorage;

    @Test
    void findAllUser() throws ValidationException {
        User user = User.builder()
                .name("Том4")
                .email("nnjh@come.4")
                .login("oooo")
                .birthday(LocalDate.of(1997, 07, 05))
                .build();
        userStorage.createUser(user);

        User user1 = User.builder()
                .name("Том14")
                .email("nnjh@come.1")
                .login("11111")
                .birthday(LocalDate.of(2001, 07, 05))
                .build();
        userStorage.createUser(user1);
        List<User> actualUsers = userStorage.findAllUser();

        assertEquals(2 , actualUsers.size());
        assertEquals(user1.getName(), actualUsers.get(1).getName());
        assertEquals(user.getLogin(), actualUsers.get(0).getLogin());
    }

    @Test
    void createUser() throws ValidationException {
        User user = User.builder()
                .name("Том4")
                .email("nnjh@come.4")
                .login("oooo")
                .birthday(LocalDate.of(1997, 07, 05))
                .build();
        User actualUser = userStorage.createUser(user);
        assertEquals(user.getName(), actualUser.getName());
    }

    @Test
    void updateUser() throws ValidationException {
        User user = User.builder()
                .name("Том14")
                .email("nnjh@come.1")
                .login("11111")
                .birthday(LocalDate.of(2001, 07, 05))
                .build();
        userStorage.createUser(user);
        User expectedUser = User.builder()
                .id(1L)
                .name("ТомНовый")
                .email("nnjh@come.новый")
                .login("новый")
                .birthday(LocalDate.of(2001, 07, 05))
                .build();
        User actualUser = userStorage.updateUser(expectedUser);

        assertEquals(1, actualUser.getId());
        assertEquals(expectedUser.getName(), actualUser.getName());
        assertEquals(expectedUser.getEmail(), actualUser.getEmail());
        assertEquals(expectedUser.getLogin(), actualUser.getLogin());
    }

    @Test
    void getUserId() throws ValidationException {
        User user = User.builder()
                .name("Том14")
                .email("nnjh@come.1")
                .login("11111")
                .birthday(LocalDate.of(2001, 07, 05))
                .build();
        userStorage.createUser(user);
        User user1 = User.builder()
                .name("Том2")
                .email("nnjh@come.2")
                .login("2222222")
                .birthday(LocalDate.of(2001, 07, 05))
                .build();
        userStorage.createUser(user1);

        assertEquals(user.getName(), userStorage.getUserId(1L).getName());
        assertEquals(user1.getName(), userStorage.getUserId(2L).getName());
        assertEquals(user1.getLogin(), userStorage.getUserId(2L).getLogin());
    }

    @Test
    void addFriend() throws ValidationException {
        User user = User.builder()
                .name("Том14")
                .email("nnjh@come.1")
                .login("11111")
                .birthday(LocalDate.of(2001, 07, 05))
                .build();
        User baseUser = userStorage.createUser(user);
        User friend1 = User.builder()
                .name("Том2")
                .email("nnjh@come.2")
                .login("2222222")
                .birthday(LocalDate.of(2001, 07, 05))
                .build();
        User baseFriend1 = userStorage.createUser(friend1);
        User friend2 = User.builder()
                .name("Том3")
                .email("nnjh@come.2")
                .login("223332")
                .birthday(LocalDate.of(2001, 07, 05))
                .build();
        User baseFriend2 = userStorage.createUser(friend2);

        //TODO тут вероятно надо объединить тест с getFriends  делать единые ассерты
        // а сам метод добавления в друзья я вроде починил
        assertEquals(2, 3);
    }
//
//    @Test
//    void deleteFriend() {
//    }
//
//    @Test
//    void getFriends() {
//    }
//
//    @Test
//    void getCommonFriends() {
//    }
}