package com.interview.controller;

import com.interview.dto.CreateUserRequest;
import com.interview.dto.UpdateUserRequest;
import com.interview.dto.UserResponse;
import com.interview.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserHateoasController.class)
@Import(UserModelAssembler.class)
@ActiveProfiles("unit")
class UserHateoasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void getUserByIdShouldReturnUserWithLinks() throws Exception {
        when(userService.getUserById(1L)).thenReturn(buildUserResponse(1L, "Ivan", "ivan@example.com", 30));

        mockMvc.perform(get("/api/v2/users/1").accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/api/v2/users/1"))
                .andExpect(jsonPath("$._links.users.href").value("http://localhost/api/v2/users"));
    }

    @Test
    void getAllUsersShouldReturnCollectionWithLinks() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(
                buildUserResponse(1L, "Ivan", "ivan@example.com", 30),
                buildUserResponse(2L, "Petr", "petr@example.com", 25)
        ));

        mockMvc.perform(get("/api/v2/users").accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.users.length()").value(2))
                .andExpect(jsonPath("$._embedded.users[0]._links.self.href").value("http://localhost/api/v2/users/1"))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/api/v2/users"));
    }

    @Test
    void createUserShouldReturnCreatedResourceWithLinks() throws Exception {
        when(userService.createUser(new CreateUserRequest("Ivan", "ivan@example.com", 30)))
                .thenReturn(buildUserResponse(1L, "Ivan", "ivan@example.com", 30));

        mockMvc.perform(post("/api/v2/users")
                        .accept(MediaTypes.HAL_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ivan",
                                  "email": "ivan@example.com",
                                  "age": 30
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/api/v2/users/1"));
    }

    @Test
    void updateUserShouldReturnUpdatedResourceWithLinks() throws Exception {
        when(userService.updateUser(1L, new UpdateUserRequest("Ivan Ivanov", "ivan.ivanov@example.com", 31)))
                .thenReturn(buildUserResponse(1L, "Ivan Ivanov", "ivan.ivanov@example.com", 31));

        mockMvc.perform(put("/api/v2/users/1")
                        .accept(MediaTypes.HAL_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ivan Ivanov",
                                  "email": "ivan.ivanov@example.com",
                                  "age": 31
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.update.href").value("http://localhost/api/v2/users/1"));
    }

    @Test
    void deleteUserShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v2/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    private UserResponse buildUserResponse(Long id, String name, String email, Integer age) {
        return new UserResponse(id, name, email, age, LocalDateTime.of(2024, 1, 10, 12, 0));
    }
}