package com.expensetracker.service;

import com.expensetracker.model.Budget;
import com.expensetracker.model.User;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTaskService {

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final EmailService emailService;
    private final BudgetService budgetService;

    /**
     * Send weekly expense summary every Sunday at 9 AM
     */
    @Scheduled(cron = "0 0 9 * * SUN")
    public void sendWeeklySummaries() {
        log.info("Starting weekly summary email job");

        List<User> activeUsers = userRepository.findAll().stream()
                .filter(User::getIsActive)
                .toList();

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusWeeks(1);

        for (User user : activeUsers) {
            try {
                var expenses = expenseRepository.findByUserIdAndExpenseDateBetween(
                        user.getId(), startDate, endDate);

                BigDecimal totalSpent = expenses.stream()
                        .map(expense -> expense.getAmount())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                int transactionCount = expenses.size();

                emailService.sendWeeklySummary(user.getId(), totalSpent, transactionCount);
                log.info("Weekly summary sent to user: {}", user.getEmail());

            } catch (Exception e) {
                log.error("Failed to send weekly summary to {}: {}", user.getEmail(), e.getMessage());
            }
        }

        log.info("Weekly summary job completed");
    }

    /**
     * Check budgets daily at 8 AM and send alerts if needed
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void checkBudgetsAndSendAlerts() {
        log.info("Starting daily budget check job");

        List<Budget> activeBudgets = budgetRepository.findByIsActiveTrue();

        for (Budget budget : activeBudgets) {
            try {
                if (budget.getCategoryId() != null) {
                    budgetService.checkBudgetAndAlert(budget.getUserId(), budget.getCategoryId());
                }
            } catch (Exception e) {
                log.error("Failed to check budget {}: {}", budget.getId(), e.getMessage());
            }
        }

        log.info("Daily budget check completed");
    }
}
