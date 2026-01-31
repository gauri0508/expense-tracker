package com.expensetracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {

    private BigDecimal totalExpenses;
    private Integer totalTransactions;
    private BigDecimal averageExpense;
    private BigDecimal highestExpense;
    private BigDecimal lowestExpense;
    private List<CategoryBreakdown> categoryBreakdown;
    private List<MonthlyTrend> monthlyTrends;
    private Map<String, BigDecimal> paymentMethodBreakdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBreakdown {
        private String categoryId;
        private String categoryName;
        private BigDecimal amount;
        private Integer count;
        private Double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTrend {
        private String month;
        private BigDecimal amount;
        private Integer count;
    }
}
