# Smart Pantry Manager

A Java Android application that helps users reduce food waste by tracking pantry ingredients and suggesting recipes they can cook **strictly** from what they already have — no shopping trip required.

---

## Features

- **Pantry Management** — Add, edit, delete ingredients (name, quantity, unit, expiry date, barcode, notes)
- **Expiry Tracking** — Colour-coded list: green (fresh), amber (≤3 days), red (expired); daily notification alerts
- **Suggested Recipes** — Strict-matching: a recipe only appears if **every** ingredient is present in the required quantity
- **Almost There** — Bonus tab showing recipes missing exactly 1 ingredient
- **Recipe Detail** — Full ingredient list and step-by-step preparation method
- **Settings** — Toggle expiry notifications, configure alert threshold (1/3/7 days), metric/imperial units preference
- **Search & Filter** — Real-time search, category chip filter, sort by name/expiry/date added
- **20 seeded recipes** — Pre-loaded on first run (Classic Omelette, Pancakes, Fried Rice, Chickpea Curry, and more)

---

## Database Choice: SQLite (SQLiteOpenHelper)

SQLite was chosen because:
1. It is covered directly in the module's persistent data chapter
2. It requires zero external services — fully on-device
3. All data survives app restarts and device reboots
4. The schema (3 tables: `pantry_items`, `recipes`, `recipe_ingredients`) maps naturally to relational SQLite
5. No internet permission needed, keeping the app simple and offline-first

---

## Project Structure

```
app/src/main/java/com/smartpantry/manager/
├── model/          PantryItem, Recipe, RecipeIngredient
├── database/       DatabaseHelper (schema + 20 seeded recipes), PantryItemDao, RecipeDao (strict-match)
├── repository/     PantryRepository (sync + async access)
├── adapter/        PantryAdapter, RecipeAdapter
├── ui/             MainActivity, AddEditItemActivity, ItemDetailActivity,
│                   SuggestedRecipesActivity, RecipeDetailActivity, SettingsActivity
└── util/           NotificationHelper, ExpiryNotificationReceiver, BootReceiver
```

---

## Setup & Run Instructions

### Prerequisites
- Android Studio (Hedgehog / 2024.x or newer)
- Android SDK API 34
- JDK 8+

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/NthabiJantjie/SmartPantryManager.git
   ```
2. Open **Android Studio** → **File → Open** → select the `SmartPantryManager` folder
3. Wait for Gradle sync to complete (first sync downloads ~200 MB of dependencies)
4. Create an AVD: **Tools → Device Manager → + → Pixel 6 → API 34**
5. Press **▶ Run** (Shift+F10) — the app installs on the emulator automatically
6. On first launch the database is created and 20 recipes are seeded automatically

### Physical Device
Enable **Developer Options → USB Debugging**, connect via USB, select the device in the dropdown and press ▶.

---

## Screens

| Screen | Description |
|--------|-------------|
| Pantry List (Home) | RecyclerView of all items with search, category filter, summary cards |
| Add / Edit Item | Form with validation, category/unit dropdowns, DatePickerDialog |
| Item Detail | Full item view with expiry banner, edit and delete |
| Suggested Recipes | Strict-matched recipes (tab 1) + Almost There (tab 2) |
| Recipe Detail | Full ingredient list and numbered preparation steps |
| Settings | Notification toggle, alert threshold radio buttons, units toggle |

---

## Strict-Matching Algorithm

Located in [`RecipeDao.java`](app/src/main/java/com/smartpantry/manager/database/RecipeDao.java):

- Every recipe ingredient name is **normalised** (lowercase, trimmed, plural suffix stripped)
- Every pantry item name is normalised the same way before building the lookup map
- Units are **converted to base units** (grams for mass, mL for volume) before quantity comparison — so `500 g` matches `0.5 kg`
- A recipe only appears in **Suggested** if `ingredientSatisfied()` returns `true` for **every** ingredient
- The **Almost There** tab shows recipes where exactly 1 ingredient fails the check

---

### Smart-Pantry Manager Video





