package com.expense_manager.expense_manager.service;

import com.expense_manager.expense_manager.dto.ExpenseRequest;
import com.expense_manager.expense_manager.entity.Expense;
import com.expense_manager.expense_manager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepo;

    public List<Expense> listAll() {
        return expenseRepo.findAll();
    }

    public Expense create(ExpenseRequest req) {
        Expense e = Expense.builder()
            .title(req.getTitle())
            .amount(req.getAmount())
            .currency(req.getCurrency())
            .category(req.getCategory())
            .expenseDate(req.getExpenseDate())
            .note(req.getNote())
            .build();
        return expenseRepo.save(e);
    }

    public void delete(Long id) {
        expenseRepo.deleteById(id);
    }
}