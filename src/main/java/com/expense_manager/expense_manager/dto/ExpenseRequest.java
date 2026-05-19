package com.expense_manager.expense_manager.dto;

import com.expense_manager.expense_manager.entity.Category;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotNull
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull
    private String currency = "EUR";

    @NotNull
    private Category category;

    @NotNull
    private LocalDate expenseDate;

    private String note;
}