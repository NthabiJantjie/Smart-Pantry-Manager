package com.smartpantry.manager.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.smartpantry.manager.model.PantryItem;
import com.smartpantry.manager.model.Recipe;
import com.smartpantry.manager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeDao {

    private final DatabaseHelper dbHelper;

    public RecipeDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // -------------------------------------------------------------------------
    // GET ALL RECIPES (with ingredients loaded)
    // -------------------------------------------------------------------------
    public List<Recipe> getAllRecipes() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Recipe> recipes = new ArrayList<>();

        Cursor c = db.query(DatabaseHelper.TABLE_RECIPES, null, null, null, null, null,
                DatabaseHelper.COL_R_NAME + " ASC");
        while (c.moveToNext()) {
            recipes.add(recipeFromCursor(c));
        }
        c.close();

        // Load ingredients for each recipe
        for (Recipe r : recipes) {
            r.setIngredients(getIngredientsForRecipe(r.getId()));
        }
        return recipes;
    }

    public Recipe getById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_RECIPES, null,
                DatabaseHelper.COL_R_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);
        Recipe recipe = null;
        if (c.moveToFirst()) {
            recipe = recipeFromCursor(c);
            recipe.setIngredients(getIngredientsForRecipe(id));
        }
        c.close();
        return recipe;
    }

    public List<RecipeIngredient> getIngredientsForRecipe(long recipeId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_RECIPE_INGREDIENTS, null,
                DatabaseHelper.COL_RI_RECIPE_ID + "=?",
                new String[]{String.valueOf(recipeId)},
                null, null, DatabaseHelper.COL_RI_NAME + " ASC");
        List<RecipeIngredient> list = new ArrayList<>();
        while (c.moveToNext()) {
            list.add(ingredientFromCursor(c));
        }
        c.close();
        return list;
    }

    // -------------------------------------------------------------------------
    // STRICT MATCHING ALGORITHM
    //
    // A recipe is suggested ONLY if EVERY required ingredient is present in
    // the pantry with at least the required quantity (after unit normalisation).
    //
    // Robustness:
    //   - Case-insensitive comparison
    //   - Singular/plural: strips trailing 's' before comparing
    //   - Unit normalisation: converts common units to a base unit (g/mL)
    //     before quantity comparison so "500 g" matches "0.5 kg"
    // -------------------------------------------------------------------------
    public List<Recipe> getSuggestedRecipes(List<PantryItem> pantryItems) {
        // Build a lookup map: normalised ingredient name -> (total quantity in base unit, base unit)
        Map<String, double[]> pantryMap = buildPantryMap(pantryItems);

        List<Recipe> allRecipes = getAllRecipes();
        List<Recipe> suggested = new ArrayList<>();

        for (Recipe recipe : allRecipes) {
            if (recipeMatchesPantry(recipe, pantryMap)) {
                suggested.add(recipe);
            }
        }
        return suggested;
    }

    /**
     * Returns recipes where exactly 1 ingredient is missing — for "Almost There" bonus list.
     */
    public List<Recipe> getAlmostThereRecipes(List<PantryItem> pantryItems) {
        Map<String, double[]> pantryMap = buildPantryMap(pantryItems);
        List<Recipe> allRecipes = getAllRecipes();
        List<Recipe> almostThere = new ArrayList<>();

        for (Recipe recipe : allRecipes) {
            int missingCount = countMissingIngredients(recipe, pantryMap);
            if (missingCount == 1) {
                almostThere.add(recipe);
            }
        }
        return almostThere;
    }

    // ---- private helpers ----

    private boolean recipeMatchesPantry(Recipe recipe, Map<String, double[]> pantryMap) {
        for (RecipeIngredient ri : recipe.getIngredients()) {
            if (!ingredientSatisfied(ri, pantryMap)) return false;
        }
        return true;
    }

    private int countMissingIngredients(Recipe recipe, Map<String, double[]> pantryMap) {
        int missing = 0;
        for (RecipeIngredient ri : recipe.getIngredients()) {
            if (!ingredientSatisfied(ri, pantryMap)) missing++;
        }
        return missing;
    }

    private boolean ingredientSatisfied(RecipeIngredient ri, Map<String, double[]> pantryMap) {
        String normName = normaliseName(ri.getIngredientName());
        double requiredBase = toBaseUnit(ri.getRequiredQuantity(), ri.getUnit());

        // Direct match
        if (pantryMap.containsKey(normName)) {
            double[] pantryEntry = pantryMap.get(normName);
            double pantryBase = toBaseUnit(pantryEntry[0], unitFromCode(pantryEntry[1]));
            return pantryBase >= requiredBase;
        }

        // Try partial name match: check if any pantry key starts with or contains the required name
        for (Map.Entry<String, double[]> entry : pantryMap.entrySet()) {
            if (namesMatch(entry.getKey(), normName)) {
                double[] pantryEntry = entry.getValue();
                double pantryBase = toBaseUnit(pantryEntry[0], unitFromCode(pantryEntry[1]));
                return pantryBase >= requiredBase;
            }
        }
        return false;
    }

    /**
     * Builds a map of normalised ingredient name -> [quantity, unit code]
     * where unit code is an integer encoding for later toBaseUnit conversion.
     */
    private Map<String, double[]> buildPantryMap(List<PantryItem> pantryItems) {
        Map<String, double[]> map = new HashMap<>();
        for (PantryItem item : pantryItems) {
            String key = normaliseName(item.getName());
            double qty = item.getQuantity();
            double unitCode = encodeUnit(item.getUnit());
            // Accumulate if same normalised name appears multiple times
            if (map.containsKey(key)) {
                double existing = toBaseUnit(map.get(key)[0], unitFromCode(map.get(key)[1]));
                double adding   = toBaseUnit(qty, item.getUnit());
                // Store accumulated base-unit value with a "g" or "mL" unit code
                boolean isVolume = isVolumeUnit(item.getUnit());
                map.put(key, new double[]{existing + adding, isVolume ? -1 : -2});
            } else {
                map.put(key, new double[]{qty, unitCode});
            }
        }
        return map;
    }

    /**
     * Normalise: lowercase, trim, collapse spaces, strip trailing 's'/'es' for plurals.
     */
    public static String normaliseName(String name) {
        if (name == null) return "";
        String s = name.toLowerCase().trim().replaceAll("\\s+", " ");
        // Strip plural suffixes
        if (s.endsWith("es") && s.length() > 3)  s = s.substring(0, s.length() - 2);
        else if (s.endsWith("s") && s.length() > 2) s = s.substring(0, s.length() - 1);
        return s;
    }

    /**
     * Two names match if one is a prefix/suffix of the other after normalisation,
     * or one contains the other (handles "chicken breast" vs "chicken").
     */
    private boolean namesMatch(String a, String b) {
        if (a.equals(b)) return true;
        if (a.contains(b) || b.contains(a)) return true;
        return false;
    }

    // ---- Unit conversion to base units (grams for mass, mL for volume) ----

    private static final double GRAM = 1.0;
    private static final double KG   = 1000.0;
    private static final double LB   = 453.592;
    private static final double OZ   = 28.3495;
    private static final double ML   = 1.0;
    private static final double L    = 1000.0;
    private static final double CUP  = 240.0;
    private static final double TBSP = 15.0;
    private static final double TSP  = 5.0;

    /**
     * Convert quantity + unit string into a base unit (grams or mL).
     * For unitless items (pcs, pack, etc.) no conversion is done — raw quantity is used.
     */
    public static double toBaseUnit(double qty, String unit) {
        if (unit == null || unit.isEmpty()) return qty;
        switch (unit.toLowerCase().trim()) {
            case "kg":   return qty * KG;
            case "lb":   return qty * LB;
            case "oz":   return qty * OZ;
            case "g":    return qty * GRAM;
            case "l":    return qty * L;
            case "ml":   return qty * ML;
            case "cup":  return qty * CUP;
            case "tbsp": return qty * TBSP;
            case "tsp":  return qty * TSP;
            default:     return qty; // pcs, pack, box, can, bottle, bag, slices, cloves, etc.
        }
    }

    private boolean isVolumeUnit(String unit) {
        if (unit == null) return false;
        switch (unit.toLowerCase().trim()) {
            case "l": case "ml": case "cup": case "tbsp": case "tsp": return true;
            default: return false;
        }
    }

    private double encodeUnit(String unit) {
        if (unit == null) return 0;
        return unit.hashCode(); // arbitrary code stored for later retrieval
    }

    private String unitFromCode(double code) {
        if (code == -1) return "mL";
        if (code == -2) return "g";
        return ""; // fallback: treat as unitless
    }

    // ---- Cursor mappers ----

    private Recipe recipeFromCursor(Cursor c) {
        Recipe r = new Recipe();
        r.setId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_R_ID)));
        r.setName(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_R_NAME)));
        r.setDescription(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_R_DESCRIPTION)));
        r.setPrepSteps(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_R_PREP_STEPS)));
        r.setPrepTimeMinutes(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_R_PREP_TIME)));
        r.setCategory(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_R_CATEGORY)));
        r.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_R_CREATED_AT)));
        return r;
    }

    private RecipeIngredient ingredientFromCursor(Cursor c) {
        RecipeIngredient ri = new RecipeIngredient();
        ri.setId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_RI_ID)));
        ri.setRecipeId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_RI_RECIPE_ID)));
        ri.setIngredientName(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_RI_NAME)));
        ri.setRequiredQuantity(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_RI_QUANTITY)));
        ri.setUnit(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_RI_UNIT)));
        return ri;
    }
}
