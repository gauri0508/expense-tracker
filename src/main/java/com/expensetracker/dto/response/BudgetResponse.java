package com.expensetracker.dto.response;

import com.expensetracker.model.Budget;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse {

    private String id;
    private String userId;
    private String categoryId;
    private String categoryName;
    private String name;
    private BigDecimal limitAmount;
    private String periodType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer alertThreshold;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static BudgetResponse fromBudget(Budget budget) {
        return BudgetResponse.builder()
                .id(budget.getId())
                .userId(budget.getUserId())
                .categoryId(budget.getCategoryId())
                .name(budget.getName())
                .limitAmount(budget.getLimitAmount())
                .periodType(budget.getPeriodType().name())
                .startDate(budget.getStartDate())
                .endDate(budget.getEndDate())
                .alertThreshold(budget.getAlertThreshold())
                .isActive(budget.getIsActive())
                .createdAt(budget.getCreatedAt())
                .build();
    }

    public static BudgetResponse fromBudget(Budget budget, String categoryName) {
        BudgetResponse response = fromBudget(budget);
        response.setCategoryName(categoryName);
        return response;
    }
}
