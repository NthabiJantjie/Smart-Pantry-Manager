package com.smartpantry.manager.ui;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.smartpantry.manager.R;
import com.smartpantry.manager.util.NotificationHelper;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME        = "pantry_prefs";
    public static final String KEY_NOTIFICATIONS = "expiry_notifications_enabled";
    public static final String KEY_EXPIRY_DAYS   = "expiry_alert_days";
    public static final String KEY_UNITS_METRIC  = "use_metric_units";

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.settings));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Expiry notifications toggle
        SwitchCompat switchNotifications = findViewById(R.id.switchNotifications);
        switchNotifications.setChecked(prefs.getBoolean(KEY_NOTIFICATIONS, true));
        switchNotifications.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(KEY_NOTIFICATIONS, isChecked).apply();
            if (isChecked) {
                NotificationHelper.createNotificationChannel(this);
                NotificationHelper.scheduleDailyCheck(this);
            } else {
                NotificationHelper.cancelDailyCheck(this);
            }
        });

        // Metric units toggle
        SwitchCompat switchMetric = findViewById(R.id.switchMetric);
        switchMetric.setChecked(prefs.getBoolean(KEY_UNITS_METRIC, true));
        switchMetric.setOnCheckedChangeListener((btn, isChecked) ->
                prefs.edit().putBoolean(KEY_UNITS_METRIC, isChecked).apply());

        // Expiry alert days selector (radio group)
        int alertDays = prefs.getInt(KEY_EXPIRY_DAYS, 3);
        switch (alertDays) {
            case 1: ((android.widget.RadioButton) findViewById(R.id.radio1day)).setChecked(true); break;
            case 7: ((android.widget.RadioButton) findViewById(R.id.radio7days)).setChecked(true); break;
            default: ((android.widget.RadioButton) findViewById(R.id.radio3days)).setChecked(true); break;
        }

        android.widget.RadioGroup rgDays = findViewById(R.id.rgExpiryDays);
        rgDays.setOnCheckedChangeListener((group, checkedId) -> {
            int days = 3;
            if (checkedId == R.id.radio1day) days = 1;
            else if (checkedId == R.id.radio7days) days = 7;
            prefs.edit().putInt(KEY_EXPIRY_DAYS, days).apply();
        });
    }
}
