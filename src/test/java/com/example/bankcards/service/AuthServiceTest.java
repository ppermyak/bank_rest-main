package com.example.bankcards.service;

import com.example.bankcards.TestData;
import com.example.bankcards.dto.request.LoginRequestDto;
import com.example.bankcards.dto.request.RegisterRequestDto;
import com.example.bankcards.dto.response.AuthResponseDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.EmailAlreadyExistsException;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    private RegisterRequestDto registerRequest;
    private User user;

    private Authentication createAuthentication() {
        Authentication authentication = mock(Authentication.class);
        lenient().doReturn(TestData.TEST_USERNAME).when(authentication).getName();

        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + TestData.ROLE_USER)
        );
        lenient().doReturn(authorities).when(authentication).getAuthorities();

        return authentication;
    }

    @BeforeEach
    void setUp() {
        registerRequest = TestData.validRegisterRequest();
        user = TestData.createTestUser();
    }

    @Test
    @DisplayName("Login: Should return token when credentials are valid")
    void login_WithValidCredentials_ShouldReturnAuthResponseDto() {
        LoginRequestDto request = TestData.validLoginRequest();
        Authentication authentication = createAuthentication();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(TestData.TEST_USERNAME, TestData.ROLE_USER))
                .thenReturn(TestData.TEST_JWT_TOKEN);

        AuthResponseDto response = authService.login(request);

        assertNotNull(response);
        assertEquals(TestData.TEST_JWT_TOKEN, response.token());
        assertEquals(TestData.TEST_USERNAME, response.username());
        assertEquals(TestData.ROLE_USER, response.role());
    }

    @Test
    @DisplayName("Login: Should throw exception when password is invalid")
    void login_WithInvalidPassword_ShouldThrowBadCredentialsException() {
        LoginRequestDto request = new LoginRequestDto(TestData.TEST_USERNAME, "wrong_password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("Register: Should create user and return token when data is valid")
    void register_WithValidData_ShouldCreateUserAndReturnToken() {
        when(userRepository.existsByUsername(TestData.TEST_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(TestData.TEST_EMAIL)).thenReturn(false);
        when(userMapper.toEntity(registerRequest)).thenReturn(user);
        when(passwordEncoder.encode(TestData.TEST_PASSWORD)).thenReturn(TestData.TEST_ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken(TestData.TEST_USERNAME, TestData.ROLE_USER))
                .thenReturn(TestData.TEST_JWT_TOKEN);

        AuthResponseDto response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals(TestData.TEST_JWT_TOKEN, response.token());
        assertEquals(TestData.TEST_USERNAME, response.username());

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Register: Should throw exception when username already exists")
    void register_WhenUsernameExists_ShouldThrowUserAlreadyExistsException() {
        when(userRepository.existsByUsername(TestData.TEST_USERNAME)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Register: Should throw exception when email already exists")
    void register_WhenEmailExists_ShouldThrowEmailAlreadyExistsException() {
        when(userRepository.existsByUsername(TestData.TEST_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(TestData.TEST_EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any(User.class));
    }
}