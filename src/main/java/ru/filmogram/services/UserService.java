package ru.filmogram.services;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.User;
import ru.filmogram.storage.user.UserStorage;

import java.util.List;

@Service
public class UserService {

    @Qualifier("userDbStorageImpl")
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> findAll() {
        return userStorage.findAllUser();
    }
    public User create(User user) throws ValidationException {
        return userStorage.createUser(user);
    }
    public User update(User user) throws ValidationException {
        return userStorage.updateUser(user);
    }

    public User getIdUser(Long id) throws ValidationException {
        return userStorage.getUserId(id);
    }

    public boolean addFriends(Long id, Long friendId) {
        return userStorage.addFriend(id, friendId);
    }

    public void deleteUserFriendsId(Long id, Long friendId) {
        userStorage.deleteFriend(id, friendId);
    }

    public List<User> allFriends(Long id) {
        return userStorage.getFriends(id);
    }

    public List<User> allCommonFriends(Long id, Long otherId) {
        return userStorage.getCommonFriends(id, otherId);
    }
}