package controllers;

import exceptions.ValidationException;
import lombok.extern.slf4j.Slf4j;
import model.User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    HashMap<String, User> users = new HashMap<>();

    @GetMapping()
    public List<User> findAll() {
        return new ArrayList<User>(users.values());
    }

    @PostMapping()
    public User create(@RequestBody User user) throws ValidationException {
        User afterCheckUser = standardCheck(user);
        users.put(afterCheckUser.getEmail(), afterCheckUser);
        log.info("Добавлен пользователь: {}", afterCheckUser);
        return afterCheckUser;
    }

    @PutMapping()
    public User update(@RequestBody User user) throws ValidationException {
        User afterCheckUser = standardCheck(user);
        if (afterCheckUser.equals(users.get(afterCheckUser.getEmail()))) {
            users.put(afterCheckUser.getEmail(), afterCheckUser);
            log.info("В объект внесены изменения: {}", afterCheckUser);
            return afterCheckUser;
        }
        throw new ValidationException("Вызван endpount Put, но данный пользователь отсутствует");
    }

    private User standardCheck(User user) throws ValidationException {

        LocalDate today = LocalDate.now();

        if (user.getEmail().isEmpty() || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.debug("Неверно введен email: {}", user);
            throw new ValidationException("Неверно введен email");
        } else if (user.getLogin().isEmpty() || user.getLogin().isBlank()) {
            log.debug("Логин не может быть пустым и содержать пробелы: {}", user);
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        } else if (user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя пользователя изменено на lodin: {}", user);
        } else if (user.getBirthday().isAfter(today)) {
            log.debug("Дата рождения не может быть в будущем: {}", user);
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        return user;
    }
}
