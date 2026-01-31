package com.expensetracker.service;

import com.expensetracker.model.User;
import com.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Value("${app.mail.from-email}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Async
    public void sendWelcomeEmail(String toEmail, String firstName) {
        String subject = "Welcome to Expense Tracker!";
        String body = String.format("""
                Hello %s,

                Welcome to Expense Tracker! We're excited to have you on board.

                With Expense Tracker, you can:
                - Track your daily expenses
                - Set budgets and receive alerts
                - Analyze your spending patterns
                - Upload receipts for your expenses

                Get started by logging in and adding your first expense!

                Best regards,
                The Expense Tracker Team
                """, firstName);

        sendEmail(toEmail, subject, body);
    }

    @Async
    public void sendBudgetAlert(String userId, String budgetName, String message) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("User not found for budget alert: {}", userId);
            return;
        }

        String subject = "Budget Alert: " + budgetName;
        String body = String.format("""
                Hello %s,

                %s

                Budget: %s

                Please review your spending and consider adjusting your budget if needed.

                Best regards,
                The Expense Tracker Team
                """, user.getFirstName(), message, budgetName);

        sendEmail(user.getEmail(), subject, body);
    }

    @Async
    public void sendWeeklySummary(String userId, BigDecimal totalSpent, int transactionCount) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("User not found for weekly summary: {}", userId);
            return;
        }

        String subject = "Your Weekly Expense Summary";
        String body = String.format("""
                Hello %s,

                Here's your weekly expense summary:

                Total Spent: %s %s
                Number of Transactions: %d

                Log in to Expense Tracker to see detailed analytics and manage your expenses.

                Best regards,
                The Expense Tracker Team
                """, user.getFirstName(), user.getPreferredCurrency(), totalSpent.toString(), transactionCount);

        sendEmail(user.getEmail(), subject, body);
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String subject = "Password Reset Request";
        String body = String.format("""
                Hello,

                We received a request to reset your password for your Expense Tracker account.

                Your password reset token is: %s

                This token will expire in 1 hour.

                If you didn't request a password reset, please ignore this email.

                Best regards,
                The Expense Tracker Team
                """, resetToken);

        sendEmail(toEmail, subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    public void sendSimpleEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
        log.info("Simple email sent to: {}", to);
    }
}
