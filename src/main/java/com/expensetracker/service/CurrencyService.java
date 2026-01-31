package com.expensetracker.service;

import com.expensetracker.dto.response.CurrencyResponse;
import com.expensetracker.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyService {

    @Value("${app.currency.api-key}")
    private String apiKey;

    @Value("${app.currency.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Cacheable(value = "exchange-rates", key = "#baseCurrency")
    public CurrencyResponse getExchangeRates(String baseCurrency) {
        String url = String.format("%s/%s/latest/%s", baseUrl, apiKey, baseCurrency.toUpperCase());

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            if (body == null || !"success".equals(body.get("result"))) {
                throw new BadRequestException("Failed to fetch exchange rates");
            }

            @SuppressWarnings("unchecked")
            Map<String, Number> rates = (Map<String, Number>) body.get("conversion_rates");

            Map<String, BigDecimal> convertedRates = new HashMap<>();
            for (Map.Entry<String, Number> entry : rates.entrySet()) {
                convertedRates.put(entry.getKey(), BigDecimal.valueOf(entry.getValue().doubleValue()));
            }

            return CurrencyResponse.builder()
                    .baseCurrency(baseCurrency.toUpperCase())
                    .rates(convertedRates)
                    .lastUpdated(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error fetching exchange rates: {}", e.getMessage());
            throw new BadRequestException("Failed to fetch exchange rates: " + e.getMessage());
        }
    }

    public CurrencyResponse.ConversionResult convertCurrency(String from, String to, BigDecimal amount) {
        CurrencyResponse rates = getExchangeRates(from);

        BigDecimal targetRate = rates.getRates().get(to.toUpperCase());
        if (targetRate == null) {
            throw new BadRequestException("Currency not supported: " + to);
        }

        BigDecimal convertedAmount = amount.multiply(targetRate).setScale(2, RoundingMode.HALF_UP);

        return CurrencyResponse.ConversionResult.builder()
                .fromCurrency(from.toUpperCase())
                .toCurrency(to.toUpperCase())
                .amount(amount)
                .convertedAmount(convertedAmount)
                .exchangeRate(targetRate)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return amount;
        }

        CurrencyResponse.ConversionResult result = convertCurrency(fromCurrency, toCurrency, amount);
        return result.getConvertedAmount();
    }
}
