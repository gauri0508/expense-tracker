package com.expensetracker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "budgets")
@CompoundIndex(name = "user_active_idx", def = "{'userId': 1, 'isActive': 1}")
public class Budget {

    @Id
    private String id;

    private String userId;

    private String categoryId;

    private String name;

    private BigDecimal limitAmount;

    private PeriodType periodType;

    private LocalDate startDate;

    private LocalDate endDate;

    @Builder.Default
    private Integer alertThreshold = 80;

    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    private LocalDateTime createdAt;

    public enum PeriodType {
        MONTHLY, QUARTERLY, YEARLY
    }
}
