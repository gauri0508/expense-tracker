package com.expensetracker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "expenses")
@CompoundIndexes({
    @CompoundIndex(name = "user_date_idx", def = "{'userId': 1, 'expenseDate': -1}"),
    @CompoundIndex(name = "user_category_idx", def = "{'userId': 1, 'categoryId': 1}"),
    @CompoundIndex(name = "user_created_idx", def = "{'userId': 1, 'createdAt': -1}")
})
public class Expense {

    @Id
    private String id;

    private String userId;

    private String categoryId;

    private BigDecimal amount;

    @Builder.Default
    private String currency = "USD";

    private String description;

    private LocalDate expenseDate;

    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.CASH;

    private String receiptUrl;

    private List<String> tags;

    @Builder.Default
    private Boolean isRecurring = false;

    private RecurrencePattern recurrencePattern;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum PaymentMethod {
        CASH, CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, UPI, OTHER
    }

    public enum RecurrencePattern {
        DAILY, WEEKLY, MONTHLY, YEARLY
    }
}
