package com.expense_manager.expense_manager.service;

import com.expense_manager.expense_manager.entity.Expense;
import com.expense_manager.expense_manager.entity.User;
import com.expense_manager.expense_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MonthlyReportScheduler {
    private final UserRepository userRepository;
    private final ExpenseService expenseService;
    private final EmailService emailService;

    // runs at 09:00 on the 1st day of every month
    @Scheduled(cron = "0 0 9 1 * *")
    public void sendMonthlyReports() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        String label = lastMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"));

        for (User user : userRepository.findAll()) {
            List<Expense> expenses = expenseService.listForUserInMonth(user, lastMonth);
            if (expenses.isEmpty())
                continue;

            BigDecimal total = expenseService.monthTotal(user, lastMonth);
            String csv = expenseService.buildCsv(expenses);
            emailService.sendReport(user.getEmail(), label, csv, total, user.getName());
        }
    }
}