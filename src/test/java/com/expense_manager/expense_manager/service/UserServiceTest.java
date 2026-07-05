package com.expense_manager.expense_manager.service;

import com.expense_manager.expense_manager.dto.RegisterRequest;
import com.expense_manager.expense_manager.entity.User;
import com.expense_manager.expense_manager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void emailExists_ShouldReturnTrue_WhenEmailExists() {
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertTrue(userService.emailExists(email));
        verify(userRepository, times(1)).existsByEmail(email);
    }

    @Test
    void emailExists_ShouldReturnFalse_WhenEmailDoesNotExist() {
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        assertFalse(userService.emailExists(email));
        verify(userRepository, times(1)).existsByEmail(email);
    }

    @Test
    void register_ShouldSaveAndReturnUserWithEncodedPassword() {
        RegisterRequest req = new RegisterRequest("John Doe", "john@example.com", "password123");
        User savedUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("encodedPassword123")
                .build();

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.register(req);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("encodedPassword123", result.getPassword());

        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void getByEmail_ShouldReturnUser_WhenUserExists() {
        String email = "john@example.com";
        User user = User.builder().id(1L).email(email).name("John").build();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        User result = userService.getByEmail(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void getByEmail_ShouldThrowException_WhenUserDoesNotExist() {
        String email = "notfound@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> userService.getByEmail(email));
        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository, times(1)).findByEmail(email);
    }
}
