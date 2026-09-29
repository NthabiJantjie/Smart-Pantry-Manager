package com.smartpantry.manager.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.smartpantry.manager.model.PantryItem;

import java.util.ArrayList;
import java.util.List;

public class PantryItemDao {

    private final DatabaseHelper dbHelper;

    public PantryItemDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // -------------------------------------------------------------------------
    // INSERT
    // -------------------------------------------------------------------------

    public long insert(PantryItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = toContentValues(item);
        long newId = db.insert(DatabaseHelper.TABLE_ITEMS, null, cv);
        item.setId(newId);
        return newId;
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    public int update(PantryItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        item.setUpdatedAt(System.currentTimeMillis());
        ContentValues cv = toContentValues(item);
        return db.update(DatabaseHelper.TABLE_ITEMS, cv,
                DatabaseHelper.COL_ID + "=?",
                new String[]{String.valueOf(item.getId())});
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    public int delete(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_ITEMS,
                DatabaseHelper.COL_ID + "=?",
                new String[]{String.valueOf(id)});
    }

    public int deleteAll() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_ITEMS, null, null);
    }

    // -------------------------------------------------------------------------
    // QUERIES
    // -------------------------------------------------------------------------

    public PantryItem getById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_ITEMS, null,
                DatabaseHelper.COL_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);
        PantryItem item = null;
        if (c.moveToFirst()) {
            item = fromCursor(c);
        }
        c.close();
        return item;
    }

    public List<PantryItem> getAll() {
        return queryItems(null, null, DatabaseHelper.COL_NAME + " ASC");
    }

    public List<PantryItem> getByCategory(String category) {
        return queryItems(DatabaseHelper.COL_CATEGORY + "=?",
                new String[]{category},
                DatabaseHelper.COL_NAME + " ASC");
    }

    public List<PantryItem> searchByName(String query) {
        return queryItems(DatabaseHelper.COL_NAME + " LIKE ?",
                new String[]{"%" + query + "%"},
                DatabaseHelper.COL_NAME + " ASC");
    }

    public List<PantryItem> getExpiredItems() {
        String today = getTodayString();
        return queryItems(
                DatabaseHelper.COL_EXPIRY_DATE + " IS NOT NULL AND " +
                DatabaseHelper.COL_EXPIRY_DATE + " != '' AND " +
                DatabaseHelper.COL_EXPIRY_DATE + " < ?",
                new String[]{today},
                DatabaseHelper.COL_EXPIRY_DATE + " ASC");
    }

    public List<PantryItem> getItemsExpiringSoon(int days) {
        String today = getTodayString();
        String threshold = getDateString(days);
        return queryItems(
                DatabaseHelper.COL_EXPIRY_DATE + " IS NOT NULL AND " +
                DatabaseHelper.COL_EXPIRY_DATE + " != '' AND " +
                DatabaseHelper.COL_EXPIRY_DATE + " >= ? AND " +
                DatabaseHelper.COL_EXPIRY_DATE + " <= ?",
                new String[]{today, threshold},
                DatabaseHelper.COL_EXPIRY_DATE + " ASC");
    }

    public List<String> getAllCategories() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT DISTINCT " + DatabaseHelper.COL_CATEGORY +
                " FROM " + DatabaseHelper.TABLE_ITEMS +
                " WHERE " + DatabaseHelper.COL_CATEGORY + " IS NOT NULL" +
                " ORDER BY " + DatabaseHelper.COL_CATEGORY + " ASC",
                null);
        List<String> categories = new ArrayList<>();
        while (c.moveToNext()) {
            String cat = c.getString(0);
            if (cat != null && !cat.isEmpty()) categories.add(cat);
        }
        c.close();
        return categories;
    }

    public int getCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_ITEMS, null);
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private List<PantryItem> queryItems(String selection, String[] args, String orderBy) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_ITEMS, null, selection, args, null, null, orderBy);
        List<PantryItem> items = new ArrayList<>();
        while (c.moveToNext()) {
            items.add(fromCursor(c));
        }
        c.close();
        return items;
    }

    private ContentValues toContentValues(PantryItem item) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_NAME, item.getName());
        cv.put(DatabaseHelper.COL_CATEGORY, item.getCategory());
        cv.put(DatabaseHelper.COL_QUANTITY, item.getQuantity());
        cv.put(DatabaseHelper.COL_UNIT, item.getUnit());
        cv.put(DatabaseHelper.COL_EXPIRY_DATE, item.getExpiryDate());
        cv.put(DatabaseHelper.COL_NOTES, item.getNotes());
        cv.put(DatabaseHelper.COL_BARCODE, item.getBarcode());
        cv.put(DatabaseHelper.COL_CREATED_AT, item.getCreatedAt());
        cv.put(DatabaseHelper.COL_UPDATED_AT, item.getUpdatedAt());
        return cv;
    }

    private PantryItem fromCursor(Cursor c) {
        PantryItem item = new PantryItem();
        item.setId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_ID)));
        item.setName(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_NAME)));
        item.setCategory(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY)));
        item.setQuantity(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_QUANTITY)));
        item.setUnit(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_UNIT)));
        item.setExpiryDate(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_EXPIRY_DATE)));
        item.setNotes(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_NOTES)));
        item.setBarcode(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_BARCODE)));
        item.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_CREATED_AT)));
        item.setUpdatedAt(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_UPDATED_AT)));
        return item;
    }

    private String getTodayString() {
        return getDateString(0);
    }

    private String getDateString(int daysFromNow) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_YEAR, daysFromNow);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
        return sdf.format(cal.getTime());
    }
}
