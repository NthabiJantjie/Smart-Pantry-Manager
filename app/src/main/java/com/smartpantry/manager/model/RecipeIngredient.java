package com.smartpantry.manager.model;

public class RecipeIngredient {
    private long id;
    private long recipeId;
    private String ingredientName;   // normalised name used for matching
    private double requiredQuantity;
    private String unit;

    public RecipeIngredient() {}

    public RecipeIngredient(long recipeId, String ingredientName, double requiredQuantity, String unit) {
        this.recipeId = recipeId;
        this.ingredientName = ingredientName;
        this.requiredQuantity = requiredQuantity;
        this.unit = unit;
    }

    // --- Getters & Setters ---

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getRecipeId() { return recipeId; }
    public void setRecipeId(long recipeId) { this.recipeId = recipeId; }

    public String getIngredientName() { return ingredientName; }
    public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }

    public double getRequiredQuantity() { return requiredQuantity; }
    public void setRequiredQuantity(double requiredQuantity) { this.requiredQuantity = requiredQuantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    /** Returns a human-readable string like "2 cups flour" */
    public String getDisplayString() {
        String qty = requiredQuantity % 1 == 0
                ? String.valueOf((int) requiredQuantity)
                : String.valueOf(requiredQuantity);
        String u = unit != null && !unit.isEmpty() ? " " + unit : "";
        return qty + u + " " + ingredientName;
    }
}
