package com.smartpantry.manager.repository;

import android.content.Context;

import com.smartpantry.manager.database.DatabaseHelper;
import com.smartpantry.manager.database.PantryItemDao;
import com.smartpantry.manager.model.PantryItem;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PantryRepository {

    private static volatile PantryRepository instance;
    private final PantryItemDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface Callback<T> {
        void onResult(T result);
    }

    public static PantryRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (PantryRepository.class) {
                if (instance == null) {
                    instance = new PantryRepository(context);
                }
            }
        }
        return instance;
    }

    private PantryRepository(Context context) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);
        this.dao = new PantryItemDao(db);
    }

    // --- Synchronous ---

    public long insert(PantryItem item) {
        return dao.insert(item);
    }

    public int update(PantryItem item) {
        return dao.update(item);
    }

    public int delete(long id) {
        return dao.delete(id);
    }

    public PantryItem getById(long id) {
        return dao.getById(id);
    }

    public List<PantryItem> getAll() {
        return dao.getAll();
    }

    public List<PantryItem> getByCategory(String category) {
        return dao.getByCategory(category);
    }

    public List<PantryItem> searchByName(String query) {
        return dao.searchByName(query);
    }

    public List<PantryItem> getExpiredItems() {
        return dao.getExpiredItems();
    }

    public List<PantryItem> getItemsExpiringSoon(int days) {
        return dao.getItemsExpiringSoon(days);
    }

    public List<String> getAllCategories() {
        return dao.getAllCategories();
    }

    public int getCount() {
        return dao.getCount();
    }

    // --- Async convenience wrappers ---

    public void getAllAsync(android.os.Handler mainHandler, Callback<List<PantryItem>> callback) {
        executor.execute(() -> {
            List<PantryItem> result = dao.getAll();
            mainHandler.post(() -> callback.onResult(result));
        });
    }

    public void getByCategoryAsync(String category, android.os.Handler mainHandler,
                                   Callback<List<PantryItem>> callback) {
        executor.execute(() -> {
            List<PantryItem> result = dao.getByCategory(category);
            mainHandler.post(() -> callback.onResult(result));
        });
    }

    public void searchByNameAsync(String query, android.os.Handler mainHandler,
                                  Callback<List<PantryItem>> callback) {
        executor.execute(() -> {
            List<PantryItem> result = dao.searchByName(query);
            mainHandler.post(() -> callback.onResult(result));
        });
    }

    public void insertAsync(PantryItem item, android.os.Handler mainHandler,
                            Callback<Long> callback) {
        executor.execute(() -> {
            long id = dao.insert(item);
            if (mainHandler != null && callback != null) {
                mainHandler.post(() -> callback.onResult(id));
            }
        });
    }

    public void updateAsync(PantryItem item, android.os.Handler mainHandler,
                            Callback<Integer> callback) {
        executor.execute(() -> {
            int rows = dao.update(item);
            if (mainHandler != null && callback != null) {
                mainHandler.post(() -> callback.onResult(rows));
            }
        });
    }

    public void deleteAsync(long id, android.os.Handler mainHandler,
                            Callback<Integer> callback) {
        executor.execute(() -> {
            int rows = dao.delete(id);
            if (mainHandler != null && callback != null) {
                mainHandler.post(() -> callback.onResult(rows));
            }
        });
    }
}
