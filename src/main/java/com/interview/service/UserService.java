package com.interview.service;

import com.interview.domain.entity.User;
import com.interview.exception.DuplicateEmailException;
import com.interview.exception.ValidationException;
import com.interview.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final UserRepository userRepository;

    @Transactional
    public User createUser(String name, String email, Integer age) {
        validateUserData(name, email, age);

        User user = User.builder()
                .name(name.trim())
                .email(email.trim().toLowerCase())
                .age(age)
                .createdAt(LocalDateTime.now())
                .build();

        try {
            User savedUser = userRepository.save(user);
            log.info("Created user with id {}", savedUser.getId());
            return savedUser;
        } catch (DataIntegrityViolationException e) {
            throw mapRepositoryException(e, "User with this email already exists");
        }
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        validateId(id);
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public Optional<User> updateUser(Long id,
                                     String name,
                                     String email,
                                     Integer age) {
        validateId(id);
        validateUserData(name, email, age);

        try {
            Optional<User> existingUser = userRepository.findById(id);

            if (existingUser.isEmpty()) {
                return Optional.empty();
            }

            User user = existingUser.get();
            user.setName(name.trim());
            user.setEmail(email.trim().toLowerCase());
            user.setAge(age);

            Optional<User> updatedUser = Optional.of(userRepository.save(user));
            updatedUser.ifPresent(savedUser -> log.info("Updated user with id {}", savedUser.getId()));
            return updatedUser;
        } catch (DataIntegrityViolationException e) {
            throw mapRepositoryException(e, "User with this email already exists");
        }
    }

    @Transactional
    public boolean deleteUser(Long id) {
        validateId(id);
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            log.info("Deleted user with id {}", id);
            return true;
        }
        return false;
    }

    private void validateUserData(String name, String email, Integer age) {
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

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("Id must be a positive number");
        }
    }

    private RuntimeException mapRepositoryException(DataIntegrityViolationException e, String duplicateEmailMessage) {
        if (isUniqueViolation(e)) {
            return new DuplicateEmailException(duplicateEmailMessage, e);
        }
        return e;
    }

    private boolean isUniqueViolation(Throwable throwable) {
        Throwable current = throwable;

        while (current != null) {
            if (current instanceof ConstraintViolationException constraintViolationException) {
                String sqlState = constraintViolationException.getSQLState();
                return "23505".equals(sqlState);
            }
            current = current.getCause();
        }

        return false;
    }
}
