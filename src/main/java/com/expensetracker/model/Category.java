package com.expensetracker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "categories")
@CompoundIndex(name = "user_name_idx", def = "{'userId': 1, 'name': 1}", unique = true)
public class Category {

    @Id
    private String id;

    private String userId;

    private String name;

    private String description;

    @Builder.Default
    private String color = "#6366f1";

    private String icon;

    @Builder.Default
    private Boolean isDefault = false;

    @CreatedDate
    private LocalDateTime createdAt;
}
