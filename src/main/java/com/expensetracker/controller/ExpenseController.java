package com.expensetracker.controller;

import com.expensetracker.dto.request.ExpenseRequest;
import com.expensetracker.dto.response.ApiResponse;
import com.expensetracker.dto.response.ExpenseResponse;
import com.expensetracker.dto.response.PageResponse;
import com.expensetracker.service.ExpenseService;
import com.expensetracker.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Expense management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final FileStorageService fileStorageService;

    @GetMapping
    @Operation(summary = "Get all expenses", description = "Returns paginated list of expenses with optional filters")
    public ResponseEntity<ApiResponse<PageResponse<ExpenseResponse>>> getAllExpenses(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "expenseDate") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Filter by category ID") @RequestParam(required = false) String categoryId,
            @Parameter(description = "Filter by start date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Filter by end date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Filter by minimum amount") @RequestParam(required = false) BigDecimal minAmount,
            @Parameter(description = "Filter by maximum amount") @RequestParam(required = false) BigDecimal maxAmount) {

        PageResponse<ExpenseResponse> expenses = expenseService.getAllExpenses(
                userDetails.getUsername(), page, size, sortBy, sortDir, categoryId, startDate, endDate, minAmount, maxAmount);
        return ResponseEntity.ok(ApiResponse.success(expenses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get expense by ID", description = "Returns a specific expense by ID")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpenseById(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        ExpenseResponse expense = expenseService.getExpenseById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(expense));
    }

    @PostMapping
    @Operation(summary = "Create expense", description = "Creates a new expense")
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(
            @Valid @RequestBody ExpenseRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ExpenseResponse expense = expenseService.createExpense(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Expense created successfully", expense));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update expense", description = "Updates an existing expense")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @PathVariable String id,
            @Valid @RequestBody ExpenseRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ExpenseResponse expense = expenseService.updateExpense(id, userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Expense updated successfully", expense));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete expense", description = "Deletes an expense")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        expenseService.deleteExpense(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Expense deleted successfully"));
    }

    @PostMapping(value = "/{id}/receipt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload receipt", description = "Uploads a receipt image for an expense")
    public ResponseEntity<ApiResponse<ExpenseResponse>> uploadReceipt(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        String receiptUrl = fileStorageService.storeFile(file, userId, id);
        ExpenseResponse expense = expenseService.updateReceiptUrl(id, userId, receiptUrl);
        return ResponseEntity.ok(ApiResponse.success("Receipt uploaded successfully", expense));
    }

    @DeleteMapping("/{id}/receipt")
    @Operation(summary = "Delete receipt", description = "Deletes the receipt from an expense")
    public ResponseEntity<ApiResponse<Void>> deleteReceipt(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        ExpenseResponse expense = expenseService.getExpenseById(id, userId);
        if (expense.getReceiptUrl() != null) {
            fileStorageService.deleteFile(expense.getReceiptUrl(), userId);
        }
        expenseService.deleteReceipt(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Receipt deleted successfully"));
    }
}
