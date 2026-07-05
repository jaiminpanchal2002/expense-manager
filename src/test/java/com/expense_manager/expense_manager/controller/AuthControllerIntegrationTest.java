package com.expense_manager.expense_manager.controller;

import com.expense_manager.expense_manager.dto.RegisterRequest;
import com.expense_manager.expense_manager.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void getLogin_ShouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void getRegister_ShouldReturnRegisterViewWithModel() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void postRegister_ShouldRedirectToLogin_WhenSuccessful() throws Exception {
        when(userService.emailExists("new@example.com")).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("name", "New User")
                        .param("email", "new@example.com")
                        .param("password", "securepwd")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        verify(userService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void postRegister_ShouldReturnRegisterView_WhenEmailExists() throws Exception {
        when(userService.emailExists("existing@example.com")).thenReturn(true);

        mockMvc.perform(post("/register")
                        .param("name", "Existing User")
                        .param("email", "existing@example.com")
                        .param("password", "securepwd")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("registerRequest", "email"));

        verify(userService, never()).register(any(RegisterRequest.class));
    }
}
