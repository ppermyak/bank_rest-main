package com.example.bankcards.controller;

import com.example.bankcards.TestData;
import com.example.bankcards.dto.response.UserResponseDto;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("GET /admin/users - ADMIN should return page of users")
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_AsAdmin_ShouldReturnPageOfUsers() throws Exception {
        Page<UserResponseDto> page = new PageImpl<>(
                List.of(TestData.validUserResponseDto()),
                PageRequest.of(0, 10),
                1
        );

        when(userService.getAllUsers(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/admin/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /admin/users/{id} - ADMIN should return user by id")
    @WithMockUser(roles = "ADMIN")
    void getUserById_AsAdmin_ShouldReturnUser() throws Exception {
        UserResponseDto response = TestData.validUserResponseDto();
        UUID userId = response.id();

        when(userService.getUserById(userId)).thenReturn(response);

        mockMvc.perform(get("/admin/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value(TestData.TEST_USERNAME));
    }

    @Test
    @DisplayName("GET /admin/users/username/{username} - ADMIN should return user by username")
    @WithMockUser(roles = "ADMIN")
    void getUserByUsername_AsAdmin_ShouldReturnUser() throws Exception {
        UserResponseDto response = TestData.validUserResponseDto();

        when(userService.getUserByUsername(TestData.TEST_USERNAME)).thenReturn(response);

        mockMvc.perform(get("/admin/users/username/{username}", TestData.TEST_USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(TestData.TEST_USERNAME));
    }

    @Test
    @DisplayName("PATCH /admin/users/{id}/role - ADMIN should update role")
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_AsAdmin_ShouldUpdateRole() throws Exception {
        UserResponseDto adminResponse = TestData.adminUserResponseDto();
        UUID userId = adminResponse.id();

        when(userService.updateUserRole(eq(userId), eq("ADMIN"))).thenReturn(adminResponse);

        mockMvc.perform(patch("/admin/users/{id}/role", userId)
                        .param("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value(TestData.ROLE_ADMIN));
    }

    @Test
    @DisplayName("DELETE /admin/users/{id} - ADMIN should delete user")
    @WithMockUser(roles = "ADMIN")
    void deleteUser_AsAdmin_ShouldReturnNoContent() throws Exception {
        UUID userId = TestData.validUserResponseDto().id();
        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/admin/users/{id}", userId))
                .andExpect(status().isNoContent());
    }
}