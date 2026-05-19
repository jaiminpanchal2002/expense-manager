package com.expense_manager.expense_manager.controller;

import com.expense_manager.expense_manager.dto.ExpenseRequest;
import com.expense_manager.expense_manager.entity.Category;
import com.expense_manager.expense_manager.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // GET /expenses → show all expenses
    @GetMapping
    public String list(Model model) {
        model.addAttribute("expenses", expenseService.listAll());
        return "expenses/list"; // renders templates/expenses/list.html
    }

    // GET /expenses/new → show the "add expense" form
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("expenseRequest", new ExpenseRequest());
        model.addAttribute("categories", Category.values());
        return "expenses/form";
    }

    // POST /expenses → handle form submission
    @PostMapping
    public String create(@Valid @ModelAttribute ExpenseRequest expenseRequest,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", Category.values());
            return "expenses/form";
        }
        expenseService.create(expenseRequest);
        return "redirect:/expenses";
    }

    // POST /expenses/{id}/delete → delete an expense
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        expenseService.delete(id);
        return "redirect:/expenses";
    }
}