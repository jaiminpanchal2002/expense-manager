package com.expense_manager.expense_manager.service;

import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${BREVO_API_KEY}")
    private String apiKey;

    private final OkHttpClient client = new OkHttpClient();

    public void sendReport(String to, String monthLabel, String csv,
            BigDecimal total, String name) {

        try {

            String htmlContent = """
                    <h2>Expense Report</h2>

                    <p>Hi %s,</p>

                    <p>Here is your expense report for <b>%s</b>.</p>

                    <p><b>Total spent:</b> %s EUR</p>

                    <p>The full breakdown is attached below:</p>

                    <pre>%s</pre>

                    <br>

                    <p>— Expense Manager</p>
                    """.formatted(name, monthLabel, total, csv);

            sendEmail(
                    to,
                    "Expense Report - " + monthLabel,
                    htmlContent);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send report email", e);
        }
    }

    public void sendBudgetAlert(String to, String name, BigDecimal total,
            BigDecimal limit, String monthLabel) {

        try {

            String htmlContent = """
                    <h2>⚠️ Budget Alert</h2>

                    <p>Hi %s,</p>

                    <p>Your spending for <b>%s</b> has reached <b>%s EUR</b>.</p>

                    <p>Your monthly limit is <b>%s EUR</b>.</p>

                    <br>

                    <p>— Expense Manager</p>
                    """.formatted(name, monthLabel, total, limit);

            sendEmail(
                    to,
                    "⚠️ Budget Alert - " + monthLabel,
                    htmlContent);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send alert email", e);
        }
    }

    private void sendEmail(String to, String subject, String htmlContent) throws Exception {

        MediaType mediaType = MediaType.parse("application/json");

        String json = """
                {
                  "sender":{"email":"noreply@expensemanagerde.in"},
                  "to":[{"email":"%s"}],
                  "subject":"%s",
                  "htmlContent":"%s"
                }
                """.formatted(
                to,
                subject,
                htmlContent.replace("\"", "\\\"").replace("\n", ""));

        RequestBody body = RequestBody.create(json, mediaType);

        Request request = new Request.Builder()
                .url("https://api.brevo.com/v3/smtp/email")
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("api-key", apiKey)
                .addHeader("content-type", "application/json")
                .build();

        Response response = client.newCall(request).execute();

        System.out.println(response.body().string());
    }
}