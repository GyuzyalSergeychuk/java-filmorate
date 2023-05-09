package ru.filmogram.storage.user;

import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.User;

import java.util.List;

public interface UserStorage {
    public List<User> findAllUser();

    public User createUser(User user) throws ValidationException;

    public User updateUser(User user) throws ValidationException;

    public User getUserId(Long id);

    public User addFriend(Long id, Long friendId);

    public void deleteFriend(Long id, Long friendId);

    public List<User> getFriends(Long id);

    public List<User> getCommonFriends(Long id, Long otherId);

}
