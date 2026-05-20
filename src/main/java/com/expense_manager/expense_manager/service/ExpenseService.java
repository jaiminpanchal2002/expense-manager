package com.expense_manager.expense_manager.service;

import com.expense_manager.expense_manager.dto.ExpenseRequest;
import com.expense_manager.expense_manager.entity.Expense;
import com.expense_manager.expense_manager.entity.User;
import com.expense_manager.expense_manager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepo;

    public List<Expense> listForUser(User user) {
        return expenseRepo.findByUserOrderByExpenseDateDesc(user);
    }

    public List<Expense> listForUserInMonth(User user, YearMonth ym) {
        return expenseRepo.findByUserAndExpenseDateBetweenOrderByExpenseDateDesc(
                user, ym.atDay(1), ym.atEndOfMonth());
    }

    public Expense create(ExpenseRequest req, User user) {
        Expense e = Expense.builder()
                .title(req.getTitle())
                .amount(req.getAmount())
                .currency(req.getCurrency())
                .category(req.getCategory())
                .expenseDate(req.getExpenseDate())
                .note(req.getNote())
                .user(user)
                .build();
        return expenseRepo.save(e);
    }

    public void delete(Long id, User user) {
        Expense e = expenseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        // security: make sure this expense belongs to the logged-in user
        if (!e.getUser().getId().equals(user.getId())) {
            throw new SecurityException("This expense doesn't belong to you");
        }
        expenseRepo.delete(e);
    }
}