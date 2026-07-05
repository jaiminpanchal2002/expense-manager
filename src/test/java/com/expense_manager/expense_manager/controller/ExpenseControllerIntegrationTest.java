package com.expense_manager.expense_manager.controller;

import com.expense_manager.expense_manager.dto.ExpenseRequest;
import com.expense_manager.expense_manager.entity.Category;
import com.expense_manager.expense_manager.entity.User;
import com.expense_manager.expense_manager.service.ExpenseService;
import com.expense_manager.expense_manager.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.security.Principal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
class ExpenseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpenseService expenseService;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(username = "user@example.com")
    void showCreateForm_ShouldReturnFormViewWithModel() throws Exception {
        mockMvc.perform(get("/expenses/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("expenses/form"))
                .andExpect(model().attributeExists("expenseRequest"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void createExpense_ShouldRedirectToDashboard_WhenSuccessful() throws Exception {
        User mockUser = User.builder().id(1L).email("user@example.com").build();
        when(userService.getByEmail("user@example.com")).thenReturn(mockUser);

        Principal principal = () -> "user@example.com";

        mockMvc.perform(post("/expenses")
                        .principal(principal)
                        .param("title", "Starbucks Coffee")
                        .param("amount", "5.50")
                        .param("currency", "EUR")
                        .param("category", "FOOD")
                        .param("expenseDate", "2026-07-05")
                        .param("note", "Midday pick-me-up")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(expenseService, times(1)).create(any(ExpenseRequest.class), eq(mockUser));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void deleteExpense_ShouldRedirectToDashboard_WhenSuccessful() throws Exception {
        User mockUser = User.builder().id(1L).email("user@example.com").build();
        when(userService.getByEmail("user@example.com")).thenReturn(mockUser);

        Principal principal = () -> "user@example.com";

        mockMvc.perform(post("/expenses/1/delete")
                        .principal(principal)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(expenseService, times(1)).delete(eq(1L), eq(mockUser));
    }
}
