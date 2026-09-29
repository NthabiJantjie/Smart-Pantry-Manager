package com.smartpantry.manager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "smart_pantry.db";
    public static final int DATABASE_VERSION = 2;

    // ---- Table: pantry_items ----
    public static final String TABLE_ITEMS        = "pantry_items";
    public static final String COL_ID             = "_id";
    public static final String COL_NAME           = "name";
    public static final String COL_CATEGORY       = "category";
    public static final String COL_QUANTITY       = "quantity";
    public static final String COL_UNIT           = "unit";
    public static final String COL_EXPIRY_DATE    = "expiry_date";
    public static final String COL_NOTES          = "notes";
    public static final String COL_BARCODE        = "barcode";
    public static final String COL_CREATED_AT     = "created_at";
    public static final String COL_UPDATED_AT     = "updated_at";

    // ---- Table: recipes ----
    public static final String TABLE_RECIPES       = "recipes";
    public static final String COL_R_ID            = "_id";
    public static final String COL_R_NAME          = "name";
    public static final String COL_R_DESCRIPTION   = "description";
    public static final String COL_R_PREP_STEPS    = "prep_steps";
    public static final String COL_R_PREP_TIME     = "prep_time_minutes";
    public static final String COL_R_CATEGORY      = "category";
    public static final String COL_R_CREATED_AT    = "created_at";

    // ---- Table: recipe_ingredients ----
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RI_ID           = "_id";
    public static final String COL_RI_RECIPE_ID    = "recipe_id";
    public static final String COL_RI_NAME         = "ingredient_name";
    public static final String COL_RI_QUANTITY     = "required_quantity";
    public static final String COL_RI_UNIT         = "unit";

    // ---- CREATE statements ----
    private static final String CREATE_TABLE_ITEMS =
            "CREATE TABLE " + TABLE_ITEMS + " (" +
            COL_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_NAME        + " TEXT NOT NULL, " +
            COL_CATEGORY    + " TEXT, " +
            COL_QUANTITY    + " REAL NOT NULL DEFAULT 0, " +
            COL_UNIT        + " TEXT, " +
            COL_EXPIRY_DATE + " TEXT, " +
            COL_NOTES       + " TEXT, " +
            COL_BARCODE     + " TEXT, " +
            COL_CREATED_AT  + " INTEGER NOT NULL, " +
            COL_UPDATED_AT  + " INTEGER NOT NULL);";

    private static final String CREATE_TABLE_RECIPES =
            "CREATE TABLE " + TABLE_RECIPES + " (" +
            COL_R_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_R_NAME        + " TEXT NOT NULL, " +
            COL_R_DESCRIPTION + " TEXT, " +
            COL_R_PREP_STEPS  + " TEXT, " +
            COL_R_PREP_TIME   + " INTEGER DEFAULT 0, " +
            COL_R_CATEGORY    + " TEXT, " +
            COL_R_CREATED_AT  + " INTEGER NOT NULL);";

    private static final String CREATE_TABLE_RECIPE_INGREDIENTS =
            "CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
            COL_RI_ID        + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_RI_RECIPE_ID + " INTEGER NOT NULL REFERENCES " + TABLE_RECIPES + "(" + COL_R_ID + ") ON DELETE CASCADE, " +
            COL_RI_NAME      + " TEXT NOT NULL, " +
            COL_RI_QUANTITY  + " REAL NOT NULL DEFAULT 0, " +
            COL_RI_UNIT      + " TEXT);";

    private static volatile DatabaseHelper instance;

    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                if (instance == null) {
                    instance = new DatabaseHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ITEMS);
        db.execSQL(CREATE_TABLE_RECIPES);
        db.execSQL(CREATE_TABLE_RECIPE_INGREDIENTS);
        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // -------------------------------------------------------------------------
    // SEED DATA — 20 recipes
    // -------------------------------------------------------------------------
    private void seedRecipes(SQLiteDatabase db) {
        long now = System.currentTimeMillis();

        // Helper lambdas replaced with method calls for Java 8 compatibility
        insertRecipe(db, now, "Classic Omelette",
                "A quick protein-rich breakfast using pantry staples.",
                "1. Beat eggs with salt and pepper.\n2. Melt butter in a non-stick pan over medium heat.\n3. Pour in eggs; when edges set, add cheese.\n4. Fold omelette in half and serve hot.",
                10, "Breakfast",
                new String[]{"egg", "butter", "cheese", "salt", "pepper"},
                new double[]{3, 15, 30, 2, 1},
                new String[]{"pcs", "g", "g", "g", "g"});

        insertRecipe(db, now, "Scrambled Eggs on Toast",
                "Simple breakfast ready in minutes.",
                "1. Toast bread slices.\n2. Beat eggs with milk, salt and pepper.\n3. Scramble in buttered pan over low heat until just set.\n4. Serve on toast.",
                8, "Breakfast",
                new String[]{"egg", "bread", "milk", "butter", "salt"},
                new double[]{2, 2, 30, 10, 1},
                new String[]{"pcs", "slices", "mL", "g", "g"});

        insertRecipe(db, now, "Banana Oat Smoothie",
                "Creamy no-cook breakfast smoothie.",
                "1. Peel and slice banana.\n2. Add banana, oats, milk and honey to blender.\n3. Blend until smooth.\n4. Pour and serve immediately.",
                5, "Breakfast",
                new String[]{"banana", "oat", "milk", "honey"},
                new double[]{1, 50, 200, 15},
                new String[]{"pcs", "g", "mL", "mL"});

        insertRecipe(db, now, "Pasta Aglio e Olio",
                "Classic Italian garlic pasta with minimal ingredients.",
                "1. Cook pasta in salted boiling water until al dente.\n2. Sauté sliced garlic in olive oil until golden.\n3. Add chilli flakes.\n4. Toss drained pasta in the oil.\n5. Season and serve with parsley.",
                20, "Dinner",
                new String[]{"pasta", "garlic", "olive oil", "chilli flake", "parsley", "salt"},
                new double[]{200, 4, 60, 2, 10, 3},
                new String[]{"g", "cloves", "mL", "g", "g", "g"});

        insertRecipe(db, now, "Tomato & Egg Stir-Fry",
                "A Chinese home-style classic, fast and satisfying.",
                "1. Beat eggs and scramble in hot oil; set aside.\n2. Fry garlic, add chopped tomatoes.\n3. Season with salt, sugar and soy sauce.\n4. Return eggs to pan, stir to combine.\n5. Serve over rice.",
                15, "Lunch",
                new String[]{"egg", "tomato", "garlic", "oil", "salt", "sugar", "soy sauce", "rice"},
                new double[]{3, 2, 2, 30, 3, 5, 15, 150},
                new String[]{"pcs", "pcs", "cloves", "mL", "g", "g", "mL", "g"});

        insertRecipe(db, now, "Fried Rice",
                "Turn leftover rice into a complete meal.",
                "1. Heat oil in wok on high heat.\n2. Add garlic and stir-fry 30 seconds.\n3. Push aside; scramble egg.\n4. Add cooked rice; stir-fry 2 min.\n5. Season with soy sauce and salt.",
                15, "Lunch",
                new String[]{"cooked rice", "egg", "garlic", "oil", "soy sauce", "salt"},
                new double[]{300, 2, 2, 30, 20, 2},
                new String[]{"g", "pcs", "cloves", "mL", "mL", "g"});

        insertRecipe(db, now, "Pancakes",
                "Fluffy weekend breakfast pancakes.",
                "1. Mix flour, baking powder, sugar and salt.\n2. Whisk in egg, milk and melted butter.\n3. Cook ladles of batter in a buttered pan until bubbles form; flip.\n4. Serve with honey or jam.",
                20, "Breakfast",
                new String[]{"flour", "egg", "milk", "butter", "baking powder", "sugar", "salt"},
                new double[]{200, 1, 250, 30, 8, 20, 2},
                new String[]{"g", "pcs", "mL", "g", "g", "g", "g"});

        insertRecipe(db, now, "Grilled Cheese Sandwich",
                "Melty comfort food in 10 minutes.",
                "1. Butter one side of each bread slice.\n2. Place cheese between unbuttered sides.\n3. Grill in pan butter-side-down until golden.\n4. Flip and grill other side.\n5. Serve hot.",
                10, "Lunch",
                new String[]{"bread", "cheese", "butter"},
                new double[]{2, 60, 20},
                new String[]{"slices", "g", "g"});

        insertRecipe(db, now, "Vegetable Stir-Fry",
                "Quick healthy veggie dish with whatever you have.",
                "1. Heat oil in wok.\n2. Add garlic and onion; stir 1 min.\n3. Add carrot and capsicum; stir-fry 3 min.\n4. Add soy sauce and sesame oil.\n5. Serve over rice or noodles.",
                15, "Dinner",
                new String[]{"carrot", "capsicum", "onion", "garlic", "soy sauce", "oil", "sesame oil"},
                new double[]{2, 1, 1, 2, 30, 20, 10},
                new String[]{"pcs", "pcs", "pcs", "cloves", "mL", "mL", "mL"});

        insertRecipe(db, now, "Tuna Pasta Bake",
                "Hearty baked pasta, pantry-friendly.",
                "1. Cook pasta; drain.\n2. Mix tuna, corn, and cream of mushroom soup.\n3. Combine with pasta.\n4. Top with cheese.\n5. Bake at 180°C for 20 min until golden.",
                40, "Dinner",
                new String[]{"pasta", "tuna", "corn", "cream of mushroom soup", "cheese"},
                new double[]{200, 185, 200, 400, 80},
                new String[]{"g", "g", "g", "g", "g"});

        insertRecipe(db, now, "Bean & Tomato Soup",
                "Filling, nutritious one-pot soup.",
                "1. Sauté onion and garlic in oil.\n2. Add canned tomatoes and beans.\n3. Add stock and simmer 15 min.\n4. Season with salt, pepper and cumin.\n5. Serve with bread.",
                25, "Lunch",
                new String[]{"canned tomato", "canned bean", "onion", "garlic", "vegetable stock", "oil", "cumin", "salt"},
                new double[]{400, 400, 1, 2, 500, 20, 3, 3},
                new String[]{"g", "g", "pcs", "cloves", "mL", "mL", "g", "g"});

        insertRecipe(db, now, "Banana Pancakes (2-ingredient)",
                "Healthy gluten-free pancakes with just banana and egg.",
                "1. Mash ripe banana in a bowl.\n2. Beat in eggs until combined.\n3. Cook small rounds in a non-stick pan over medium-low heat.\n4. Flip when set on the bottom.\n5. Serve with fruit.",
                10, "Breakfast",
                new String[]{"banana", "egg"},
                new double[]{2, 2},
                new String[]{"pcs", "pcs"});

        insertRecipe(db, now, "Garlic Butter Noodles",
                "Buttery, garlicky noodles in under 15 minutes.",
                "1. Cook noodles per packet; drain, keep ¼ cup water.\n2. Melt butter in pan; add minced garlic.\n3. Toss noodles with garlic butter.\n4. Add pasta water for silkiness.\n5. Season with salt, pepper and parsley.",
                15, "Dinner",
                new String[]{"noodle", "butter", "garlic", "salt", "pepper", "parsley"},
                new double[]{200, 40, 3, 2, 1, 5},
                new String[]{"g", "g", "cloves", "g", "g", "g"});

        insertRecipe(db, now, "Rice Porridge (Congee)",
                "Comforting rice porridge, easy on the stomach.",
                "1. Rinse rice.\n2. Bring water or stock to boil.\n3. Add rice; simmer 30-40 min stirring occasionally.\n4. Season with salt and soy sauce.\n5. Top with spring onion.",
                45, "Breakfast",
                new String[]{"rice", "water", "salt", "soy sauce", "spring onion"},
                new double[]{150, 1200, 3, 15, 20},
                new String[]{"g", "mL", "g", "mL", "g"});

        insertRecipe(db, now, "French Toast",
                "Eggy bread, golden and slightly sweet.",
                "1. Whisk eggs, milk, sugar and cinnamon.\n2. Dip bread slices in mixture.\n3. Fry in buttered pan until golden on both sides.\n4. Serve with honey.",
                12, "Breakfast",
                new String[]{"bread", "egg", "milk", "sugar", "cinnamon", "butter", "honey"},
                new double[]{2, 2, 60, 15, 2, 15, 20},
                new String[]{"slices", "pcs", "mL", "g", "g", "g", "mL"});

        insertRecipe(db, now, "Potato & Onion Frittata",
                "Hearty Italian baked egg dish.",
                "1. Boil potato until just tender; slice.\n2. Sauté onion in oil until soft.\n3. Add potato slices.\n4. Pour beaten seasoned eggs over everything.\n5. Cook on hob until edges set, then finish under grill.",
                30, "Lunch",
                new String[]{"potato", "onion", "egg", "oil", "salt", "pepper"},
                new double[]{2, 1, 4, 30, 3, 1},
                new String[]{"pcs", "pcs", "pcs", "mL", "g", "g"});

        insertRecipe(db, now, "Cucumber Yoghurt Salad",
                "Cooling tzatziki-style side dish.",
                "1. Grate or dice cucumber; salt and leave 5 min, then squeeze dry.\n2. Mix with yoghurt, garlic, olive oil and salt.\n3. Season with pepper.\n4. Chill and serve.",
                10, "Snack",
                new String[]{"cucumber", "yoghurt", "garlic", "olive oil", "salt", "pepper"},
                new double[]{1, 200, 1, 15, 2, 1},
                new String[]{"pcs", "g", "cloves", "mL", "g", "g"});

        insertRecipe(db, now, "Lemon Rice",
                "Fragrant South Indian-style lemony rice.",
                "1. Heat oil; add mustard seeds until they pop.\n2. Add turmeric and cooked rice.\n3. Stir-fry 2 min.\n4. Squeeze lemon juice over rice.\n5. Season with salt.",
                10, "Lunch",
                new String[]{"cooked rice", "lemon", "oil", "mustard seed", "turmeric", "salt"},
                new double[]{300, 1, 20, 5, 2, 3},
                new String[]{"g", "pcs", "mL", "g", "g", "g"});

        insertRecipe(db, now, "Avocado Toast",
                "Trendy, filling and nutritious breakfast.",
                "1. Toast bread.\n2. Mash avocado with lemon juice, salt and pepper.\n3. Spread on toast.\n4. Top with chilli flakes.",
                8, "Breakfast",
                new String[]{"bread", "avocado", "lemon", "salt", "pepper", "chilli flake"},
                new double[]{2, 1, 1, 2, 1, 1},
                new String[]{"slices", "pcs", "pcs", "g", "g", "g"});

        insertRecipe(db, now, "Chickpea Curry",
                "Hearty vegan curry using pantry staples.",
                "1. Sauté onion and garlic in oil.\n2. Add curry powder and stir 1 min.\n3. Add canned tomatoes and chickpeas.\n4. Simmer 20 min.\n5. Season with salt; serve with rice or bread.",
                30, "Dinner",
                new String[]{"canned chickpea", "canned tomato", "onion", "garlic", "curry powder", "oil", "salt"},
                new double[]{400, 400, 1, 2, 15, 20, 3},
                new String[]{"g", "g", "pcs", "cloves", "g", "mL", "g"});
    }

    private void insertRecipe(SQLiteDatabase db, long now,
                               String name, String desc, String steps, int prepTime, String cat,
                               String[] ingNames, double[] ingQtys, String[] ingUnits) {
        ContentValues rv = new ContentValues();
        rv.put(COL_R_NAME, name);
        rv.put(COL_R_DESCRIPTION, desc);
        rv.put(COL_R_PREP_STEPS, steps);
        rv.put(COL_R_PREP_TIME, prepTime);
        rv.put(COL_R_CATEGORY, cat);
        rv.put(COL_R_CREATED_AT, now);
        long recipeId = db.insert(TABLE_RECIPES, null, rv);

        for (int i = 0; i < ingNames.length; i++) {
            ContentValues iv = new ContentValues();
            iv.put(COL_RI_RECIPE_ID, recipeId);
            iv.put(COL_RI_NAME, ingNames[i]);
            iv.put(COL_RI_QUANTITY, ingQtys[i]);
            iv.put(COL_RI_UNIT, i < ingUnits.length ? ingUnits[i] : "");
            db.insert(TABLE_RECIPE_INGREDIENTS, null, iv);
        }
    }
}
