package ru.filmogram.controllers;

import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    User user;
    @InjectMocks
    UserController userController;

    @BeforeEach
    public void beforeEach() throws ValidationException{
        user = User.builder()
                .id(1)
                .birthday(LocalDate.of(1985,05,13))
                .email("nnjh@come.ru")
                .login("nnn")
                .name("Ben")
                .build();
    }

    @Test
    void findAll() {
        userController.users.put(user.getEmail(), user);
        // action
        List<User> userList = userController.findAll();

        // assert
        assertEquals(user.getEmail(), userList.get(0).getEmail());
    }

    @Test
    void create() throws ValidationException {
        // action
        User user1 = userController.create(user);

        // assert
        assertEquals(user.getLogin(), user1.getLogin());
        assertEquals(user.getEmail(), user1.getEmail());
    }

    @Test
    void update() throws ValidationException {
        userController.users.put(user.getEmail(), user);
        String name = "BEEEEEEEEEn";
        User expectedUser = User.builder()
                .id(1)
                .birthday(LocalDate.of(1985,05,13))
                .email("nnjh@come.ru")
                .login("nnn")
                .name("BEEEEEEEEEn")
                .build();

        // action
        User user1 = userController.update(expectedUser);

        assertEquals(name, user1.getName());
    }

    @Test
    void updateInvalideEmail() throws ValidationException {
        userController.users.put(user.getEmail(), user);
        User expectedUser = User.builder()
                .id(1)
                .birthday(LocalDate.of(1985,05,13))
                .email("nnjhcome.ru")
                .login("nnn")
                .name("BEEEEEEEEEn")
                .build();

        // action
        assertThrows(ValidationException.class,
                () -> userController.update(expectedUser),
                "Неверно введен email");
    }

    @Test
    void updateInvalideBirthday() throws ValidationException {
        userController.users.put(user.getEmail(), user);
        User expectedUser = User.builder()
                .id(1)
                .birthday(LocalDate.of(2024,05,13))
                .email("nnjh@come.ru")
                .login("nnn")
                .name("BEEEEEEEEEn")
                .build();

        // action
        assertThrows(ValidationException.class,
                () -> userController.update(expectedUser),
                "Дата рождения не может быть в будущем");
    }

    @Test
    void updateInvalideLogin() throws ValidationException {
        userController.users.put(user.getEmail(), user);
        User expectedUser = User.builder()
                .id(1)
                .birthday(LocalDate.of(1985, 05, 13))
                .email("nnjh@come.ru")
                .login("")
                .name("BEEEEEEEEEn")
                .build();

        // action
        assertThrows(ValidationException.class,
                () -> userController.update(expectedUser),
                "Логин не может быть пустым и содержать пробелы");
    }

    @Test
    void updateInvalideName() throws ValidationException {
        userController.users.put(user.getEmail(), user);
        User expectedUser = User.builder()
                .id(1)
                .birthday(LocalDate.of(1985, 05, 13))
                .email("nnjh@come.ru")
                .login("nnn")
                .name("")
                .build();

        // action
        assertThrows(ValidationException.class,
                () -> userController.update(expectedUser),
                "Имя пользователя не может быть пустым");
    }
}