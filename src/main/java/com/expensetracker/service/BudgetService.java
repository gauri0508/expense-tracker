package com.expensetracker.service;

import com.expensetracker.dto.request.BudgetRequest;
import com.expensetracker.dto.response.BudgetResponse;
import com.expensetracker.dto.response.BudgetStatusResponse;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.model.Budget;
import com.expensetracker.model.BudgetAlert;
import com.expensetracker.repository.BudgetAlertRepository;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetAlertRepository budgetAlertRepository;
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final EmailService emailService;

    public List<BudgetResponse> getAllBudgets(String userId) {
        List<Budget> budgets = budgetRepository.findByUserId(userId);
        return budgets.stream()
                .map(budget -> {
                    String categoryName = getCategoryName(budget.getCategoryId(), userId);
                    return BudgetResponse.fromBudget(budget, categoryName);
                })
                .collect(Collectors.toList());
    }

    public BudgetResponse getBudgetById(String budgetId, String userId) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", budgetId));
        String categoryName = getCategoryName(budget.getCategoryId(), userId);
        return BudgetResponse.fromBudget(budget, categoryName);
    }

    @Transactional
    public BudgetResponse createBudget(String userId, BudgetRequest request) {
        Budget budget = Budget.builder()
                .userId(userId)
                .categoryId(request.getCategoryId())
                .name(request.getName())
                .limitAmount(request.getLimitAmount())
                .periodType(request.getPeriodType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .alertThreshold(request.getAlertThreshold() != null ? request.getAlertThreshold() : 80)
                .isActive(true)
                .build();

        budget = budgetRepository.save(budget);
        log.info("Budget created: {} for user: {}", budget.getId(), userId);

        String categoryName = getCategoryName(budget.getCategoryId(), userId);
        return BudgetResponse.fromBudget(budget, categoryName);
    }

    @Transactional
    public BudgetResponse updateBudget(String budgetId, String userId, BudgetRequest request) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", budgetId));

        if (request.getName() != null) {
            budget.setName(request.getName());
        }
        if (request.getCategoryId() != null) {
            budget.setCategoryId(request.getCategoryId());
        }
        if (request.getLimitAmount() != null) {
            budget.setLimitAmount(request.getLimitAmount());
        }
        if (request.getPeriodType() != null) {
            budget.setPeriodType(request.getPeriodType());
        }
        if (request.getStartDate() != null) {
            budget.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            budget.setEndDate(request.getEndDate());
        }
        if (request.getAlertThreshold() != null) {
            budget.setAlertThreshold(request.getAlertThreshold());
        }

        budget = budgetRepository.save(budget);
        log.info("Budget updated: {}", budget.getId());

        String categoryName = getCategoryName(budget.getCategoryId(), userId);
        return BudgetResponse.fromBudget(budget, categoryName);
    }

    @Transactional
    public void deleteBudget(String budgetId, String userId) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", budgetId));

        budgetAlertRepository.deleteByBudgetId(budgetId);
        budgetRepository.delete(budget);
        log.info("Budget deleted: {}", budgetId);
    }

    public BudgetStatusResponse getBudgetStatus(String budgetId, String userId) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", budgetId));

        LocalDate startDate = calculatePeriodStartDate(budget);
        LocalDate endDate = LocalDate.now();

        BigDecimal spentAmount = calculateSpentAmount(userId, budget.getCategoryId(), startDate, endDate);

        String categoryName = getCategoryName(budget.getCategoryId(), userId);

        return BudgetStatusResponse.of(
                budget.getId(),
                budget.getName(),
                budget.getCategoryId(),
                categoryName,
                budget.getLimitAmount(),
                spentAmount);
    }

    @Transactional
    public void checkBudgetAndAlert(String userId, String categoryId) {
        budgetRepository.findByUserIdAndCategoryIdAndIsActiveTrue(userId, categoryId)
                .ifPresent(budget -> {
                    LocalDate startDate = calculatePeriodStartDate(budget);
                    LocalDate endDate = LocalDate.now();

                    BigDecimal spentAmount = calculateSpentAmount(userId, categoryId, startDate, endDate);
                    double percentageUsed = spentAmount.divide(budget.getLimitAmount(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue();

                    if (percentageUsed >= 100) {
                        createAlertIfNotExists(budget, userId, BudgetAlert.AlertType.BUDGET_EXCEEDED,
                                "Budget exceeded! You have spent " + String.format("%.2f", percentageUsed) + "% of your budget.");
                    } else if (percentageUsed >= budget.getAlertThreshold()) {
                        createAlertIfNotExists(budget, userId, BudgetAlert.AlertType.THRESHOLD_REACHED,
                                "Budget alert! You have spent " + String.format("%.2f", percentageUsed) + "% of your budget.");
                    }
                });
    }

    private void createAlertIfNotExists(Budget budget, String userId, BudgetAlert.AlertType alertType, String message) {
        BudgetAlert alert = BudgetAlert.builder()
                .budgetId(budget.getId())
                .userId(userId)
                .alertType(alertType)
                .message(message)
                .isNotified(false)
                .build();

        budgetAlertRepository.save(alert);
        log.info("Budget alert created: {} for budget: {}", alertType, budget.getId());

        // Send email notification
        try {
            emailService.sendBudgetAlert(userId, budget.getName(), message);
        } catch (Exception e) {
            log.error("Failed to send budget alert email: {}", e.getMessage());
        }
    }

    private BigDecimal calculateSpentAmount(String userId, String categoryId, LocalDate startDate, LocalDate endDate) {
        if (categoryId != null) {
            return expenseRepository.findByUserIdAndCategoryIdAndExpenseDateBetween(userId, categoryId, startDate, endDate)
                    .stream()
                    .map(expense -> expense.getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            // Calculate sum using stream instead of aggregation query
            return expenseRepository.findByUserIdAndExpenseDateBetween(userId, startDate, endDate)
                    .stream()
                    .map(expense -> expense.getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    private LocalDate calculatePeriodStartDate(Budget budget) {
        LocalDate now = LocalDate.now();
        return switch (budget.getPeriodType()) {
            case MONTHLY -> now.withDayOfMonth(1);
            case QUARTERLY -> now.withDayOfMonth(1).minusMonths((now.getMonthValue() - 1) % 3);
            case YEARLY -> now.withDayOfYear(1);
        };
    }

    private String getCategoryName(String categoryId, String userId) {
        if (categoryId == null) {
            return "All Categories";
        }
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .map(category -> category.getName())
                .orElse("Unknown");
    }
}
