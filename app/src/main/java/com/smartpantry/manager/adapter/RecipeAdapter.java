package com.smartpantry.manager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.smartpantry.manager.R;
import com.smartpantry.manager.model.Recipe;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private List<Recipe> recipes = new ArrayList<>();
    private final OnRecipeClickListener listener;

    public RecipeAdapter(OnRecipeClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(recipes.get(position), listener);
    }

    @Override
    public int getItemCount() { return recipes.size(); }

    public void submitList(List<Recipe> newList) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return recipes.size(); }
            @Override public int getNewListSize() { return newList.size(); }
            @Override public boolean areItemsTheSame(int op, int np) {
                return recipes.get(op).getId() == newList.get(np).getId();
            }
            @Override public boolean areContentsTheSame(int op, int np) {
                return recipes.get(op).getName().equals(newList.get(np).getName());
            }
        });
        recipes = new ArrayList<>(newList);
        result.dispatchUpdatesTo(this);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView tvName, tvDescription, tvMeta, tvIngCount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardRecipe);
            tvName = itemView.findViewById(R.id.tvRecipeName);
            tvDescription = itemView.findViewById(R.id.tvRecipeDescription);
            tvMeta = itemView.findViewById(R.id.tvRecipeMeta);
            tvIngCount = itemView.findViewById(R.id.tvIngredientCount);
        }

        void bind(Recipe recipe, OnRecipeClickListener listener) {
            tvName.setText(recipe.getName());
            tvDescription.setText(recipe.getDescription());
            String meta = recipe.getCategory() + "  ·  " + recipe.getPrepTimeMinutes() + " min";
            tvMeta.setText(meta);
            tvIngCount.setText(recipe.getIngredientCount() + " ingredients");
            card.setOnClickListener(v -> { if (listener != null) listener.onRecipeClick(recipe); });
        }
    }
}
