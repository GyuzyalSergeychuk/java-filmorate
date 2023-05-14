package ru.filmogram.storage.user;

import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.User;

import java.util.List;

public interface UserStorage {
    List<User> findAllUser();

    User createUser(User user) throws ValidationException;

    User updateUser(User user) throws ValidationException;

    User getUserId(Long id);

    User addFriend(Long id, Long friendId);

    void deleteFriend(Long id, Long friendId);

    List<User> getFriends(Long id);

    List<User> getCommonFriends(Long id, Long otherId);

}
