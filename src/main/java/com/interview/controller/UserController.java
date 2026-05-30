package com.interview.controller;

import com.interview.domain.entity.User;
import com.interview.service.UserService;

import java.util.List;
import java.util.Optional;

public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public User createUser(String name, String email, Integer age) {
        return userService.createUser(name, email, age);
    }

    public Optional<User> getUserById(Long id) {
        return userService.getUserById(id);
    }

    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    public Optional<User> updateUser(Long id, String name, String email, Integer age) {
        return userService.updateUser(id, name, email, age);
    }

    public boolean deleteUser(Long id) {
        return userService.deleteUser(id);
    }
}
