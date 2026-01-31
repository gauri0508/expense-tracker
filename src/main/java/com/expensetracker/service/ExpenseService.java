package com.expensetracker.service;

import com.expensetracker.dto.request.ExpenseRequest;
import com.expensetracker.dto.response.ExpenseResponse;
import com.expensetracker.dto.response.PageResponse;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.model.Expense;
import com.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryService categoryService;
    private final BudgetService budgetService;

    public PageResponse<ExpenseResponse> getAllExpenses(String userId, int page, int size,
            String sortBy, String sortDir, String categoryId, LocalDate startDate,
            LocalDate endDate, BigDecimal minAmount, BigDecimal maxAmount) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Expense> expensePage;

        if (categoryId != null) {
            expensePage = expenseRepository.findByUserIdAndCategoryId(userId, categoryId, pageable);
        } else if (minAmount != null && maxAmount != null) {
            expensePage = expenseRepository.findByUserIdAndAmountBetween(userId, minAmount, maxAmount, pageable);
        } else {
            expensePage = expenseRepository.findByUserId(userId, pageable);
        }

        List<ExpenseResponse> expenseResponses = expensePage.getContent().stream()
                .map(expense -> {
                    String categoryName = categoryService.getCategoryName(expense.getCategoryId(), userId);
                    return ExpenseResponse.fromExpense(expense, categoryName);
                })
                .collect(Collectors.toList());

        return PageResponse.from(expensePage, expenseResponses);
    }

    public ExpenseResponse getExpenseById(String expenseId, String userId) {
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", expenseId));
        String categoryName = categoryService.getCategoryName(expense.getCategoryId(), userId);
        return ExpenseResponse.fromExpense(expense, categoryName);
    }

    @Transactional
    public ExpenseResponse createExpense(String userId, ExpenseRequest request) {
        Expense expense = Expense.builder()
                .userId(userId)
                .categoryId(request.getCategoryId())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .description(request.getDescription())
                .expenseDate(request.getExpenseDate())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : Expense.PaymentMethod.CASH)
                .tags(request.getTags())
                .isRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false)
                .recurrencePattern(request.getRecurrencePattern())
                .build();

        expense = expenseRepository.save(expense);
        log.info("Expense created: {} for user: {}", expense.getId(), userId);

        // Check budget after creating expense
        if (request.getCategoryId() != null) {
            budgetService.checkBudgetAndAlert(userId, request.getCategoryId());
        }

        String categoryName = categoryService.getCategoryName(expense.getCategoryId(), userId);
        return ExpenseResponse.fromExpense(expense, categoryName);
    }

    @Transactional
    public ExpenseResponse updateExpense(String expenseId, String userId, ExpenseRequest request) {
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", expenseId));

        if (request.getAmount() != null) {
            expense.setAmount(request.getAmount());
        }
        if (request.getCurrency() != null) {
            expense.setCurrency(request.getCurrency());
        }
        if (request.getDescription() != null) {
            expense.setDescription(request.getDescription());
        }
        if (request.getExpenseDate() != null) {
            expense.setExpenseDate(request.getExpenseDate());
        }
        if (request.getCategoryId() != null) {
            expense.setCategoryId(request.getCategoryId());
        }
        if (request.getPaymentMethod() != null) {
            expense.setPaymentMethod(request.getPaymentMethod());
        }
        if (request.getTags() != null) {
            expense.setTags(request.getTags());
        }
        if (request.getIsRecurring() != null) {
            expense.setIsRecurring(request.getIsRecurring());
        }
        if (request.getRecurrencePattern() != null) {
            expense.setRecurrencePattern(request.getRecurrencePattern());
        }

        expense = expenseRepository.save(expense);
        log.info("Expense updated: {}", expense.getId());

        String categoryName = categoryService.getCategoryName(expense.getCategoryId(), userId);
        return ExpenseResponse.fromExpense(expense, categoryName);
    }

    @Transactional
    public void deleteExpense(String expenseId, String userId) {
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", expenseId));

        expenseRepository.delete(expense);
        log.info("Expense deleted: {}", expenseId);
    }

    @Transactional
    public ExpenseResponse updateReceiptUrl(String expenseId, String userId, String receiptUrl) {
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", expenseId));

        expense.setReceiptUrl(receiptUrl);
        expense = expenseRepository.save(expense);

        String categoryName = categoryService.getCategoryName(expense.getCategoryId(), userId);
        return ExpenseResponse.fromExpense(expense, categoryName);
    }

    @Transactional
    public void deleteReceipt(String expenseId, String userId) {
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", expenseId));

        expense.setReceiptUrl(null);
        expenseRepository.save(expense);
        log.info("Receipt deleted for expense: {}", expenseId);
    }

    public List<Expense> getExpensesByDateRange(String userId, LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByUserIdAndExpenseDateBetween(userId, startDate, endDate);
    }

    public BigDecimal getTotalExpensesByDateRange(String userId, LocalDate startDate, LocalDate endDate) {
        // Calculate sum using stream instead of aggregation query
        return expenseRepository.findByUserIdAndExpenseDateBetween(userId, startDate, endDate)
                .stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
