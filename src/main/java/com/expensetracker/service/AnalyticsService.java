package com.expensetracker.service;

import com.expensetracker.dto.response.AnalyticsResponse;
import com.expensetracker.model.Expense;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    @Cacheable(value = "analytics-summary", key = "#userId + '-' + #startDate + '-' + #endDate")
    public AnalyticsResponse getExpenseSummary(String userId, LocalDate startDate, LocalDate endDate) {
        List<Expense> expenses = expenseRepository.findByUserIdAndExpenseDateBetween(userId, startDate, endDate);

        if (expenses.isEmpty()) {
            return AnalyticsResponse.builder()
                    .totalExpenses(BigDecimal.ZERO)
                    .totalTransactions(0)
                    .averageExpense(BigDecimal.ZERO)
                    .highestExpense(BigDecimal.ZERO)
                    .lowestExpense(BigDecimal.ZERO)
                    .categoryBreakdown(new ArrayList<>())
                    .monthlyTrends(new ArrayList<>())
                    .paymentMethodBreakdown(new HashMap<>())
                    .build();
        }

        BigDecimal totalExpenses = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalTransactions = expenses.size();

        BigDecimal averageExpense = totalExpenses.divide(
                BigDecimal.valueOf(totalTransactions), 2, RoundingMode.HALF_UP);

        BigDecimal highestExpense = expenses.stream()
                .map(Expense::getAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal lowestExpense = expenses.stream()
                .map(Expense::getAmount)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        List<AnalyticsResponse.CategoryBreakdown> categoryBreakdown = getCategoryBreakdown(userId, expenses, totalExpenses);
        List<AnalyticsResponse.MonthlyTrend> monthlyTrends = getMonthlyTrends(expenses);
        Map<String, BigDecimal> paymentMethodBreakdown = getPaymentMethodBreakdown(expenses);

        return AnalyticsResponse.builder()
                .totalExpenses(totalExpenses)
                .totalTransactions(totalTransactions)
                .averageExpense(averageExpense)
                .highestExpense(highestExpense)
                .lowestExpense(lowestExpense)
                .categoryBreakdown(categoryBreakdown)
                .monthlyTrends(monthlyTrends)
                .paymentMethodBreakdown(paymentMethodBreakdown)
                .build();
    }

    public List<AnalyticsResponse.CategoryBreakdown> getCategoryWiseExpenses(String userId,
            LocalDate startDate, LocalDate endDate) {
        List<Expense> expenses = expenseRepository.findByUserIdAndExpenseDateBetween(userId, startDate, endDate);

        BigDecimal totalExpenses = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return getCategoryBreakdown(userId, expenses, totalExpenses);
    }

    public List<AnalyticsResponse.MonthlyTrend> getMonthlyExpenses(String userId, LocalDate startDate, LocalDate endDate) {
        List<Expense> expenses = expenseRepository.findByUserIdAndExpenseDateBetween(userId, startDate, endDate);
        return getMonthlyTrends(expenses);
    }

    public AnalyticsResponse.MonthlyTrend getTrendAnalysis(String userId, LocalDate startDate, LocalDate endDate) {
        List<Expense> expenses = expenseRepository.findByUserIdAndExpenseDateBetween(userId, startDate, endDate);

        BigDecimal totalAmount = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return AnalyticsResponse.MonthlyTrend.builder()
                .month(startDate.toString() + " to " + endDate.toString())
                .amount(totalAmount)
                .count(expenses.size())
                .build();
    }

    private List<AnalyticsResponse.CategoryBreakdown> getCategoryBreakdown(String userId,
            List<Expense> expenses, BigDecimal totalExpenses) {

        Map<String, List<Expense>> expensesByCategory = expenses.stream()
                .collect(Collectors.groupingBy(e -> e.getCategoryId() != null ? e.getCategoryId() : "uncategorized"));

        List<AnalyticsResponse.CategoryBreakdown> breakdown = new ArrayList<>();

        for (Map.Entry<String, List<Expense>> entry : expensesByCategory.entrySet()) {
            String categoryId = entry.getKey();
            List<Expense> categoryExpenses = entry.getValue();

            BigDecimal categoryTotal = categoryExpenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            double percentage = totalExpenses.compareTo(BigDecimal.ZERO) > 0
                    ? categoryTotal.divide(totalExpenses, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue()
                    : 0.0;

            String categoryName = "uncategorized".equals(categoryId)
                    ? "Uncategorized"
                    : categoryRepository.findByIdAndUserId(categoryId, userId)
                            .map(c -> c.getName())
                            .orElse("Unknown");

            breakdown.add(AnalyticsResponse.CategoryBreakdown.builder()
                    .categoryId(categoryId)
                    .categoryName(categoryName)
                    .amount(categoryTotal)
                    .count(categoryExpenses.size())
                    .percentage(percentage)
                    .build());
        }

        breakdown.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));
        return breakdown;
    }

    private List<AnalyticsResponse.MonthlyTrend> getMonthlyTrends(List<Expense> expenses) {
        Map<String, List<Expense>> expensesByMonth = expenses.stream()
                .collect(Collectors.groupingBy(e -> e.getExpenseDate().getYear() + "-"
                        + String.format("%02d", e.getExpenseDate().getMonthValue())));

        List<AnalyticsResponse.MonthlyTrend> trends = new ArrayList<>();

        for (Map.Entry<String, List<Expense>> entry : expensesByMonth.entrySet()) {
            BigDecimal monthTotal = entry.getValue().stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            trends.add(AnalyticsResponse.MonthlyTrend.builder()
                    .month(entry.getKey())
                    .amount(monthTotal)
                    .count(entry.getValue().size())
                    .build());
        }

        trends.sort((a, b) -> a.getMonth().compareTo(b.getMonth()));
        return trends;
    }

    private Map<String, BigDecimal> getPaymentMethodBreakdown(List<Expense> expenses) {
        return expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getPaymentMethod() != null ? e.getPaymentMethod().name() : "UNKNOWN",
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)));
    }
}
