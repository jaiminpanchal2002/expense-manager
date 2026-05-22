package com.expense_manager.expense_manager.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendReport(String to, String monthLabel, String csv,
                           BigDecimal total, String name) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("Expense Report - " + monthLabel);
            helper.setText("""
                Hi %s,

                Here is your expense report for %s.
                Total spent: %s EUR

                The full breakdown is attached as a CSV file.

                — Expense Manager
                """.formatted(name, monthLabel, total));
            helper.addAttachment("expenses-" + monthLabel + ".csv",
                new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8)));
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send report email", e);
        }
    }

    public void sendBudgetAlert(String to, String name, BigDecimal total,
                                BigDecimal limit, String monthLabel) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("⚠️ Budget Alert - " + monthLabel);
            helper.setText("""
                Hi %s,

                Heads up — your spending for %s has reached %s EUR,
                which is at or above your monthly limit of %s EUR.

                — Expense Manager
                """.formatted(name, monthLabel, total, limit));
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send alert email", e);
        }
    }
}