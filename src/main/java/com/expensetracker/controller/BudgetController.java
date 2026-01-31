package com.expensetracker.controller;

import com.expensetracker.dto.request.BudgetRequest;
import com.expensetracker.dto.response.ApiResponse;
import com.expensetracker.dto.response.BudgetResponse;
import com.expensetracker.dto.response.BudgetStatusResponse;
import com.expensetracker.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
@Tag(name = "Budgets", description = "Budget management APIs")
@SecurityRequirement(name = "bearerAuth")
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    @Operation(summary = "Get all budgets", description = "Returns all budgets for the current user")
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getAllBudgets(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<BudgetResponse> budgets = budgetService.getAllBudgets(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(budgets));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get budget by ID", description = "Returns a specific budget by ID")
    public ResponseEntity<ApiResponse<BudgetResponse>> getBudgetById(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        BudgetResponse budget = budgetService.getBudgetById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(budget));
    }

    @PostMapping
    @Operation(summary = "Create budget", description = "Creates a new budget")
    public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(
            @Valid @RequestBody BudgetRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        BudgetResponse budget = budgetService.createBudget(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Budget created successfully", budget));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update budget", description = "Updates an existing budget")
    public ResponseEntity<ApiResponse<BudgetResponse>> updateBudget(
            @PathVariable String id,
            @Valid @RequestBody BudgetRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        BudgetResponse budget = budgetService.updateBudget(id, userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Budget updated successfully", budget));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete budget", description = "Deletes a budget")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        budgetService.deleteBudget(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Budget deleted successfully"));
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Get budget status", description = "Returns the current status of a budget including spent amount and percentage")
    public ResponseEntity<ApiResponse<BudgetStatusResponse>> getBudgetStatus(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        BudgetStatusResponse status = budgetService.getBudgetStatus(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}
