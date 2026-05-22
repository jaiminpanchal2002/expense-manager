package com.expense_manager.expense_manager.service;

import com.expense_manager.expense_manager.dto.ExpenseRequest;
import com.expense_manager.expense_manager.entity.Category;
import com.expense_manager.expense_manager.entity.Expense;
import com.expense_manager.expense_manager.entity.User;
import com.expense_manager.expense_manager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepo;
    private final EmailService emailService;

    @Value("${app.budget.monthly-limit:600}")
    private BigDecimal monthlyLimit;

    public BigDecimal getMonthlyLimit() { return monthlyLimit; }

    public List<Expense> listForUserInMonth(User user, YearMonth ym) {
        return expenseRepo.findByUserAndExpenseDateBetweenOrderByExpenseDateDesc(
            user, ym.atDay(1), ym.atEndOfMonth());
    }

    public BigDecimal monthTotal(User user, YearMonth ym) {
        return listForUserInMonth(user, ym).stream()
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<Category, BigDecimal> totalsByCategory(List<Expense> expenses) {
        Map<Category, BigDecimal> map = new LinkedHashMap<>();
        for (Expense e : expenses) {
            map.merge(e.getCategory(), e.getAmount(), BigDecimal::add);
        }
        return map;
    }

    public Expense create(ExpenseRequest req, User user) {
        YearMonth ym = YearMonth.from(req.getExpenseDate());
        BigDecimal before = monthTotal(user, ym);

        Expense saved = expenseRepo.save(Expense.builder()
            .title(req.getTitle())
            .amount(req.getAmount())
            .currency(req.getCurrency())
            .category(req.getCategory())
            .expenseDate(req.getExpenseDate())
            .note(req.getNote())
            .user(user)
            .build());

        BigDecimal after = before.add(req.getAmount());
        // send alert only the first time the month crosses the limit (avoids spam)
        if (before.compareTo(monthlyLimit) < 0 && after.compareTo(monthlyLimit) >= 0) {
            String label = ym.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
            emailService.sendBudgetAlert(user.getEmail(), user.getName(), after, monthlyLimit, label);
        }
        return saved;
    }

    public void delete(Long id, User user) {
        Expense e = expenseRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Expense not found"));
        if (!e.getUser().getId().equals(user.getId())) {
            throw new SecurityException("This expense doesn't belong to you");
        }
        expenseRepo.delete(e);
    }

    public String buildCsv(List<Expense> expenses) {
        StringBuilder sb = new StringBuilder("Date,Title,Category,Amount,Currency,Note\n");
        for (Expense e : expenses) {
            sb.append(e.getExpenseDate()).append(',')
              .append(csv(e.getTitle())).append(',')
              .append(e.getCategory()).append(',')
              .append(e.getAmount()).append(',')
              .append(e.getCurrency()).append(',')
              .append(csv(e.getNote() == null ? "" : e.getNote()))
              .append('\n');
        }
        return sb.toString();
    }

    // wrap fields with commas/quotes safely
    private String csv(String value) {
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}