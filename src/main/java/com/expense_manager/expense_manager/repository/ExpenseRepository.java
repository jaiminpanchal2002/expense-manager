package com.expense_manager.expense_manager.repository;

import com.expense_manager.expense_manager.entity.Expense;
import com.expense_manager.expense_manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUserOrderByExpenseDateDesc(User user);

    List<Expense> findByUserAndExpenseDateBetweenOrderByExpenseDateDesc(
            User user, LocalDate from, LocalDate to);
}