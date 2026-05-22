package com.expense_manager.expense_manager.controller;

import com.expense_manager.expense_manager.entity.Category;
import com.expense_manager.expense_manager.entity.Expense;
import com.expense_manager.expense_manager.entity.User;
import com.expense_manager.expense_manager.service.ExpenseService;
import com.expense_manager.expense_manager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
        YearMonth ym = (ymParam != null) ? YearMonth.parse(ymParam) : YearMonth.now();

        List<Expense> expenses = expenseService.listForUserInMonth(user, ym);
        BigDecimal total = expenseService.monthTotal(user, ym);
        BigDecimal limit = expenseService.getMonthlyLimit();

        // category breakdown for the chart
        Map<Category, BigDecimal> byCat = expenseService.totalsByCategory(expenses);
        List<String> categoryLabels = byCat.keySet().stream().map(Enum::name).toList();
        List<BigDecimal> categoryData = new ArrayList<>(byCat.values());

        int percent = limit.signum() == 0 ? 0
                : total.multiply(BigDecimal.valueOf(100))
                        .divide(limit, 0, RoundingMode.HALF_UP).intValue();

        model.addAttribute("userName", user.getName());
        model.addAttribute("userEmail", user.getEmail());
        model.addAttribute("expenses", expenses);
        model.addAttribute("count", expenses.size());
        model.addAttribute("total", total);
        model.addAttribute("limit", limit);
        model.addAttribute("remaining", limit.subtract(total));
        model.addAttribute("percent", percent);
        model.addAttribute("overBudget", total.compareTo(limit) >= 0);
        model.addAttribute("monthLabel", ym.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        model.addAttribute("ymValue", ym.toString());
        model.addAttribute("prevMonth", ym.minusMonths(1));
        model.addAttribute("nextMonth", ym.plusMonths(1));
        model.addAttribute("categoryLabels", categoryLabels);
        model.addAttribute("categoryData", categoryData);
        return "dashboard";
    }
}