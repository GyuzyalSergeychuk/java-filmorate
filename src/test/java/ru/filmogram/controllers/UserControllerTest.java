package ru.filmogram.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.filmogram.FilmApplication;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = FilmApplication.class, webEnvironment = DEFINED_PORT)
class UserControllerTest {
    User user;

    @Autowired
    UserController userController;

    @BeforeEach
    public void beforeEach() throws ValidationException {
        user = User.builder()
                .id(1L)
                .birthday(LocalDate.of(1985, 05, 13))
                .email("nnjh@come.ru")
                .login("nnn")
                .name("Ben")
                .build();
    }

    @Test
    void findAll() throws ValidationException {

        userController.create(user);
        // action
        List<User> userList = userController.findAll();

        // assert
        assertEquals(
                user.getEmail(),
                userList.get(0).getEmail(),
                "Проверка корректности работы findAll()");
    }

    @Test
    void create() throws ValidationException {
        // action
        User user1 = userController.create(user);

        // assert
        assertEquals(
                user.getLogin(),
                user1.getLogin(),
                "Сравнение login пользователя");
        assertEquals(
                user.getEmail(),
                user1.getEmail(),
                "Сравнение email пользователя");
    }

    @Test
    void update() throws ValidationException {
        userController.update(user);
        String name = "BEEEEEEEEEn";
        User expectedUser = User.builder()
                .id(1L)
                .birthday(LocalDate.of(1985, 05, 13))
                .email("nnjh@come.ru")
                .login("nnn")
                .name("BEEEEEEEEEn")
                .build();

        // action
        User user1 = userController.update(expectedUser);

        assertEquals(
                name,
                user1.getName(),
                "Проверка корректности работы update()");
    }

    @Test
    void updateInvalideEmail() throws ValidationException {
        userController.update(user);
        User expectedUser = User.builder()
                .id(1L)
                .birthday(LocalDate.of(1985, 05, 13))
                .email("nnjhcome.ru")
                .login("nnn")
                .name("BEEEEEEEEEn")
                .build();

        // action
        assertThrows(
                ValidationException.class,
                () -> userController.update(expectedUser),
                "Проверка исключения на правильность заполнения email");
    }

    @Test
    void updateInvalideBirthday() throws ValidationException {
        userController.update(user);
        User expectedUser = User.builder()
                .id(1L)
                .birthday(LocalDate.of(2024, 05, 13))
                .email("nnjh@come.ru")
                .login("nnn")
                .name("BEEEEEEEEEn")
                .build();

        // action
        assertThrows(
                ValidationException.class,
                () -> userController.update(expectedUser),
                "Проверка исключения на дату рождения");
    }

    @Test
    void updateInvalideLogin() throws ValidationException {
        userController.update(user);
        User expectedUser = User.builder()
                .id(1L)
                .birthday(LocalDate.of(1985, 05, 13))
                .email("nnjh@come.ru")
                .login("")
                .name("BEEEEEEEEEn")
                .build();

        // action
        assertThrows(
                ValidationException.class,
                () -> userController.update(expectedUser),
                "Проверка исключения на правильность заполнения логина");
    }

    @Test
    void updateInvalideName() throws ValidationException {
        userController.update(user);
        User expectedUser = User.builder()
                .id(1L)
                .birthday(LocalDate.of(1985, 05, 13))
                .email("nnjh@come.ru")
                .login("nnn")
                .name("")
                .build();

        User user1 = userController.update(expectedUser);

        // action
        assertEquals(
                expectedUser.getLogin(),
                user1.getName(),
                "Проверка заполнения поля имя пользователя логином при пустом поле имени");
    }
}