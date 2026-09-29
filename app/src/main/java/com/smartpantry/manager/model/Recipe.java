package com.smartpantry.manager.model;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
    private long id;
    private String name;
    private String description;
    private String prepSteps;       // full preparation method (newline-separated steps)
    private int prepTimeMinutes;
    private String category;        // e.g. Breakfast, Lunch, Dinner, Snack
    private long createdAt;

    // Transient — loaded separately by DAO join
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe() {
        this.createdAt = System.currentTimeMillis();
    }

    public Recipe(String name, String description, String prepSteps, int prepTimeMinutes, String category) {
        this();
        this.name = name;
        this.description = description;
        this.prepSteps = prepSteps;
        this.prepTimeMinutes = prepTimeMinutes;
        this.category = category;
    }

    // --- Getters & Setters ---

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPrepSteps() { return prepSteps; }
    public void setPrepSteps(String prepSteps) { this.prepSteps = prepSteps; }

    public int getPrepTimeMinutes() { return prepTimeMinutes; }
    public void setPrepTimeMinutes(int prepTimeMinutes) { this.prepTimeMinutes = prepTimeMinutes; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<RecipeIngredient> ingredients) { this.ingredients = ingredients; }

    public int getIngredientCount() { return ingredients == null ? 0 : ingredients.size(); }
}
