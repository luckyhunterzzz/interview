package com.interview.service;

import com.interview.domain.entity.User;
import com.interview.dto.CreateUserRequest;
import com.interview.dto.UpdateUserRequest;
import com.interview.dto.UserResponse;
import com.interview.exception.DuplicateEmailException;
import com.interview.exception.UserNotFoundException;
import com.interview.exception.ValidationException;
import com.interview.repository.UserRepository;
import com.interview.validator.UserValidator;
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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserValidator userValidator;

    @Mock
    private OutboxEventService outboxEventService;

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

        UserResponse result = userService.createUser(new CreateUserRequest("  Ivan  ", "  Ivan@Example.com  ", 30));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertEquals(savedUser.getId(), result.id());
        assertEquals(savedUser.getName(), result.name());
        assertEquals(savedUser.getEmail(), result.email());
        assertEquals(savedUser.getAge(), result.age());
        assertEquals("Ivan", capturedUser.getName());
        assertEquals("ivan@example.com", capturedUser.getEmail());
        assertEquals(30, capturedUser.getAge());
        assertNotNull(capturedUser.getCreatedAt());
        verify(outboxEventService).saveCreatedUserEvent("ivan@example.com");
    }

    @Test
    void createUserShouldThrowValidationExceptionForInvalidName() {
        doThrowValidationOnCreate();
        assertThrows(ValidationException.class, () -> userService.createUser(new CreateUserRequest("   ", "ivan@example.com", 30)));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserShouldThrowValidationExceptionForInvalidEmail() {
        doThrowValidationOnCreate();
        assertThrows(ValidationException.class, () -> userService.createUser(new CreateUserRequest("Ivan", "invalid-email", 30)));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserShouldThrowValidationExceptionForInvalidAge() {
        doThrowValidationOnCreate();
        assertThrows(ValidationException.class, () -> userService.createUser(new CreateUserRequest("Ivan", "ivan@example.com", 151)));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserShouldMapUniqueConstraintViolationToDuplicateEmailException() {
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Failed to save user"));

        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> userService.createUser(new CreateUserRequest("Ivan", "ivan@example.com", 30))
        );

        assertEquals("User with this email already exists", exception.getMessage());
        assertInstanceOf(DataIntegrityViolationException.class, exception.getCause());
        verify(outboxEventService, never()).saveCreatedUserEvent(any());
    }

    @Test
    void getUserByIdShouldReturnRepositoryResult() {
        User user = User.builder().id(1L).name("Ivan").email("ivan@example.com").age(30).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserById(1L);

        assertEquals(user.getId(), result.id());
        assertEquals(user.getName(), result.name());
    }

    @Test
    void getUserByIdShouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.getUserById(99L));

        assertEquals("User with id 99 not found", exception.getMessage());
    }

    @Test
    void getUserByIdShouldValidateId() {
        doThrowValidationOnId();
        assertThrows(ValidationException.class, () -> userService.getUserById(0L));
        verify(userRepository, never()).findById(any());
    }

    @Test
    void getAllUsersShouldReturnAllUsersFromRepository() {
        List<User> users = List.of(
                User.builder().id(1L).name("Ivan").email("ivan@example.com").age(30).build(),
                User.builder().id(2L).name("Petr").email("petr@example.com").age(25).build()
        );
        when(userRepository.findAll()).thenReturn(users);

        List<UserResponse> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("Ivan", result.get(0).name());
        assertEquals("Petr", result.get(1).name());
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

        UserResponse result = userService.updateUser(
                1L,
                new UpdateUserRequest("  Ivan Ivanov  ", "  Ivan.Ivanov@Example.com  ", 31)
        );

        assertEquals(updatedUser.getId(), result.id());
        assertEquals(updatedUser.getName(), result.name());
        assertEquals(updatedUser.getEmail(), result.email());
        assertEquals("Ivan Ivanov", existingUser.getName());
        assertEquals("ivan.ivanov@example.com", existingUser.getEmail());
        assertEquals(31, existingUser.getAge());
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUserShouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUser(99L, new UpdateUserRequest("Ivan", "ivan@example.com", 30))
        );

        assertEquals("User with id 99 not found", exception.getMessage());
    }

    @Test
    void updateUserShouldMapUniqueConstraintViolationToDuplicateEmailException() {
        User existingUser = User.builder()
                .id(1L)
                .name("Old")
                .email("old@example.com")
                .age(30)
                .createdAt(LocalDateTime.now())
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser))
                .thenThrow(new DataIntegrityViolationException("Failed to update user"));

        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> userService.updateUser(1L, new UpdateUserRequest("Ivan", "ivan@example.com", 30))
        );

        assertEquals("User with this email already exists", exception.getMessage());
    }

    @Test
    void updateUserShouldValidateId() {
        doThrowValidationOnId();
        assertThrows(ValidationException.class, () -> userService.updateUser(-1L, new UpdateUserRequest("Ivan", "ivan@example.com", 30)));
        verify(userRepository, never()).findById(any());
    }

    @Test
    void deleteUserShouldReturnTrueWhenRepositoryDeletesUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(
                User.builder()
                        .id(1L)
                        .name("Ivan")
                        .email("ivan@example.com")
                        .age(30)
                        .createdAt(LocalDateTime.now())
                        .build()
        ));
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
        verify(outboxEventService).saveDeletedUserEvent("ivan@example.com");
    }

    @Test
    void deleteUserShouldThrowUserNotFoundExceptionWhenRepositoryDoesNotDeleteUser() {
        when(userRepository.findById(42L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.deleteUser(42L));

        assertEquals("User with id 42 not found", exception.getMessage());
        verify(userRepository, never()).deleteById(any());
        verify(outboxEventService, never()).saveDeletedUserEvent(any());
    }

    @Test
    void deleteUserShouldValidateId() {
        doThrowValidationOnId();
        assertThrows(ValidationException.class, () -> userService.deleteUser(null));
        verify(userRepository, never()).deleteById(any());
        verify(outboxEventService, never()).saveDeletedUserEvent(any());
    }

    private void doThrowValidationOnCreate() {
        doThrow(new ValidationException("validation"))
                .when(userValidator)
                .validateUserData(any(), any(), any());
    }

    private void doThrowValidationOnId() {
        doThrow(new ValidationException("validation"))
                .when(userValidator)
                .validateId(any());
    }
}
