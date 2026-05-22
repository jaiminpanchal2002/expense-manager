package com.expense_manager.expense_manager.controller;

import com.expense_manager.expense_manager.entity.Expense;
import com.expense_manager.expense_manager.entity.User;
import com.expense_manager.expense_manager.service.EmailService;
import com.expense_manager.expense_manager.service.ExpenseService;
import com.expense_manager.expense_manager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ExportController {
    private final ExpenseService expenseService;
    private final UserService userService;
    private final EmailService emailService;

    @GetMapping("/expenses/export")
    public ResponseEntity<byte[]> downloadCsv(@RequestParam String ym, Principal principal) {
        User user = userService.getByEmail(principal.getName());
        YearMonth yearMonth = YearMonth.parse(ym);
        List<Expense> expenses = expenseService.listForUserInMonth(user, yearMonth);
        byte[] body = expenseService.buildCsv(expenses).getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=expenses-" + ym + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(body);
    }

    @PostMapping("/expenses/email")
    public String emailReport(@RequestParam String ym, @RequestParam String to,
            Principal principal, RedirectAttributes ra) {
        User user = userService.getByEmail(principal.getName());
        YearMonth yearMonth = YearMonth.parse(ym);
        List<Expense> expenses = expenseService.listForUserInMonth(user, yearMonth);
        BigDecimal total = expenseService.monthTotal(user, yearMonth);
        String label = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"));

        try {
            emailService.sendReport(to, label, expenseService.buildCsv(expenses), total, user.getName());
            ra.addFlashAttribute("message", "Report for " + label + " sent to " + to);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Couldn't send email — check your mail settings.");
        }
        return "redirect:/dashboard?ym=" + ym;
    }
}