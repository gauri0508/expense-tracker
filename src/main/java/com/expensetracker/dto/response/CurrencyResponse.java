package com.expensetracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyResponse {

    private String baseCurrency;
    private Map<String, BigDecimal> rates;
    private LocalDateTime lastUpdated;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConversionResult {
        private String fromCurrency;
        private String toCurrency;
        private BigDecimal amount;
        private BigDecimal convertedAmount;
        private BigDecimal exchangeRate;
        private LocalDateTime timestamp;
    }
}
