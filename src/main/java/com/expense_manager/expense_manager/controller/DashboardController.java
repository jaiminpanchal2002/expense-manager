package com.expense_manager.expense_manager.controller;

import com.expense_manager.expense_manager.entity.Expense;
import com.expense_manager.expense_manager.entity.User;
import com.expense_manager.expense_manager.service.ExpenseService;
import com.expense_manager.expense_manager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {
    private final ExpenseService expenseService;
    private final UserService userService;

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(value = "ym", required = false) String ymParam,
            Principal principal, Model model) {

        
        User user = userService.getByEmail(principal.getName());

        // which month are we viewing? default = current month
        YearMonth ym = (ymParam != null) ? YearMonth.parse(ymParam) : YearMonth.now();

        List<Expense> expenses = expenseService.listForUserInMonth(user, ym);
        BigDecimal total = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("userName", user.getName());
        model.addAttribute("expenses", expenses);
        model.addAttribute("total", total);
        model.addAttribute("monthLabel", ym.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        model.addAttribute("prevMonth", ym.minusMonths(1)); // "2026-04"
        model.addAttribute("nextMonth", ym.plusMonths(1));
        return "dashboard";
    }
}