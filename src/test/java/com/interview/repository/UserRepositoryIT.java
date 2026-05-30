package com.interview.repository;

import com.interview.domain.entity.User;
import com.interview.exception.RepositoryException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRepositoryIT extends AbstractRepositoryIT {

    private final UserRepository userRepository = new UserRepository();

    @Test
    void saveShouldPersistUserAndGenerateId() {
        User user = buildUser("Ivan", "ivan@example.com", 30);

        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        Optional<User> persistedUser = userRepository.findById(savedUser.getId());
        assertTrue(persistedUser.isPresent());
        assertEquals("Ivan", persistedUser.get().getName());
        assertEquals("ivan@example.com", persistedUser.get().getEmail());
        assertEquals(30, persistedUser.get().getAge());
    }

    @Test
    void saveShouldThrowRepositoryExceptionForDuplicateEmail() {
        userRepository.save(buildUser("Ivan", "ivan@example.com", 30));

        RepositoryException exception = assertThrows(
                RepositoryException.class,
                () -> userRepository.save(buildUser("Petr", "ivan@example.com", 25))
        );

        assertEquals("Failed to save user", exception.getMessage());
    }

    @Test
    void findByIdShouldReturnUserWhenItExists() {
        User savedUser = userRepository.save(buildUser("Ivan", "ivan@example.com", 30));

        Optional<User> result = userRepository.findById(savedUser.getId());

        assertTrue(result.isPresent());
        assertEquals(savedUser.getId(), result.get().getId());
    }

    @Test
    void findByIdShouldReturnEmptyWhenUserDoesNotExist() {
        Optional<User> result = userRepository.findById(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllShouldReturnAllPersistedUsers() {
        userRepository.save(buildUser("Ivan", "ivan@example.com", 30));
        userRepository.save(buildUser("Petr", "petr@example.com", 25));

        List<User> users = userRepository.findAll();

        assertEquals(2, users.size());
    }

    @Test
    void updateShouldModifyExistingUser() {
        User savedUser = userRepository.save(buildUser("Ivan", "ivan@example.com", 30));

        Optional<User> updatedUser = userRepository.update(
                savedUser.getId(),
                "Ivan Ivanov",
                "ivan.ivanov@example.com",
                31
        );

        assertTrue(updatedUser.isPresent());
        assertEquals("Ivan Ivanov", updatedUser.get().getName());
        assertEquals("ivan.ivanov@example.com", updatedUser.get().getEmail());
        assertEquals(31, updatedUser.get().getAge());

        Optional<User> persistedUser = userRepository.findById(savedUser.getId());
        assertTrue(persistedUser.isPresent());
        assertEquals("Ivan Ivanov", persistedUser.get().getName());
    }

    @Test
    void updateShouldReturnEmptyWhenUserDoesNotExist() {
        Optional<User> result = userRepository.update(999L, "Ivan", "ivan@example.com", 30);

        assertTrue(result.isEmpty());
    }

    @Test
    void updateShouldThrowRepositoryExceptionForDuplicateEmail() {
        User firstUser = userRepository.save(buildUser("Ivan", "ivan@example.com", 30));
        userRepository.save(buildUser("Petr", "petr@example.com", 25));

        RepositoryException exception = assertThrows(
                RepositoryException.class,
                () -> userRepository.update(firstUser.getId(), "Ivan", "petr@example.com", 30)
        );

        assertEquals("Failed to update user", exception.getMessage());
    }

    @Test
    void deleteByIdShouldRemoveExistingUser() {
        User savedUser = userRepository.save(buildUser("Ivan", "ivan@example.com", 30));

        boolean deleted = userRepository.deleteById(savedUser.getId());

        assertTrue(deleted);
        assertTrue(userRepository.findById(savedUser.getId()).isEmpty());
    }

    @Test
    void deleteByIdShouldReturnFalseWhenUserDoesNotExist() {
        boolean deleted = userRepository.deleteById(999L);

        assertFalse(deleted);
    }

    private User buildUser(String name, String email, Integer age) {
        return User.builder()
                .name(name)
                .email(email)
                .age(age)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
