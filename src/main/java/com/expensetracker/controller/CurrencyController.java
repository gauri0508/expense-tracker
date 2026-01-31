package com.expensetracker.controller;

import com.expensetracker.dto.response.ApiResponse;
import com.expensetracker.dto.response.CurrencyResponse;
import com.expensetracker.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/currency")
@RequiredArgsConstructor
@Tag(name = "Currency", description = "Currency conversion APIs")
@SecurityRequirement(name = "bearerAuth")
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping("/rates")
    @Operation(summary = "Get exchange rates", description = "Returns exchange rates for a base currency")
    public ResponseEntity<ApiResponse<CurrencyResponse>> getExchangeRates(
            @Parameter(description = "Base currency code (e.g., USD)") @RequestParam(defaultValue = "USD") String base) {
        CurrencyResponse rates = currencyService.getExchangeRates(base);
        return ResponseEntity.ok(ApiResponse.success(rates));
    }

    @GetMapping("/convert")
    @Operation(summary = "Convert currency", description = "Converts an amount from one currency to another")
    public ResponseEntity<ApiResponse<CurrencyResponse.ConversionResult>> convertCurrency(
            @Parameter(description = "Source currency code") @RequestParam String from,
            @Parameter(description = "Target currency code") @RequestParam String to,
            @Parameter(description = "Amount to convert") @RequestParam BigDecimal amount) {
        CurrencyResponse.ConversionResult result = currencyService.convertCurrency(from, to, amount);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
