package com.interview.console;

import com.interview.controller.UserController;
import com.interview.domain.entity.User;
import com.interview.exception.DuplicateEmailException;
import com.interview.exception.RepositoryException;
import com.interview.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Slf4j
public class UserConsole {

    private final UserController userController;
    private final Scanner scanner;

    public UserConsole(UserController userController) {
        this.userController = userController;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;

        try {
            while (running) {
                printMenu();

                String command = scanner.nextLine().trim();

                try {
                    switch (command) {
                        case "1" -> createUser();
                        case "2" -> findUserById();
                        case "3" -> findAllUsers();
                        case "4" -> updateUser();
                        case "5" -> deleteUser();
                        case "0" -> running = false;
                        default -> System.out.println("Unknown command");
                    }
                } catch (ValidationException | DuplicateEmailException e) {
                    System.out.println("Error: " + e.getMessage());
                } catch (RepositoryException e) {
                    log.error("Database operation failed", e);
                    System.out.println("Database error occurred. Please try again later.");
                } catch (Exception e) {
                    log.error("Unexpected console error", e);
                    System.out.println("Unexpected error occurred. Please try again.");
                }
            }
        } finally {
            scanner.close();
        }

        System.out.println("Application stopped");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("=== USER SERVICE ===");
        System.out.println("1 - Create user");
        System.out.println("2 - Find user by id");
        System.out.println("3 - Find all users");
        System.out.println("4 - Update user");
        System.out.println("5 - Delete user");
        System.out.println("0 - Exit");
        System.out.print("Enter command: ");
    }

    private void createUser() {
        String name = readRequiredString("Enter name: ");
        String email = readRequiredString("Enter email: ");
        Integer age = readInt("Enter age: ");

        User user = userController.createUser(name, email, age);
        System.out.println("User created: " + user);
    }

    private void findUserById() {
        Long id = readLong("Enter user id: ");
        Optional<User> optionalUser = userController.getUserById(id);

        if (optionalUser.isPresent()) {
            System.out.println(optionalUser.get());
        } else {
            System.out.println("User not found");
        }
    }

    private void findAllUsers() {
        List<User> users = userController.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("No users found");
            return;
        }

        users.forEach(System.out::println);
    }

    private void updateUser() {
        Long id = readLong("Enter user id: ");
        String name = readRequiredString("Enter new name: ");
        String email = readRequiredString("Enter new email: ");
        Integer age = readInt("Enter new age: ");

        Optional<User> updatedUser = userController.updateUser(id, name, email, age);

        if (updatedUser.isPresent()) {
            System.out.println("User updated: " + updatedUser.get());
        } else {
            System.out.println("User not found");
        }
    }

    private void deleteUser() {
        Long id = readLong("Enter user id: ");
        boolean deleted = userController.deleteUser(id);

        if (deleted) {
            System.out.println("User deleted");
        } else {
            System.out.println("User not found");
        }
    }

    private String readRequiredString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private Integer readInt(String prompt) {
        System.out.print(prompt);
        String value = scanner.nextLine().trim();

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ValidationException("Please enter a valid integer number");
        }
    }

    private Long readLong(String prompt) {
        System.out.print(prompt);
        String value = scanner.nextLine().trim();

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new ValidationException("Please enter a valid long number");
        }
    }
}
