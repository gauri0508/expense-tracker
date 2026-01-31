package com.expensetracker.repository;

import com.expensetracker.model.Budget;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends MongoRepository<Budget, String> {

    List<Budget> findByUserId(String userId);

    List<Budget> findByUserIdAndIsActiveTrue(String userId);

    Optional<Budget> findByIdAndUserId(String id, String userId);

    Optional<Budget> findByUserIdAndCategoryIdAndIsActiveTrue(String userId, String categoryId);

    void deleteByIdAndUserId(String id, String userId);

    List<Budget> findByIsActiveTrue();
}
