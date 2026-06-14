package com.interview.service;

import com.interview.domain.entity.User;
import com.interview.dto.CreateUserRequest;
import com.interview.dto.UpdateUserRequest;
import com.interview.dto.UserResponse;
import com.interview.event.UserEventProducer;
import com.interview.exception.DuplicateEmailException;
import com.interview.exception.UserNotFoundException;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final UserEventProducer userEventProducer;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        userValidator.validateUserData(request.name(), request.email(), request.age());

        User user = User.builder()
                .name(request.name().trim())
                .email(request.email().trim().toLowerCase())
                .age(request.age())
                .createdAt(LocalDateTime.now())
                .build();

        try {
            User savedUser = userRepository.save(user);
            userEventProducer.publishCreated(savedUser.getEmail());
            log.info("Created user with id {}", savedUser.getId());
            return UserMapper.toResponse(savedUser);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEmailException("User with this email already exists", exception);
        }
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        userValidator.validateId(id);
        return userRepository.findById(id)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(UserMapper::toResponse).toList();
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        userValidator.validateId(id);
        userValidator.validateUserData(request.name(), request.email(), request.age());

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setName(request.name().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setAge(request.age());

        try {
            User updatedUser = userRepository.save(user);
            log.info("Updated user with id {}", updatedUser.getId());
            return UserMapper.toResponse(updatedUser);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEmailException("User with this email already exists", exception);
        }
    }

    @Transactional
    public void deleteUser(Long id) {
        userValidator.validateId(id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        userRepository.deleteById(id);
        userEventProducer.publishDeleted(user.getEmail());
        log.info("Deleted user with id {}", id);
    }
}
