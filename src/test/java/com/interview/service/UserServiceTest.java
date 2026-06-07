package com.interview.service;

import com.interview.domain.entity.User;
import com.interview.exception.DuplicateEmailException;
import com.interview.exception.ValidationException;
import com.interview.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUserShouldNormalizeInputAndDelegateToRepository() {
        User savedUser = User.builder()
                .id(1L)
                .name("Ivan")
                .email("ivan@example.com")
                .age(30)
                .createdAt(LocalDateTime.now())
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.createUser("  Ivan  ", "  Ivan@Example.com  ", 30);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertEquals(savedUser, result);
        assertEquals("Ivan", capturedUser.getName());
        assertEquals("ivan@example.com", capturedUser.getEmail());
        assertEquals(30, capturedUser.getAge());
        assertNotNull(capturedUser.getCreatedAt());
    }

    @Test
    void createUserShouldThrowValidationExceptionForInvalidName() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.createUser("   ", "ivan@example.com", 30)
        );

        assertEquals("Name must not be empty", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserShouldThrowValidationExceptionForInvalidEmail() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.createUser("Ivan", "invalid-email", 30)
        );

        assertEquals("Email has invalid format", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserShouldThrowValidationExceptionForInvalidAge() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.createUser("Ivan", "ivan@example.com", 151)
        );

        assertEquals("Age must be between 0 and 150", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserShouldMapUniqueConstraintViolationToDuplicateEmailException() {
        ConstraintViolationException constraintViolationException = org.mockito.Mockito.mock(ConstraintViolationException.class);
        when(constraintViolationException.getSQLState()).thenReturn("23505");
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Failed to save user", constraintViolationException));

        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> userService.createUser("Ivan", "ivan@example.com", 30)
        );

        assertEquals("User with this email already exists", exception.getMessage());
        assertInstanceOf(DataIntegrityViolationException.class, exception.getCause());
    }

    @Test
    void createUserShouldRethrowDataIntegrityViolationExceptionWhenItIsNotUniqueConstraintViolation() {
        DataIntegrityViolationException repositoryException =
                new DataIntegrityViolationException("Failed to save user", new RuntimeException("boom"));
        when(userRepository.save(any(User.class))).thenThrow(repositoryException);

        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> userService.createUser("Ivan", "ivan@example.com", 30)
        );

        assertSame(repositoryException, exception);
    }

    @Test
    void getUserByIdShouldReturnRepositoryResult() {
        User user = User.builder().id(1L).name("Ivan").email("ivan@example.com").age(30).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void getUserByIdShouldValidateId() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.getUserById(0L)
        );

        assertEquals("Id must be a positive number", exception.getMessage());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void getAllUsersShouldReturnAllUsersFromRepository() {
        List<User> users = List.of(
                User.builder().id(1L).name("Ivan").email("ivan@example.com").age(30).build(),
                User.builder().id(2L).name("Petr").email("petr@example.com").age(25).build()
        );
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(users, result);
    }

    @Test
    void updateUserShouldNormalizeInputAndReturnUpdatedUser() {
        User existingUser = User.builder()
                .id(1L)
                .name("Ivan")
                .email("ivan@example.com")
                .age(30)
                .createdAt(LocalDateTime.now())
                .build();
        User updatedUser = User.builder()
                .id(1L)
                .name("Ivan Ivanov")
                .email("ivan.ivanov@example.com")
                .age(31)
                .createdAt(existingUser.getCreatedAt())
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(updatedUser);

        Optional<User> result = userService.updateUser(1L, "  Ivan Ivanov  ", "  Ivan.Ivanov@Example.com  ", 31);

        assertTrue(result.isPresent());
        assertEquals(updatedUser, result.get());
        assertEquals("Ivan Ivanov", existingUser.getName());
        assertEquals("ivan.ivanov@example.com", existingUser.getEmail());
        assertEquals(31, existingUser.getAge());
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUserShouldReturnEmptyWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<User> result = userService.updateUser(99L, "Ivan", "ivan@example.com", 30);

        assertTrue(result.isEmpty());
    }

    @Test
    void updateUserShouldMapUniqueConstraintViolationToDuplicateEmailException() {
        ConstraintViolationException constraintViolationException = org.mockito.Mockito.mock(ConstraintViolationException.class);
        when(constraintViolationException.getSQLState()).thenReturn("23505");
        User existingUser = User.builder()
                .id(1L)
                .name("Old")
                .email("old@example.com")
                .age(30)
                .createdAt(LocalDateTime.now())
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser))
                .thenThrow(new DataIntegrityViolationException("Failed to update user", constraintViolationException));

        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> userService.updateUser(1L, "Ivan", "ivan@example.com", 30)
        );

        assertEquals("User with this email already exists", exception.getMessage());
    }

    @Test
    void updateUserShouldValidateId() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.updateUser(-1L, "Ivan", "ivan@example.com", 30)
        );

        assertEquals("Id must be a positive number", exception.getMessage());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void deleteUserShouldReturnTrueWhenRepositoryDeletesUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        boolean result = userService.deleteUser(1L);

        assertTrue(result);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUserShouldReturnFalseWhenRepositoryDoesNotDeleteUser() {
        when(userRepository.existsById(42L)).thenReturn(false);

        boolean result = userService.deleteUser(42L);

        assertFalse(result);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void deleteUserShouldValidateId() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.deleteUser(null)
        );

        assertEquals("Id must be a positive number", exception.getMessage());
        verify(userRepository, never()).deleteById(any());
    }
}
