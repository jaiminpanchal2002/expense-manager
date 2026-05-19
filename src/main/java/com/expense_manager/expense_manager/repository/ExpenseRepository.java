package com.expense_manager.expense_manager.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.expense_manager.expense_manager.entity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByExpenseDateBetween(LocalDate from, LocalDate to);

    List<Expense> findAllByOrderByExpenseDateDesc();
}