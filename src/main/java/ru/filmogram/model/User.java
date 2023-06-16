package ru.filmogram.model;

import lombok.Builder;
import lombok.Data;
import ru.filmogram.exceptions.ObjectNotFoundException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class User {
    private static Long nextId = 0L;
    private Long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    private Boolean status;
    private Set<Long> friends;

    public void assignId() {
        nextId++;
        id = nextId;
    }

    public void addFriend(Long friendId) {
        if (friends == null) {
            friends = new HashSet<>();
        }
        if (friendId < 0) {
            throw new ObjectNotFoundException("Пользователь не найден.");
        }
        friends.add(friendId);
    }

    public void deleteFriend(Long friendId) {
        friends.remove(friendId);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", login='" + login + '\'' +
                ", name='" + name + '\'' +
                ", birthday=" + birthday +
                ", status=" + status +
                ", friends=" + friends +
                '}';
    }
}
