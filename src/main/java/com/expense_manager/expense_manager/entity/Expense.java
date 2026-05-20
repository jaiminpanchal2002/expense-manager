package com.expense_manager.expense_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Builder.Default
    @Column(nullable = false, length = 3)
    private String currency = "EUR";

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(nullable = false)
    private LocalDate expenseDate;

    private String note;

    // NEW: each expense belongs to a user
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;
}