package com.smartpantry.manager.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.smartpantry.manager.R;
import com.smartpantry.manager.model.PantryItem;
import com.smartpantry.manager.repository.PantryRepository;

import java.util.List;

public class ExpiryNotificationReceiver extends BroadcastReceiver {

    private static final int EXPIRY_NOTIF_ID_BASE = 2000;

    @Override
    public void onReceive(Context context, Intent intent) {
        PantryRepository repo = PantryRepository.getInstance(context);

        // Items expiring within 3 days
        List<PantryItem> soon = repo.getItemsExpiringSoon(3);
        // Items already expired
        List<PantryItem> expired = repo.getExpiredItems();

        int total = soon.size() + expired.size();
        if (total == 0) return;

        String title = context.getString(R.string.notif_title);
        String message;

        if (total == 1) {
            PantryItem item = soon.isEmpty() ? expired.get(0) : soon.get(0);
            message = context.getString(R.string.notif_text_single, item.getName());
        } else {
            message = context.getString(R.string.notif_text_multiple, total);
        }

        NotificationHelper.showExpiryNotification(context, title, message, EXPIRY_NOTIF_ID_BASE);
    }
}
