package com.expensetracker.repository;

import com.expensetracker.model.BudgetAlert;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetAlertRepository extends MongoRepository<BudgetAlert, String> {

    List<BudgetAlert> findByUserId(String userId);

    List<BudgetAlert> findByUserIdAndIsNotifiedFalse(String userId);

    List<BudgetAlert> findByBudgetId(String budgetId);

    void deleteByBudgetId(String budgetId);
}
