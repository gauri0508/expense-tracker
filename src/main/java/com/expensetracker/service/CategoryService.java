package com.expensetracker.service;

import com.expensetracker.dto.request.CategoryRequest;
import com.expensetracker.dto.response.CategoryResponse;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.model.Category;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    @Cacheable(value = "categories", key = "#userId")
    public List<CategoryResponse> getAllCategories(String userId) {
        List<Category> categories = categoryRepository.findByUserId(userId);
        return categories.stream()
                .map(CategoryResponse::fromCategory)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(String categoryId, String userId) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
        return CategoryResponse.fromCategory(category);
    }

    @Transactional
    @CacheEvict(value = "categories", key = "#userId")
    public CategoryResponse createCategory(String userId, CategoryRequest request) {
        if (categoryRepository.existsByUserIdAndName(userId, request.getName())) {
            throw new BadRequestException("Category with name '" + request.getName() + "' already exists");
        }

        Category category = Category.builder()
                .userId(userId)
                .name(request.getName())
                .description(request.getDescription())
                .color(request.getColor() != null ? request.getColor() : "#6366f1")
                .icon(request.getIcon())
                .isDefault(false)
                .build();

        category = categoryRepository.save(category);
        log.info("Category created: {} for user: {}", category.getId(), userId);
        return CategoryResponse.fromCategory(category);
    }

    @Transactional
    @CacheEvict(value = "categories", key = "#userId")
    public CategoryResponse updateCategory(String categoryId, String userId, CategoryRequest request) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        // Check if new name conflicts with existing category
        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (categoryRepository.existsByUserIdAndName(userId, request.getName())) {
                throw new BadRequestException("Category with name '" + request.getName() + "' already exists");
            }
            category.setName(request.getName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getColor() != null) {
            category.setColor(request.getColor());
        }
        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }

        category = categoryRepository.save(category);
        log.info("Category updated: {}", category.getId());
        return CategoryResponse.fromCategory(category);
    }

    @Transactional
    @CacheEvict(value = "categories", key = "#userId")
    public void deleteCategory(String categoryId, String userId) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        // Check if category has expenses
        List<?> expenses = expenseRepository.findByUserIdAndCategoryId(userId, categoryId);
        if (!expenses.isEmpty()) {
            throw new BadRequestException("Cannot delete category with existing expenses. Please reassign or delete the expenses first.");
        }

        categoryRepository.delete(category);
        log.info("Category deleted: {}", categoryId);
    }

    public Category getCategoryEntity(String categoryId, String userId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElse(null);
    }

    public String getCategoryName(String categoryId, String userId) {
        if (categoryId == null) {
            return "Uncategorized";
        }
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .map(Category::getName)
                .orElse("Unknown");
    }
}
