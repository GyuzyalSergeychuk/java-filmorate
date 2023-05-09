package ru.filmogram.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.filmogram.model.User;
import ru.filmogram.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private UserStorage userStorage;

    public User getIdUser(Long id){
        return userStorage.getUserId(id);
    }

    public User addFriends(Long id, Long friendId) {
        return userStorage.addFriend(id, friendId);
    }

    public void deleteUserFriendsId(Long id, Long friendId){
        userStorage.deleteFriend(id, friendId);
    }

    public List<User> allFriends(Long id) {
        return userStorage.getFriends(id);
    }

    public List<User> allCommonFriends(Long id, Long otherId)  {
        return userStorage.getCommonFriends(id, otherId);
    }
}