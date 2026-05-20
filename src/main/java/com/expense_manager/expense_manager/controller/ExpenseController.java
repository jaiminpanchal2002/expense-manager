package com.expense_manager.expense_manager.controller;

import com.expense_manager.expense_manager.dto.ExpenseRequest;
import com.expense_manager.expense_manager.entity.Category;
import com.expense_manager.expense_manager.entity.User;
import com.expense_manager.expense_manager.service.ExpenseService;
import com.expense_manager.expense_manager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDate;

@Controller
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;
    private final UserService userService;

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        ExpenseRequest req = new ExpenseRequest();
        req.setExpenseDate(LocalDate.now()); // pre-fill today's date
        model.addAttribute("expenseRequest", req);
        model.addAttribute("categories", Category.values());
        return "expenses/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute ExpenseRequest expenseRequest,
            BindingResult result, Model model, Principal principal) {
        if (result.hasErrors()) {
            model.addAttribute("categories", Category.values());
            return "expenses/form";
        }
        User user = userService.getByEmail(principal.getName());
        expenseService.create(expenseRequest, user);
        return "redirect:/dashboard";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal) {
        User user = userService.getByEmail(principal.getName());
        expenseService.delete(id, user);
        return "redirect:/dashboard";
    }
}