package com.smartpantry.manager.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.smartpantry.manager.R;
import com.smartpantry.manager.model.PantryItem;
import com.smartpantry.manager.repository.PantryRepository;

import java.util.Calendar;

public class AddEditItemActivity extends AppCompatActivity {

    private PantryRepository repository;
    private PantryItem editItem = null; // null = add mode

    private TextInputLayout tilName, tilQuantity;
    private TextInputEditText etName, etQuantity, etExpiryDate, etBarcode, etNotes;
    private AutoCompleteTextView actvCategory, actvUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_item);

        repository = PantryRepository.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Bind views
        tilName = findViewById(R.id.tilName);
        tilQuantity = findViewById(R.id.tilQuantity);
        etName = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        etBarcode = findViewById(R.id.etBarcode);
        etNotes = findViewById(R.id.etNotes);
        actvCategory = findViewById(R.id.actvCategory);
        actvUnit = findViewById(R.id.actvUnit);

        // Category dropdown
        String[] categories = getResources().getStringArray(R.array.default_categories);
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
        actvCategory.setAdapter(catAdapter);

        // Unit dropdown
        String[] units = getResources().getStringArray(R.array.default_units);
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, units);
        actvUnit.setAdapter(unitAdapter);

        // Date picker
        TextInputLayout tilExpiry = findViewById(R.id.tilExpiryDate);
        tilExpiry.setEndIconOnClickListener(v -> showDatePicker());
        etExpiryDate.setOnClickListener(v -> showDatePicker());

        // Check if editing
        long itemId = getIntent().getLongExtra(MainActivity.EXTRA_ITEM_ID, -1);
        if (itemId != -1) {
            editItem = repository.getById(itemId);
            if (editItem != null) populateFields(editItem);
        }

        // Title
        boolean isEdit = editItem != null;
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isEdit
                    ? getString(R.string.edit_item_title)
                    : getString(R.string.add_item_title));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        MaterialButton btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> attemptSave());
    }

    private void populateFields(PantryItem item) {
        etName.setText(item.getName());
        actvCategory.setText(item.getCategory(), false);
        double qty = item.getQuantity();
        etQuantity.setText(qty % 1 == 0
                ? String.valueOf((int) qty)
                : String.valueOf(qty));
        actvUnit.setText(item.getUnit(), false);
        etExpiryDate.setText(item.getExpiryDate() != null ? item.getExpiryDate() : "");
        etBarcode.setText(item.getBarcode() != null ? item.getBarcode() : "");
        etNotes.setText(item.getNotes() != null ? item.getNotes() : "");
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        // Pre-fill from existing text if valid
        String existing = getStr(etExpiryDate);
        if (existing.matches("\\d{4}-\\d{2}-\\d{2}")) {
            String[] parts = existing.split("-");
            cal.set(Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]) - 1,
                    Integer.parseInt(parts[2]));
        }
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String date = String.format(java.util.Locale.US,
                            "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    etExpiryDate.setText(date);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void attemptSave() {
        boolean valid = true;

        String name = getStr(etName);
        if (TextUtils.isEmpty(name)) {
            tilName.setError(getString(R.string.field_required));
            valid = false;
        } else {
            tilName.setError(null);
        }

        double qty = 0;
        String qtyStr = getStr(etQuantity);
        if (!qtyStr.isEmpty()) {
            try {
                qty = Double.parseDouble(qtyStr);
                tilQuantity.setError(null);
            } catch (NumberFormatException e) {
                tilQuantity.setError(getString(R.string.invalid_quantity));
                valid = false;
            }
        }

        if (!valid) return;

        PantryItem item = editItem != null ? editItem : new PantryItem();
        item.setName(name);
        item.setCategory(getStr(actvCategory));
        item.setQuantity(qty);
        item.setUnit(getStr(actvUnit));
        item.setExpiryDate(getStr(etExpiryDate));
        item.setBarcode(getStr(etBarcode));
        item.setNotes(getStr(etNotes));
        item.setUpdatedAt(System.currentTimeMillis());

        if (editItem != null) {
            repository.update(item);
            Snackbar.make(etName, getString(R.string.item_updated), Snackbar.LENGTH_SHORT).show();
        } else {
            repository.insert(item);
            Snackbar.make(etName, getString(R.string.item_saved), Snackbar.LENGTH_SHORT).show();
        }
        setResult(RESULT_OK);
        finish();
    }

    private String getStr(android.widget.EditText et) {
        if (et.getText() == null) return "";
        return et.getText().toString().trim();
    }
}
