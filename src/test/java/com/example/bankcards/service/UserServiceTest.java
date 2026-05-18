package com.example.bankcards.service;

import com.example.bankcards.TestData;
import com.example.bankcards.dto.response.UserResponseDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.exception.InvalidRoleException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Get all users: Should return page of users")
    void getAllUsers_ShouldReturnPageOfUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        User user = TestData.createTestUser();
        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);
        UserResponseDto expectedResponse = TestData.validUserResponseDto();

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toResponseDto(any(User.class))).thenReturn(expectedResponse);

        Page<UserResponseDto> response = userService.getAllUsers(pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    @DisplayName("Get user by id: Should return user when found")
    void getUserById_WhenUserExists_ShouldReturnUser() {
        User user = TestData.createTestUser();
        UserResponseDto expectedResponse = TestData.validUserResponseDto();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userMapper.toResponseDto(user)).thenReturn(expectedResponse);

        UserResponseDto response = userService.getUserById(user.getId());

        assertNotNull(response);
        assertEquals(expectedResponse.username(), response.username());
    }

    @Test
    @DisplayName("Get user by id: Should throw UserNotFoundException when not found")
    void getUserById_WhenUserNotFound_ShouldThrowUserNotFoundException() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(TestData.TEST_CARD_ID))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Get user by username: Should return user when found")
    void getUserByUsername_WhenUserExists_ShouldReturnUser() {
        User user = TestData.createTestUser();
        UserResponseDto expectedResponse = TestData.validUserResponseDto();

        when(userRepository.findByUsername(TestData.TEST_USERNAME)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDto(user)).thenReturn(expectedResponse);

        UserResponseDto response = userService.getUserByUsername(TestData.TEST_USERNAME);

        assertNotNull(response);
        assertEquals(TestData.TEST_USERNAME, response.username());
    }

    @Test
    @DisplayName("Update user role: Should update role when valid")
    void updateUserRole_WhenValid_ShouldUpdateRole() {
        User user = TestData.createTestUser();
        UserResponseDto adminResponse = TestData.adminUserResponseDto();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponseDto(user)).thenReturn(adminResponse);

        UserResponseDto response = userService.updateUserRole(user.getId(), "ADMIN");

        assertNotNull(response);
        assertEquals(Role.ADMIN.name(), response.role());
    }

    @Test
    @DisplayName("Update user role: Should throw InvalidRoleException when role is invalid")
    void updateUserRole_WhenRoleInvalid_ShouldThrowInvalidRoleException() {
        User user = TestData.createTestUser();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.updateUserRole(user.getId(), "INVALID_ROLE"))
                .isInstanceOf(InvalidRoleException.class);
    }

    @Test
    @DisplayName("Delete user: Should delete user when exists")
    void deleteUser_WhenUserExists_ShouldDeleteUser() {
        User user = TestData.createTestUser();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userService.deleteUser(user.getId());

        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("Delete user: Should throw UserNotFoundException when user not found")
    void deleteUser_WhenUserNotFound_ShouldThrowUserNotFoundException() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(TestData.TEST_CARD_ID))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).delete(any());
    }
}