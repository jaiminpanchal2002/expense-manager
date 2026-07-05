package com.expense_manager.expense_manager.service;

import com.expense_manager.expense_manager.dto.ExpenseRequest;
import com.expense_manager.expense_manager.entity.Category;
import com.expense_manager.expense_manager.entity.Expense;
import com.expense_manager.expense_manager.entity.User;
import com.expense_manager.expense_manager.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepo;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ExpenseService expenseService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(expenseService, "monthlyLimit", new BigDecimal("600"));
        user = User.builder().id(1L).email("user@example.com").name("Jaimin Panchal").build();
    }

    @Test
    void listForUserInMonth_ShouldReturnExpenses() {
        YearMonth ym = YearMonth.of(2026, 7);
        Expense e1 = Expense.builder().id(1L).amount(new BigDecimal("100")).user(user).build();
        Expense e2 = Expense.builder().id(2L).amount(new BigDecimal("50")).user(user).build();

        when(expenseRepo.findByUserAndExpenseDateBetweenOrderByExpenseDateDesc(
                eq(user), eq(ym.atDay(1)), eq(ym.atEndOfMonth())))
                .thenReturn(Arrays.asList(e1, e2));

        List<Expense> result = expenseService.listForUserInMonth(user, ym);

        assertEquals(2, result.size());
        verify(expenseRepo, times(1)).findByUserAndExpenseDateBetweenOrderByExpenseDateDesc(
                eq(user), eq(ym.atDay(1)), eq(ym.atEndOfMonth()));
    }

    @Test
    void monthTotal_ShouldCalculateSumOfExpenses() {
        YearMonth ym = YearMonth.of(2026, 7);
        Expense e1 = Expense.builder().id(1L).amount(new BigDecimal("100.50")).user(user).build();
        Expense e2 = Expense.builder().id(2L).amount(new BigDecimal("50.25")).user(user).build();

        when(expenseRepo.findByUserAndExpenseDateBetweenOrderByExpenseDateDesc(
                eq(user), eq(ym.atDay(1)), eq(ym.atEndOfMonth())))
                .thenReturn(Arrays.asList(e1, e2));

        BigDecimal total = expenseService.monthTotal(user, ym);

        assertEquals(new BigDecimal("150.75"), total);
    }

    @Test
    void totalsByCategory_ShouldGroupTotalsCorrectly() {
        Expense e1 = Expense.builder().category(Category.FOOD).amount(new BigDecimal("20")).build();
        Expense e2 = Expense.builder().category(Category.FOOD).amount(new BigDecimal("30")).build();
        Expense e3 = Expense.builder().category(Category.RENT).amount(new BigDecimal("500")).build();

        Map<Category, BigDecimal> totals = expenseService.totalsByCategory(Arrays.asList(e1, e2, e3));

        assertEquals(new BigDecimal("50"), totals.get(Category.FOOD));
        assertEquals(new BigDecimal("500"), totals.get(Category.RENT));
    }

    @Test
    void create_ShouldSaveExpense_AndNotSendEmailIfBelowLimit() {
        ExpenseRequest req = new ExpenseRequest("Lunch", new BigDecimal("50"), "EUR", Category.FOOD, LocalDate.of(2026, 7, 5), "Notes");
        Expense savedExpense = Expense.builder()
                .id(1L)
                .title("Lunch")
                .amount(new BigDecimal("50"))
                .currency("EUR")
                .category(Category.FOOD)
                .expenseDate(LocalDate.of(2026, 7, 5))
                .user(user)
                .build();

        when(expenseRepo.findByUserAndExpenseDateBetweenOrderByExpenseDateDesc(
                eq(user), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of()); // total spent before is 0

        when(expenseRepo.save(any(Expense.class))).thenReturn(savedExpense);

        Expense result = expenseService.create(req, user);

        assertNotNull(result);
        assertEquals(new BigDecimal("50"), result.getAmount());
        verify(emailService, never()).sendBudgetAlert(anyString(), anyString(), any(BigDecimal.class), any(BigDecimal.class), anyString());
    }

    @Test
    void create_ShouldSendEmail_WhenCrossingLimit() {
        ExpenseRequest req = new ExpenseRequest("Rent Payment", new BigDecimal("200"), "EUR", Category.RENT, LocalDate.of(2026, 7, 5), "Notes");
        Expense existingExpense = Expense.builder().amount(new BigDecimal("500")).build(); // total spent is 500
        Expense savedExpense = Expense.builder()
                .id(1L)
                .title("Rent Payment")
                .amount(new BigDecimal("200"))
                .currency("EUR")
                .category(Category.RENT)
                .expenseDate(LocalDate.of(2026, 7, 5))
                .user(user)
                .build();

        // returns existing expenses totaling 500
        when(expenseRepo.findByUserAndExpenseDateBetweenOrderByExpenseDateDesc(
                eq(user), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(existingExpense));

        when(expenseRepo.save(any(Expense.class))).thenReturn(savedExpense);

        expenseService.create(req, user);

        // Limit is 600, before was 500, after is 700. It crossed the limit!
        verify(emailService, times(1)).sendBudgetAlert(
                eq("user@example.com"), eq("Jaimin Panchal"), eq(new BigDecimal("700")), eq(new BigDecimal("600")), anyString());
    }

    @Test
    void delete_ShouldDeleteExpense_WhenOwnedByUser() {
        Expense e = Expense.builder().id(1L).user(user).build();
        when(expenseRepo.findById(1L)).thenReturn(java.util.Optional.of(e));

        expenseService.delete(1L, user);

        verify(expenseRepo, times(1)).delete(e);
    }

    @Test
    void delete_ShouldThrowException_WhenNotOwnedByUser() {
        User otherUser = User.builder().id(2L).build();
        Expense e = Expense.builder().id(1L).user(otherUser).build();
        when(expenseRepo.findById(1L)).thenReturn(java.util.Optional.of(e));

        assertThrows(SecurityException.class, () -> expenseService.delete(1L, user));
        verify(expenseRepo, never()).delete(any(Expense.class));
    }
}
