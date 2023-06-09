package ru.filmogram.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.filmogram.exceptions.ValidationException;
import ru.filmogram.model.User;
import ru.filmogram.services.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private final UserService userService;

    @GetMapping()
    public List<User> findAll() {
        return userService.findAll();
    }

    @PostMapping()
    public User create(@RequestBody User user)
            throws ValidationException {
        return userService.create(user);
    }

    @PutMapping()
    public User update(@RequestBody User user)
            throws ValidationException {
        return userService.update(user);
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable("id") Long id) {
        return userService.getIdUser(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public boolean addUserFriends(@PathVariable("id") Long id, @PathVariable("friendId") Long friendId) {
        return userService.addFriends(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteUserFriends(@PathVariable("id") Long id, @PathVariable("friendId") Long friendId)
            throws ValidationException {
        userService.deleteUserFriendsId(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getAllFriends(@PathVariable("id") Long id)
            throws ValidationException {
        return userService.allFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getAllСommonFriends(@PathVariable("id") Long id, @PathVariable("otherId") Long otherId)
            throws ValidationException {
        return userService.allCommonFriends(id, otherId);
    }
}
