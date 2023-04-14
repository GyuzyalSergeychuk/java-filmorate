package controllers;

import exceptions.ValidationException;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    User user;

    @BeforeEach
    public void beforeEach() throws ValidationException{
        user = new User();

    }
    @Test
    void findAll() {
    }

    @Test
    void create() {
    }

    @Test
    void update() {
    }
}