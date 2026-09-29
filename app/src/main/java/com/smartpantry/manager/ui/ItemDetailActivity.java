package com.smartpantry.manager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.smartpantry.manager.R;
import com.smartpantry.manager.model.PantryItem;
import com.smartpantry.manager.repository.PantryRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ItemDetailActivity extends AppCompatActivity {

    private PantryRepository repository;
    private PantryItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        repository = PantryRepository.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        long itemId = getIntent().getLongExtra(MainActivity.EXTRA_ITEM_ID, -1);
        if (itemId == -1) { finish(); return; }

        item = repository.getById(itemId);
        if (item == null) { finish(); return; }

        bindViews();

        findViewById(R.id.btnEdit).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditItemActivity.class);
            intent.putExtra(MainActivity.EXTRA_ITEM_ID, item.getId());
            startActivityForResult(intent, MainActivity.REQUEST_EDIT);
        });

        findViewById(R.id.btnDelete).setOnClickListener(v -> confirmDelete());
    }

    private void bindViews() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(item.getName());
        }

        TextView tvName = findViewById(R.id.tvName);
        TextView tvCategory = findViewById(R.id.tvCategory);
        MaterialCardView cardBanner = findViewById(R.id.cardExpiryBanner);
        TextView tvBanner = findViewById(R.id.tvExpiryBanner);
        MaterialCardView cardNotes = findViewById(R.id.cardNotes);
        TextView tvNotes = findViewById(R.id.tvNotes);

        tvName.setText(item.getName());

        String cat = item.getCategory();
        if (cat != null && !cat.isEmpty()) {
            tvCategory.setText(cat);
            tvCategory.setVisibility(View.VISIBLE);
        } else {
            tvCategory.setVisibility(View.GONE);
        }

        // Expiry banner
        int days = item.daysUntilExpiry();
        if (days < 0) {
            cardBanner.setVisibility(View.VISIBLE);
            cardBanner.setCardBackgroundColor(0xFFC62828);
            tvBanner.setText(getString(R.string.expired_label));
        } else if (days == 0) {
            cardBanner.setVisibility(View.VISIBLE);
            cardBanner.setCardBackgroundColor(0xFFBF360C);
            tvBanner.setText(getString(R.string.expires_today));
        } else if (days <= 7) {
            cardBanner.setVisibility(View.VISIBLE);
            cardBanner.setCardBackgroundColor(0xFFFF8F00);
            tvBanner.setText(getString(R.string.expires_in_days, days));
        } else {
            cardBanner.setVisibility(View.GONE);
        }

        // Detail rows
        setRow(R.id.rowQuantity, getString(R.string.label_quantity),
                formatQuantity(item));
        setRow(R.id.rowExpiry, getString(R.string.label_expiry),
                item.getExpiryDate() != null && !item.getExpiryDate().isEmpty()
                        ? item.getExpiryDate()
                        : getString(R.string.none));
        setRow(R.id.rowBarcode, getString(R.string.label_barcode),
                item.getBarcode() != null && !item.getBarcode().isEmpty()
                        ? item.getBarcode()
                        : getString(R.string.none));
        setRow(R.id.rowAdded, getString(R.string.label_added),
                new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(new Date(item.getCreatedAt())));

        // Notes
        if (item.getNotes() != null && !item.getNotes().isEmpty()) {
            cardNotes.setVisibility(View.VISIBLE);
            tvNotes.setText(item.getNotes());
        }
    }

    private void setRow(int rowId, String label, String value) {
        View row = findViewById(rowId);
        if (row == null) return;
        TextView tvLabel = row.findViewById(R.id.tvLabel);
        TextView tvValue = row.findViewById(R.id.tvValue);
        tvLabel.setText(label);
        tvValue.setText(value);
    }

    private String formatQuantity(PantryItem item) {
        double qty = item.getQuantity();
        String qtyStr = qty % 1 == 0 ? String.valueOf((int) qty) : String.valueOf(qty);
        String unit = item.getUnit();
        return unit != null && !unit.isEmpty() ? qtyStr + " " + unit : qtyStr;
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, item.getName()))
                .setPositiveButton(R.string.delete, (d, w) -> {
                    repository.delete(item.getId());
                    setResult(RESULT_OK);
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // Reload item details after edit
            item = repository.getById(item.getId());
            if (item == null) {
                setResult(RESULT_OK);
                finish();
            } else {
                bindViews();
                setResult(RESULT_OK);
            }
        }
    }
}
