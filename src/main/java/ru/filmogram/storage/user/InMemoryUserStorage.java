package ru.filmogram.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.filmogram.exceptions.ObjectNotFoundException;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.User;
import ru.filmogram.services.UserService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    HashMap<Long, User> users = new HashMap<>();

    @Override
    public List<User> findAllUser() {
        return new ArrayList<User>(users.values());
    }

    @Override
    public User createUser(User user) throws ValidationException {
        User afterCheckUser = standardCheck(user);
        afterCheckUser.assignId();
        users.put(afterCheckUser.getId(), afterCheckUser);
        log.info("Добавлен пользователь: {}", afterCheckUser);
        return afterCheckUser;
    }

    @Override
    public User updateUser(User user) throws ValidationException {
        User afterCheckUser = standardCheck(user);
        if (Objects.equals(afterCheckUser.getId(), users.get(afterCheckUser.getId()).getId())) {
            users.put(afterCheckUser.getId(), afterCheckUser);
            log.info("В объект внесены изменения: {}", afterCheckUser);
            return afterCheckUser;
        }
        throw new ValidationException("Вызван endpount Put, но данный пользователь отсутствует");
    }

    @Override
    public User getUserId(Long id) {
        if(users.containsKey(id)) {
            return users.get(id);
        }
        throw new ObjectNotFoundException(String.format("Пользователь %d не найден", id));
    }

    @Override
    public User addFriend(Long id, Long friendId) {
        User user = users.get(id);
        user.addFriend(friendId);
        return user;
    }

    @Override
    public void deleteFriend(Long id, Long friendId) {
        users.get(id).deleteFriend(friendId);
    }

    @Override
    public List<User> getFriends(Long id) {
        List<User> listUserFriends = new ArrayList<>();

        for (Long friend : users.get(id).getFriends()) {
            listUserFriends.add(users.get(id));
        }
        return listUserFriends;
    }

    @Override
    public List<User> getCommonFriends(Long id, Long otherId) {
        List<User> listCommonFriends = new ArrayList<>();

        for (Long friend : users.get(id).getFriends()) {
            if (users.get(otherId).getFriends().contains(friend)) {
                listCommonFriends.add(users.get(friend));
            }
        }

//        if (listCommonFriends.size() == 0){
//            throw new ObjectNotFoundException("Общих знакомых не найдено.");
//        }
        return listCommonFriends;
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
