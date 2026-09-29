package com.smartpantry.manager.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.smartpantry.manager.R;
import com.smartpantry.manager.database.DatabaseHelper;
import com.smartpantry.manager.database.RecipeDao;
import com.smartpantry.manager.model.Recipe;
import com.smartpantry.manager.model.RecipeIngredient;

public class RecipeDetailActivity extends AppCompatActivity {

    private RecipeDao recipeDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        recipeDao = new RecipeDao(DatabaseHelper.getInstance(this));

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        long recipeId = getIntent().getLongExtra(SuggestedRecipesActivity.EXTRA_RECIPE_ID, -1);
        if (recipeId == -1) { finish(); return; }

        Recipe recipe = recipeDao.getById(recipeId);
        if (recipe == null) { finish(); return; }

        bindViews(recipe);
    }

    private void bindViews(Recipe recipe) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(recipe.getName());
        }

        TextView tvName = findViewById(R.id.tvRecipeDetailName);
        TextView tvCategory = findViewById(R.id.tvRecipeDetailCategory);
        TextView tvPrepTime = findViewById(R.id.tvRecipeDetailPrepTime);
        TextView tvDescription = findViewById(R.id.tvRecipeDetailDescription);
        TextView tvIngredients = findViewById(R.id.tvRecipeDetailIngredients);
        TextView tvSteps = findViewById(R.id.tvRecipeDetailSteps);

        tvName.setText(recipe.getName());
        tvCategory.setText(recipe.getCategory());

        String prepTime = recipe.getPrepTimeMinutes() > 0
                ? recipe.getPrepTimeMinutes() + " min"
                : getString(R.string.none);
        tvPrepTime.setText(getString(R.string.prep_time_label) + " " + prepTime);

        tvDescription.setText(recipe.getDescription());

        // Build ingredient list
        StringBuilder ingBuilder = new StringBuilder();
        for (RecipeIngredient ri : recipe.getIngredients()) {
            ingBuilder.append("• ").append(ri.getDisplayString()).append("\n");
        }
        tvIngredients.setText(ingBuilder.toString().trim());

        // Prep steps — already newline separated
        if (recipe.getPrepSteps() != null && !recipe.getPrepSteps().isEmpty()) {
            tvSteps.setText(recipe.getPrepSteps());
        } else {
            tvSteps.setText(getString(R.string.none));
        }
    }
}
