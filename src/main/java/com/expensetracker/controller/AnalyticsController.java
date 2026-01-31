package com.expensetracker.controller;

import com.expensetracker.dto.response.AnalyticsResponse;
import com.expensetracker.dto.response.ApiResponse;
import com.expensetracker.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Expense analytics APIs")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    @Operation(summary = "Get expense summary", description = "Returns comprehensive expense summary for a date range")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getExpenseSummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        AnalyticsResponse summary = analyticsService.getExpenseSummary(userDetails.getUsername(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/by-category")
    @Operation(summary = "Get expenses by category", description = "Returns expense breakdown by category")
    public ResponseEntity<ApiResponse<List<AnalyticsResponse.CategoryBreakdown>>> getExpensesByCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AnalyticsResponse.CategoryBreakdown> breakdown = analyticsService.getCategoryWiseExpenses(
                userDetails.getUsername(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(breakdown));
    }

    @GetMapping("/by-month")
    @Operation(summary = "Get monthly expenses", description = "Returns monthly expense trends")
    public ResponseEntity<ApiResponse<List<AnalyticsResponse.MonthlyTrend>>> getMonthlyExpenses(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AnalyticsResponse.MonthlyTrend> trends = analyticsService.getMonthlyExpenses(
                userDetails.getUsername(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(trends));
    }

    @GetMapping("/trends")
    @Operation(summary = "Get spending trends", description = "Returns spending trend analysis for a date range")
    public ResponseEntity<ApiResponse<AnalyticsResponse.MonthlyTrend>> getTrends(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        AnalyticsResponse.MonthlyTrend trend = analyticsService.getTrendAnalysis(
                userDetails.getUsername(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(trend));
    }
}
