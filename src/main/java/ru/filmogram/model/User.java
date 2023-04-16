package ru.filmogram.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class User {
    private static int nextId = 0;
    private int id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;

    public void assignId() {
        nextId++;
        id = nextId;
    }
}
