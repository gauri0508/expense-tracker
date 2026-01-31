package com.expensetracker.repository;

import com.expensetracker.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {

    List<Category> findByUserId(String userId);

    Optional<Category> findByIdAndUserId(String id, String userId);

    Optional<Category> findByUserIdAndName(String userId, String name);

    Boolean existsByUserIdAndName(String userId, String name);

    void deleteByIdAndUserId(String id, String userId);

    List<Category> findByIsDefaultTrue();
}
