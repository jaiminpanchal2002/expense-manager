package com.expense_manager.expense_manager.controller;

import com.expense_manager.expense_manager.dto.RegisterRequest;
import com.expense_manager.expense_manager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequest registerRequest,
            BindingResult result, Model model) {
        if (userService.emailExists(registerRequest.getEmail())) {
            result.rejectValue("email", "exists", "That email is already registered");
        }
        if (result.hasErrors()) {
            return "auth/register";
        }
        userService.register(registerRequest);
        return "redirect:/login?registered";
    }
}