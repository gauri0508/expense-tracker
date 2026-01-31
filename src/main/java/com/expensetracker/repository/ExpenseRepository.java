package com.expensetracker.repository;

import com.expensetracker.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends MongoRepository<Expense, String> {

    Page<Expense> findByUserId(String userId, Pageable pageable);

    Optional<Expense> findByIdAndUserId(String id, String userId);

    void deleteByIdAndUserId(String id, String userId);

    @Query("{'userId': ?0, 'expenseDate': {$gte: ?1, $lte: ?2}}")
    List<Expense> findByUserIdAndExpenseDateBetween(String userId, LocalDate startDate, LocalDate endDate);

    @Query("{'userId': ?0, 'categoryId': ?1, 'expenseDate': {$gte: ?2, $lte: ?3}}")
    List<Expense> findByUserIdAndCategoryIdAndExpenseDateBetween(
            String userId, String categoryId, LocalDate startDate, LocalDate endDate);

    Page<Expense> findByUserIdAndCategoryId(String userId, String categoryId, Pageable pageable);

    @Query("{'userId': ?0, 'amount': {$gte: ?1, $lte: ?2}}")
    Page<Expense> findByUserIdAndAmountBetween(String userId, BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    List<Expense> findByUserIdAndCategoryId(String userId, String categoryId);
}
