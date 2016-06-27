package com.dandersen.app.easyshoppinglist.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.dandersen.app.easyshoppinglist.R;

/**
 * Created by dandersen on 29-05-2016.
 * Database utilities
 */
public class Utilities {

    private static String LOG_TAG = Utilities.class.getSimpleName();

    private static void deleteAllRecordsFromDatabase(Context context) {
        context.getContentResolver().delete(
                ShoppingContract.ProductEntry.CONTENT_URI,
                null,
                null
        );
        context.getContentResolver().delete(
                ShoppingContract.CategoryEntry.CONTENT_URI,
                null,
                null
        );
        context.getContentResolver().delete(
                ShoppingContract.ShoppingListEntry.CONTENT_URI,
                null,
                null
        );
        context.getContentResolver().delete(
                ShoppingContract.ShopEntry.CONTENT_URI,
                null,
                null
        );
        context.getContentResolver().delete(
                ShoppingContract.ShopCategoryEntry.CONTENT_URI,
                null,
                null
        );
        context.getContentResolver().delete(
                ShoppingContract.ShoppingListProductEntry.CONTENT_URI,
                null,
                null
        );
    }


    public static void populateDatabase(Context context, SQLiteDatabase db) {
        // Get categories
        String[] categories = context.getResources().getStringArray(R.array.category_names);

        // Get default category sort order
        int[] categorySortOrder = context.getResources().getIntArray(R.array.category_sortorder);

        if (categories.length != categorySortOrder.length)
            throw new AssertionError("Category sort orders does not match no of categories");

        // Get category colors which will be dealt out randomly
        String[] colors = context.getResources().getStringArray(R.array.categoryColors);
        //StringUtil.shuffle(colors);
        int colorIndex = 0;

        // Insert categories (bulk)
        int insertCategoriesCount = 0, insertProductsCount = 0, insertShopCategoriesCount = 0;

        try {
            db.beginTransaction();

            insertDefaultStore(context, db);

            int index = 0;
            for (String category : categories) {
                // Restart color index if we are out of colors
                if (colorIndex >= colors.length)
                    colorIndex = 0;

                // Insert category
                ContentValues categoryValues = new ContentValues();
                categoryValues.put(ShoppingContract.CategoryEntry.COLUMN_NAME, category);
                categoryValues.put(ShoppingContract.CategoryEntry.COLUMN_SORT_ORDER, categorySortOrder[index]);
                categoryValues.put(ShoppingContract.CategoryEntry.COLUMN_COLOR, colors[colorIndex++]);
                long categoryId = db.insert(ShoppingContract.CategoryEntry.TABLE_NAME, null, categoryValues);
                if (categoryId != -1) {
                    ++insertCategoriesCount;

                    // Insert store category for default store
                    ContentValues shopCategoryValues = new ContentValues();
                    shopCategoryValues.put(ShoppingContract.ShopCategoryEntry.COLUMN_SHOP_ID, ShoppingContract.ShopEntry.DEFAULT_STORE_ID);
                    shopCategoryValues.put(ShoppingContract.ShopCategoryEntry.COLUMN_CATEGORY_ID, categoryId);
                    shopCategoryValues.put(ShoppingContract.ShopCategoryEntry.COLUMN_SORTORDER, categorySortOrder[index]);
                    long shopCategoryId = db.insert(ShoppingContract.ShopCategoryEntry.TABLE_NAME, null, shopCategoryValues);
                    if (shopCategoryId != -1) ++insertShopCategoriesCount;

                    // Insert products for category
                    String[] products = getProductsForCategoryFromRes(context, index);
                    for (String product : products) {
                        ContentValues productValues = new ContentValues();
                        productValues.put(ShoppingContract.ProductEntry.COLUMN_NAME, product);
                        productValues.put(ShoppingContract.ProductEntry.COLUMN_CATEGORY_ID, categoryId);
                        long productId = db.insert(ShoppingContract.ProductEntry.TABLE_NAME, null, productValues);
                        if (productId != -1) ++insertProductsCount;
                    }
                }

                ++index;
            }
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
        Log.i(LOG_TAG, "DSA LOG - " + insertCategoriesCount + " categories inserted, "
                + insertProductsCount + " products inserted, "
                + insertShopCategoriesCount + " shop categories inserted");
    }

    private static String[] getProductsForCategoryFromRes(Context context, int index) {
        switch (index) {
            case 0:     // Baking
                return context.getResources().getStringArray(R.array.baking_products);
            case 1:     // Bread/cakes
                return context.getResources().getStringArray(R.array.bread_products);
            case 2:     // Fish
                return context.getResources().getStringArray(R.array.fish_products);
            case 3:     // Frozen
                return context.getResources().getStringArray(R.array.frozen_products);
            case 4:     // Fruit/vegetables
                return context.getResources().getStringArray(R.array.fruit_products);
            case 5:     // Household
                return context.getResources().getStringArray(R.array.household_products);
            case 6:     // Coffee/the
                return context.getResources().getStringArray(R.array.coffee_products);
            case 7:     // Kiosk
                return context.getResources().getStringArray(R.array.kiosk_products);
            case 8:     // Colonial
                return context.getResources().getStringArray(R.array.colonial_products);
            case 9:     // Spices
                return context.getResources().getStringArray(R.array.spice_products);
            case 10:    // Meat
                return context.getResources().getStringArray(R.array.meat_products);
            case 11:    // Jam
                return context.getResources().getStringArray(R.array.jam_products);
            case 12:    // Dairy
                return context.getResources().getStringArray(R.array.dairy_products);
            case 13:    // Cereal
                return context.getResources().getStringArray(R.array.cereal_products);
            case 14:    // Cold cuts
                return context.getResources().getStringArray(R.array.coldcuts_products);
            case 15:    // Beer/wine/liquour
                return context.getResources().getStringArray(R.array.beer_products);
            default:
                throw new AssertionError("Unknown category " + index);
        }
    }
    
    private static void insertDefaultStore(Context context, SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ShoppingContract.ShopEntry._ID, ShoppingContract.ShopEntry.DEFAULT_STORE_ID);
        contentValues.put(ShoppingContract.ShopEntry.COLUMN_NAME, context.getString(R.string.default_shop_name));
        contentValues.put(ShoppingContract.ShopEntry.COLUMN_PLACE_ID, ShoppingContract.ShopEntry.DEFAULT_STORE_PLACE_ID);

        // Insert store
        long storeId = db.insert(ShoppingContract.ShopEntry.TABLE_NAME, null, contentValues);
    }

    public static void repopulateDatabase(Context context) {
        // Delete all records from database through content provider
        deleteAllRecordsFromDatabase(context);

        // Get reference to writable database
        SQLiteDatabase db = ShoppingDbHelper.getInstance(context).getWritableDatabase();
        if (!db.isOpen()) throw new AssertionError("Database is not open");

        populateDatabase(context, db);
    }
}
