package com.interview.service;

import com.interview.domain.entity.User;
import com.interview.dto.CreateUserRequest;
import com.interview.dto.UpdateUserRequest;
import com.interview.dto.UserResponse;
import com.interview.exception.DuplicateEmailException;
import com.interview.mapper.UserMapper;
import com.interview.repository.UserRepository;
import com.interview.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        UserValidator.validateUserData(request.name(), request.email(), request.age());

        User user = User.builder()
                .name(request.name().trim())
                .email(request.email().trim().toLowerCase())
                .age(request.age())
                .createdAt(LocalDateTime.now())
                .build();

        try {
            User savedUser = userRepository.save(user);
            log.info("Created user with id {}", savedUser.getId());
            return UserMapper.toResponse(savedUser);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEmailException("User with this email already exists", e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserById(Long id) {
        UserValidator.validateId(id);
        return userRepository.findById(id)
                .map(UserMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    @Transactional
    public Optional<UserResponse> updateUser(Long id, UpdateUserRequest request) {
        UserValidator.validateId(id);
        UserValidator.validateUserData(request.name(), request.email(), request.age());

        try {
            Optional<User> existingUser = userRepository.findById(id);

            if (existingUser.isEmpty()) {
                return Optional.empty();
            }

            User user = existingUser.get();
            user.setName(request.name().trim());
            user.setEmail(request.email().trim().toLowerCase());
            user.setAge(request.age());

            User updatedUser = userRepository.save(user);
            log.info("Updated user with id {}", updatedUser.getId());
            return Optional.of(UserMapper.toResponse(updatedUser));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEmailException("User with this email already exists", e);
        }
    }

    @Transactional
    public boolean deleteUser(Long id) {
        UserValidator.validateId(id);
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            log.info("Deleted user with id {}", id);
            return true;
        }
        return false;
    }
}
