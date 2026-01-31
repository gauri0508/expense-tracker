package com.expensetracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetStatusResponse {

    private String budgetId;
    private String budgetName;
    private String categoryId;
    private String categoryName;
    private BigDecimal limitAmount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private Double percentageUsed;
    private String status;

    public static BudgetStatusResponse of(String budgetId, String budgetName, String categoryId,
            String categoryName, BigDecimal limitAmount, BigDecimal spentAmount) {
        BigDecimal remaining = limitAmount.subtract(spentAmount);
        double percentage = spentAmount.divide(limitAmount, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();

        String status;
        if (percentage >= 100) {
            status = "EXCEEDED";
        } else if (percentage >= 80) {
            status = "WARNING";
        } else {
            status = "ON_TRACK";
        }

        return BudgetStatusResponse.builder()
                .budgetId(budgetId)
                .budgetName(budgetName)
                .categoryId(categoryId)
                .categoryName(categoryName)
                .limitAmount(limitAmount)
                .spentAmount(spentAmount)
                .remainingAmount(remaining)
                .percentageUsed(percentage)
                .status(status)
                .build();
    }
}
