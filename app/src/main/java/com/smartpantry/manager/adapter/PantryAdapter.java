package com.smartpantry.manager.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.smartpantry.manager.R;
import com.smartpantry.manager.model.PantryItem;

import java.util.ArrayList;
import java.util.List;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(PantryItem item);
        void onItemLongClick(PantryItem item);
    }

    private List<PantryItem> items = new ArrayList<>();
    private OnItemClickListener listener;

    private static final int[] CATEGORY_COLORS = {
            0xFF1B5E20, 0xFF0D47A1, 0xFF4A148C, 0xFFBF360C,
            0xFFF57F17, 0xFF006064, 0xFF880E4F, 0xFF33691E
    };

    public PantryAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PantryItem item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitList(List<PantryItem> newItems) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return items.size(); }
            @Override public int getNewListSize() { return newItems.size(); }
            @Override public boolean areItemsTheSame(int op, int np) {
                return items.get(op).getId() == newItems.get(np).getId();
            }
            @Override public boolean areContentsTheSame(int op, int np) {
                PantryItem o = items.get(op), n = newItems.get(np);
                return o.getName().equals(n.getName())
                        && o.getQuantity() == n.getQuantity()
                        && eq(o.getExpiryDate(), n.getExpiryDate())
                        && eq(o.getCategory(), n.getCategory());
            }
            private boolean eq(String a, String b) {
                if (a == null && b == null) return true;
                if (a == null || b == null) return false;
                return a.equals(b);
            }
        });
        items = new ArrayList<>(newItems);
        result.dispatchUpdatesTo(this);
    }

    public PantryItem getItem(int position) {
        return items.get(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardItem;
        View viewCategoryColor;
        TextView tvName, tvQuantity, tvCategory, tvExpiryDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardItem = itemView.findViewById(R.id.cardItem);
            viewCategoryColor = itemView.findViewById(R.id.viewCategoryColor);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvQuantity = itemView.findViewById(R.id.tvItemQuantity);
            tvCategory = itemView.findViewById(R.id.tvItemCategory);
            tvExpiryDate = itemView.findViewById(R.id.tvExpiryDate);
        }

        void bind(PantryItem item, OnItemClickListener listener) {
            tvName.setText(item.getName());

            // Quantity + unit
            String qty = item.getQuantity() % 1 == 0
                    ? String.valueOf((int) item.getQuantity())
                    : String.valueOf(item.getQuantity());
            String unit = item.getUnit() != null ? item.getUnit() : "";
            tvQuantity.setText(qty + (unit.isEmpty() ? "" : " " + unit));

            // Category
            String cat = item.getCategory() != null ? item.getCategory() : "Uncategorized";
            tvCategory.setText(cat);

            // Category color indicator
            int colorIndex = Math.abs(cat.hashCode()) % CATEGORY_COLORS.length;
            int color = CATEGORY_COLORS[colorIndex];
            viewCategoryColor.setBackgroundColor(color);

            // Expiry
            int days = item.daysUntilExpiry();
            if (days == Integer.MAX_VALUE || item.getExpiryDate() == null || item.getExpiryDate().isEmpty()) {
                tvExpiryDate.setText(itemView.getContext().getString(R.string.no_expiry));
                tvExpiryDate.setTextColor(0xFF6B6B6B);
                cardItem.setCardBackgroundColor(Color.WHITE);
            } else if (days < 0) {
                tvExpiryDate.setText(itemView.getContext().getString(R.string.expired_label));
                tvExpiryDate.setTextColor(0xFFC62828);
                cardItem.setCardBackgroundColor(0xFFFFF3F3);
            } else if (days == 0) {
                tvExpiryDate.setText(itemView.getContext().getString(R.string.expires_today));
                tvExpiryDate.setTextColor(0xFFBF360C);
                cardItem.setCardBackgroundColor(0xFFFFF8E1);
            } else if (days <= 3) {
                tvExpiryDate.setText(itemView.getContext().getString(R.string.expires_in_days, days));
                tvExpiryDate.setTextColor(0xFFFF8F00);
                cardItem.setCardBackgroundColor(0xFFFFFDE7);
            } else {
                tvExpiryDate.setText(item.getExpiryDate());
                tvExpiryDate.setTextColor(0xFF2E7D32);
                cardItem.setCardBackgroundColor(Color.WHITE);
            }

            cardItem.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });
            cardItem.setOnLongClickListener(v -> {
                if (listener != null) listener.onItemLongClick(item);
                return true;
            });
        }
    }
}
