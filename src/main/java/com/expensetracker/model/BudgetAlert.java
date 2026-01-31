package com.expensetracker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "budget_alerts")
public class BudgetAlert {

    @Id
    private String id;

    private String budgetId;

    private String userId;

    private AlertType alertType;

    private String message;

    @Builder.Default
    private Boolean isNotified = false;

    @Builder.Default
    private LocalDateTime triggeredAt = LocalDateTime.now();

    public enum AlertType {
        THRESHOLD_REACHED, BUDGET_EXCEEDED
    }
}
