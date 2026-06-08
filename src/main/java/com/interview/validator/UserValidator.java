package com.interview.validator;

import com.interview.exception.ValidationException;

import java.util.regex.Pattern;

public final class UserValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private UserValidator() {
    }

    public static void validateUserData(String name, String email, Integer age) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Name must not be empty");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email must not be empty");
        }

        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Email has invalid format");
        }

        if (age == null || age < 0 || age > 150) {
            throw new ValidationException("Age must be between 0 and 150");
        }
    }

    public static void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("Id must be a positive number");
        }
    }
}
