package com.interview.controller;

import com.interview.dto.CreateUserRequest;
import com.interview.dto.UpdateUserRequest;
import com.interview.dto.UserResponse;
import com.interview.exception.DuplicateEmailException;
import com.interview.exception.ValidationException;
import com.interview.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@ActiveProfiles("unit")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void createUserShouldReturnCreatedDto() throws Exception {
        UserResponse user = buildUserResponse(1L, "Ivan", "ivan@example.com", 30);
        when(userService.createUser(new CreateUserRequest("Ivan", "ivan@example.com", 30))).thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ivan",
                                  "email": "ivan@example.com",
                                  "age": 30
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void getUserByIdShouldReturnUserDto() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.of(buildUserResponse(1L, "Ivan", "ivan@example.com", 30)));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"));
    }

    @Test
    void getUserByIdShouldReturnNotFound() throws Exception {
        when(userService.getUserById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllUsersShouldReturnDtoList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(
                buildUserResponse(1L, "Ivan", "ivan@example.com", 30),
                buildUserResponse(2L, "Petr", "petr@example.com", 25)
        ));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].name").value("Petr"));
    }

    @Test
    void updateUserShouldReturnUpdatedDto() throws Exception {
        when(userService.updateUser(1L, new UpdateUserRequest("Ivan Ivanov", "ivan.ivanov@example.com", 31)))
                .thenReturn(Optional.of(buildUserResponse(1L, "Ivan Ivanov", "ivan.ivanov@example.com", 31)));

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ivan Ivanov",
                                  "email": "ivan.ivanov@example.com",
                                  "age": 31
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ivan Ivanov"))
                .andExpect(jsonPath("$.email").value("ivan.ivanov@example.com"))
                .andExpect(jsonPath("$.age").value(31));
    }

    @Test
    void updateUserShouldReturnNotFound() throws Exception {
        when(userService.updateUser(99L, new UpdateUserRequest("Ivan", "ivan@example.com", 30)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ivan",
                                  "email": "ivan@example.com",
                                  "age": 30
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUserShouldReturnNoContent() throws Exception {
        when(userService.deleteUser(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUserShouldReturnNotFound() throws Exception {
        when(userService.deleteUser(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUserShouldReturnConflictForDuplicateEmail() throws Exception {
        when(userService.createUser(new CreateUserRequest("Ivan", "ivan@example.com", 30)))
                .thenThrow(new DuplicateEmailException("User with this email already exists", null));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ivan",
                                  "email": "ivan@example.com",
                                  "age": 30
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.messages[0]").value("User with this email already exists"));
    }

    @Test
    void createUserShouldReturnBadRequestForInvalidBody() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "email": "invalid-email",
                                  "age": 151
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages.length()").value(3));
    }

    @Test
    void getUserByIdShouldReturnBadRequestForInvalidId() throws Exception {
        when(userService.getUserById(0L)).thenThrow(new ValidationException("Id must be a positive number"));

        mockMvc.perform(get("/api/users/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").value("Id must be a positive number"));
    }

    private UserResponse buildUserResponse(Long id, String name, String email, Integer age) {
        return new UserResponse(id, name, email, age, LocalDateTime.of(2024, 1, 10, 12, 0));
    }
}
