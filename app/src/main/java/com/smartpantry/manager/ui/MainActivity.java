package com.smartpantry.manager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.smartpantry.manager.R;
import com.smartpantry.manager.adapter.PantryAdapter;
import com.smartpantry.manager.model.PantryItem;
import com.smartpantry.manager.repository.PantryRepository;
import com.smartpantry.manager.util.NotificationHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PantryAdapter.OnItemClickListener {

    public static final int REQUEST_ADD = 1001;
    public static final int REQUEST_EDIT = 1002;
    public static final String EXTRA_ITEM_ID = "item_id";

    private PantryRepository repository;
    private PantryAdapter adapter;
    private Handler mainHandler;

    private RecyclerView recyclerView;
    private View emptyState;
    private TextView tvTotalItems, tvExpiringSoon, tvExpired;
    private ChipGroup chipGroupCategories;
    private TextInputEditText etSearch;

    private List<PantryItem> allItems = new ArrayList<>();
    private String selectedCategory = null; // null = "All"
    private String searchQuery = "";
    private SortOrder sortOrder = SortOrder.NAME;
    private boolean showExpiredOnly = false;

    public enum SortOrder { NAME, EXPIRY, ADDED }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = PantryRepository.getInstance(this);
        mainHandler = new Handler(Looper.getMainLooper());

        NotificationHelper.createNotificationChannel(this);
        NotificationHelper.scheduleDailyCheck(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);
        emptyState = findViewById(R.id.emptyState);
        tvTotalItems = findViewById(R.id.tvTotalItems);
        tvExpiringSoon = findViewById(R.id.tvExpiringSoon);
        tvExpired = findViewById(R.id.tvExpired);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        etSearch = findViewById(R.id.etSearch);

        adapter = new PantryAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> openAddItem());

        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_pantry);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                return true; // already here
            } else if (id == R.id.nav_recipes) {
                startActivity(new Intent(this, SuggestedRecipesActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        repository.getAllAsync(mainHandler, items -> {
            allItems = items;
            buildCategoryChips();
            applyFilters();
            updateSummary();
        });
    }

    private void buildCategoryChips() {
        chipGroupCategories.removeAllViews();

        // "All" chip
        Chip chipAll = new Chip(this);
        chipAll.setText(getString(R.string.all_categories));
        chipAll.setCheckable(true);
        chipAll.setChecked(selectedCategory == null);
        chipAll.setOnClickListener(v -> {
            selectedCategory = null;
            applyFilters();
        });
        chipGroupCategories.addView(chipAll);

        // One chip per distinct category from loaded items
        List<String> cats = new ArrayList<>();
        for (PantryItem item : allItems) {
            String c = item.getCategory();
            if (c != null && !c.isEmpty() && !cats.contains(c)) cats.add(c);
        }
        java.util.Collections.sort(cats);

        for (String cat : cats) {
            Chip chip = new Chip(this);
            chip.setText(cat);
            chip.setCheckable(true);
            chip.setChecked(cat.equals(selectedCategory));
            chip.setOnClickListener(v -> {
                selectedCategory = cat;
                applyFilters();
            });
            chipGroupCategories.addView(chip);
        }
    }

    private void applyFilters() {
        List<PantryItem> filtered = new ArrayList<>();
        for (PantryItem item : allItems) {
            if (showExpiredOnly && !item.isExpired()) continue;
            if (selectedCategory != null && !selectedCategory.equals(item.getCategory())) continue;
            if (!searchQuery.isEmpty()
                    && !item.getName().toLowerCase().contains(searchQuery.toLowerCase())) continue;
            filtered.add(item);
        }
        // Sort
        Comparator<PantryItem> comparator;
        switch (sortOrder) {
            case EXPIRY:
                comparator = (a, b) -> {
                    int da = a.daysUntilExpiry(), db = b.daysUntilExpiry();
                    return Integer.compare(da, db);
                };
                break;
            case ADDED:
                comparator = (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt());
                break;
            default:
                comparator = (a, b) -> a.getName().compareToIgnoreCase(b.getName());
                break;
        }
        filtered.sort(comparator);
        adapter.submitList(filtered);
        emptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void updateSummary() {
        tvTotalItems.setText(String.valueOf(allItems.size()));
        int expiringSoon = 0, expired = 0;
        for (PantryItem item : allItems) {
            if (item.isExpired()) expired++;
            else if (item.isExpiringSoon(7)) expiringSoon++;
        }
        tvExpiringSoon.setText(String.valueOf(expiringSoon));
        tvExpired.setText(String.valueOf(expired));
    }

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------

    private void openAddItem() {
        Intent intent = new Intent(this, AddEditItemActivity.class);
        startActivityForResult(intent, REQUEST_ADD);
    }

    @Override
    public void onItemClick(PantryItem item) {
        Intent intent = new Intent(this, ItemDetailActivity.class);
        intent.putExtra(EXTRA_ITEM_ID, item.getId());
        startActivityForResult(intent, REQUEST_EDIT);
    }

    @Override
    public void onItemLongClick(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_title))
                .setMessage(getString(R.string.delete_message, item.getName()))
                .setPositiveButton(R.string.delete, (d, w) -> deleteItem(item))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteItem(PantryItem item) {
        repository.deleteAsync(item.getId(), mainHandler, rows -> {
            allItems.remove(item);
            applyFilters();
            updateSummary();
            Snackbar.make(recyclerView, getString(R.string.item_deleted), Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo, v -> {
                        repository.insertAsync(item, mainHandler, id -> {
                            item.setId(id);
                            allItems.add(item);
                            applyFilters();
                            updateSummary();
                        });
                    }).show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadData();
        }
    }

    // -------------------------------------------------------------------------
    // Menu
    // -------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sort) {
            showSortDialog();
            return true;
        } else if (id == R.id.action_filter_expired) {
            showExpiredOnly = !showExpiredOnly;
            item.setChecked(showExpiredOnly);
            applyFilters();
            return true;
        } else if (id == R.id.action_clear_expired) {
            confirmClearExpired();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSortDialog() {
        String[] options = {
                getString(R.string.menu_sort_name),
                getString(R.string.menu_sort_expiry),
                getString(R.string.menu_sort_added)
        };
        new AlertDialog.Builder(this)
                .setTitle(R.string.menu_sort)
                .setItems(options, (d, which) -> {
                    sortOrder = SortOrder.values()[which];
                    applyFilters();
                })
                .show();
    }

    private void confirmClearExpired() {
        List<PantryItem> expired = new ArrayList<>();
        for (PantryItem item : allItems) {
            if (item.isExpired()) expired.add(item);
        }
        if (expired.isEmpty()) {
            Snackbar.make(recyclerView, "No expired items to remove", Snackbar.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Clear Expired Items")
                .setMessage("Remove all " + expired.size() + " expired item(s)?")
                .setPositiveButton(R.string.delete, (d, w) -> {
                    for (PantryItem e : expired) {
                        repository.delete(e.getId());
                        allItems.remove(e);
                    }
                    applyFilters();
                    updateSummary();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
