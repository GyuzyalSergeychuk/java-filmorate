package ru.filmogram.controllers;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    HashMap<Integer, User> users = new HashMap<>();

    @GetMapping()
    public List<User> findAll() {
        return new ArrayList<User>(users.values());
    }

    @PostMapping()
    public User create(@RequestBody User user) throws ValidationException {
        User afterCheckUser = standardCheck(user);
        afterCheckUser.assignId();
        users.put(afterCheckUser.getId(), afterCheckUser);
        log.info("Добавлен пользователь: {}", afterCheckUser);
        return afterCheckUser;
    }

    @PutMapping()
    public User update(@RequestBody User user) throws ValidationException {
        User afterCheckUser = standardCheck(user);
        if (afterCheckUser.getId() == users.get(afterCheckUser.getId()).getId()) {
            users.put(afterCheckUser.getId(), afterCheckUser);
            log.info("В объект внесены изменения: {}", afterCheckUser);
            return afterCheckUser;
        }
        throw new ValidationException("Вызван endpount Put, но данный пользователь отсутствует");
    }

    private User standardCheck(User user) throws ValidationException {
        LocalDate today = LocalDate.now();

        if (user.getEmail().isEmpty() || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.error("Неверно введен email: {}", user);
            throw new ValidationException("Неверно введен email");
        }
        if (user.getLogin().isEmpty() || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("Логин не может быть пустым и содержать пробелы: {}", user);
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday().isAfter(today) ) {
            log.error("Дата рождения не может быть в будущем: {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
            log.error("Имя пользователя изменено на login: {}", user);
        }
        return user;
    }
}
