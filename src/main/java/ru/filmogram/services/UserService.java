package ru.filmogram.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.filmogram.exceptions.ObjectNotFoundException;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.User;
import ru.filmogram.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Service
public class UserService {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Qualifier("userDbStorageImpl")
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> findAll() {
        return userStorage.findAllUser();
    }

    public User create(User user) throws ValidationException {
        User afterCheckUser = standardCheck(user);
        return userStorage.createUser(afterCheckUser);
    }

    public User update(User user) throws ValidationException {
        User afterCheckUser = standardCheck(user);
        return userStorage.updateUser(afterCheckUser);
    }

    public User getIdUser(Long id) {
        return userStorage.getUserId(id);
    }

    public boolean addFriends(Long id, Long friendId) {
        if (id < 0) {
            throw new ObjectNotFoundException(
                    String.format("id пользователя {} не может быть отрицательным", id));
        }
        if (friendId < 0) {
            throw new ObjectNotFoundException(
                    String.format("friendId пользователя {} не может быть отрицательным", friendId));
        }
        return userStorage.addFriend(id, friendId);
    }

    public void deleteUserFriendsId(Long id, Long friendId) throws ValidationException {
        if (id <= 0) {
            throw new ValidationException(
                    String.format("id пользователя {} не может быть отрицательным", id));
        }
        if (friendId <= 0) {
            throw new ValidationException(
                    String.format("friendId пользователя {} не может быть отрицательным", friendId));
        }
        userStorage.deleteFriend(id, friendId);
    }

    public List<User> allFriends(Long id) throws ValidationException {
        if (id <= 0) {
            throw new ValidationException(String.format("Пользователь {} не найден", id));
        }
        return userStorage.getFriends(id);
    }

    public List<User> allCommonFriends(Long id, Long otherId) throws ValidationException {
        if (id <= 0) {
            throw new ValidationException(
                    String.format("id пользователя {} не может быть отрицательным", id));
        }
        if (otherId <= 0) {
            throw new ValidationException(
                    String.format("otherId пользователя {} не может быть отрицательным", otherId));
        }
        return userStorage.getCommonFriends(id, otherId);
    }

    private User standardCheck(User user) throws ValidationException {
        if (user.getEmail().isEmpty() || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.error("Неверно введен email: {}", user);
            throw new ValidationException("Неверно введен email");
        }
        if (user.getLogin().isEmpty() || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("Логин не может быть пустым и содержать пробелы: {}", user);
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }

        var today = LocalDate.now();
        if (user.getBirthday().isAfter(today)) {
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