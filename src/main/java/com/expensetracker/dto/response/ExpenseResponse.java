package com.expensetracker.dto.response;

import com.expensetracker.model.Expense;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {

    private String id;
    private String userId;
    private String categoryId;
    private String categoryName;
    private BigDecimal amount;
    private String currency;
    private String description;
    private LocalDate expenseDate;
    private String paymentMethod;
    private String receiptUrl;
    private List<String> tags;
    private Boolean isRecurring;
    private String recurrencePattern;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ExpenseResponse fromExpense(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .userId(expense.getUserId())
                .categoryId(expense.getCategoryId())
                .amount(expense.getAmount())
                .currency(expense.getCurrency())
                .description(expense.getDescription())
                .expenseDate(expense.getExpenseDate())
                .paymentMethod(expense.getPaymentMethod() != null ? expense.getPaymentMethod().name() : null)
                .receiptUrl(expense.getReceiptUrl())
                .tags(expense.getTags())
                .isRecurring(expense.getIsRecurring())
                .recurrencePattern(expense.getRecurrencePattern() != null ? expense.getRecurrencePattern().name() : null)
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }

    public static ExpenseResponse fromExpense(Expense expense, String categoryName) {
        ExpenseResponse response = fromExpense(expense);
        response.setCategoryName(categoryName);
        return response;
    }
}
