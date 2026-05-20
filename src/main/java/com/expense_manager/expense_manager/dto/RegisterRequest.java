package com.expense_manager.expense_manager.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank @Email(message = "Enter a valid email")
    private String email;

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}