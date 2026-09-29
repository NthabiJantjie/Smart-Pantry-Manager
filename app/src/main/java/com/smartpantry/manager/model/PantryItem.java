package com.smartpantry.manager.model;

public class PantryItem {
    private long id;
    private String name;
    private String category;
    private double quantity;
    private String unit;
    private String expiryDate;   // stored as "yyyy-MM-dd" string
    private String notes;
    private String barcode;
    private long createdAt;      // epoch millis
    private long updatedAt;      // epoch millis

    public PantryItem() {
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public PantryItem(String name, String category, double quantity, String unit, String expiryDate) {
        this();
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    // --- Getters & Setters ---

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Returns days until expiry (negative = already expired).
     * Returns Integer.MAX_VALUE if no expiry date is set.
     */
    public int daysUntilExpiry() {
        if (expiryDate == null || expiryDate.isEmpty()) return Integer.MAX_VALUE;
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
            java.util.Date expiry = sdf.parse(expiryDate);
            if (expiry == null) return Integer.MAX_VALUE;
            long diff = expiry.getTime() - System.currentTimeMillis();
            return (int) (diff / (1000L * 60 * 60 * 24));
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    public boolean isExpired() {
        return daysUntilExpiry() < 0;
    }

    public boolean isExpiringSoon(int daysThreshold) {
        int days = daysUntilExpiry();
        return days >= 0 && days <= daysThreshold;
    }

    @Override
    public String toString() {
        return "PantryItem{id=" + id + ", name='" + name + "', category='" + category
                + "', qty=" + quantity + " " + unit + ", expiry=" + expiryDate + "}";
    }
}
