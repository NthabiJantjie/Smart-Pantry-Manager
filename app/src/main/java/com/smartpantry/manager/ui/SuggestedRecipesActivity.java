package com.smartpantry.manager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.smartpantry.manager.R;
import com.smartpantry.manager.adapter.RecipeAdapter;
import com.smartpantry.manager.database.DatabaseHelper;
import com.smartpantry.manager.database.RecipeDao;
import com.smartpantry.manager.model.PantryItem;
import com.smartpantry.manager.model.Recipe;
import com.smartpantry.manager.repository.PantryRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SuggestedRecipesActivity extends AppCompatActivity
        implements RecipeAdapter.OnRecipeClickListener {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    private RecipeAdapter adapter;
    private RecipeDao recipeDao;
    private PantryRepository pantryRepo;
    private Handler mainHandler;
    private ExecutorService executor;

    private RecyclerView recyclerView;
    private View emptyState;
    private TextView tvEmptyMessage;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        mainHandler = new Handler(Looper.getMainLooper());
        executor = Executors.newSingleThreadExecutor();

        recipeDao = new RecipeDao(DatabaseHelper.getInstance(this));
        pantryRepo = PantryRepository.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.suggested_recipes));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewRecipes);
        emptyState = findViewById(R.id.emptyStateRecipes);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        tabLayout = findViewById(R.id.tabLayout);

        adapter = new RecipeAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { loadRecipes(tab.getPosition()); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadRecipes(0); // Default: Suggested
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecipes(tabLayout.getSelectedTabPosition());
    }

    private void loadRecipes(int tabPosition) {
        executor.execute(() -> {
            List<PantryItem> pantry = pantryRepo.getAll();
            List<Recipe> result;
            String emptyMsg;

            if (tabPosition == 0) {
                result = recipeDao.getSuggestedRecipes(pantry);
                emptyMsg = getString(R.string.no_suggested_recipes);
            } else {
                result = recipeDao.getAlmostThereRecipes(pantry);
                emptyMsg = getString(R.string.no_almost_there_recipes);
            }

            final List<Recipe> finalResult = result;
            final String finalMsg = emptyMsg;
            mainHandler.post(() -> {
                adapter.submitList(finalResult);
                boolean empty = finalResult.isEmpty();
                recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
                emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
                tvEmptyMessage.setText(finalMsg);
            });
        });
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
