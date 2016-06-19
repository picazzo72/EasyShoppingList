package com.dandersen.app.easyshoppinglist.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.dandersen.app.easyshoppinglist.R;
import com.dandersen.app.easyshoppinglist.utils.StringUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
                ShoppingContract.ShoppingListProductsEntry.CONTENT_URI,
                null,
                null
        );
    }


    public static void populateDatabase(Context context, SQLiteDatabase db) {
        HashMap<String, String[]> categoryProducts = new HashMap<>();

        int index = 0;
        String[] categories = context.getResources().getStringArray(R.array.category_names);
        for (String category : categories) {
            switch (index) {
                case 0:     // Baking
                    categoryProducts.put(category, context.getResources().getStringArray(R.array.baking_products));
                    break;
                case 1:     // Bread/cakes
                    categoryProducts.put(category, context.getResources().getStringArray(R.array.bread_products));
                    break;
                case 2:     // Fish
                    categoryProducts.put(category, context.getResources().getStringArray(R.array.fish_products));
                    break;
                case 3:     // Frozen
                    categoryProducts.put(category, context.getResources().getStringArray(R.array.frozen_products));
                    break;
                case 4:     // Fruit/vegetables
                    categoryProducts.put(category, context.getResources().getStringArray(R.array.fruit_products));
                    break;
                case 5:     // Household
                    categoryProducts.put(category, context.getResources().getStringArray(R.array.household_products));
                    break;
                case 6:     // Coffee/the
                    categoryProducts.put(category, context.getResources().getStringArray(R.array.coffee_products));
                    break;
                case 7:     // Kiosk
                    categoryProducts.put(category, context.getResources().getStringArray(R.array.kiosk_products));
                    break;
                case 8:     // Colonial
                    categoryProducts.put(category, context.getResources().getStringArray(R.array.colonial_products));
                    break;
                case 9:     // Spices
                    categoryProducts.put(category, context.getResources().getStringArray(R.array.spice_products));
                    break;
                case 10:    // Meat
                    categoryProducts.put(category, context.getResources().getStringArray(R.array.meat_products));
                    break;
                case 11:    // Jam
                    categoryProducts.put(category, context.getResources().getStringArray(R.array.jam_products));
                    break;
                case 12:    // Dairy
                    categoryProducts.put(category, context.getResources().getStringArray(R.array.dairy_products));
                    break;
                case 13:    // Cereal
                    categoryProducts.put(category, context.getResources().getStringArray(R.array.cereal_products));
                    break;
                case 14:    // Cold cuts
                    categoryProducts.put(category, context.getResources().getStringArray(R.array.coldcuts_products));
                    break;
                case 15:    // Beer/wine/liquour
                    categoryProducts.put(category, context.getResources().getStringArray(R.array.beer_products));
                    break;
                default:
                    throw new AssertionError("Unknown category " + index);
            }
            ++index;
        }


        { // Insert categories (bulk)
            int insertCategoriesCount = 0;
            int insertProductsCount = 0;

            // Get category colors which will be dealt out randomly
            String[] colors = context.getResources().getStringArray(R.array.categoryColors);
            StringUtil.shuffle(colors);

            try {
                int colorIndex = 0;
                db.beginTransaction();
                Iterator it = categoryProducts.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();

                    // Restart color index if we are out of colors
                    if (colorIndex >= colors.length)
                        colorIndex = 0;

                    // Insert category
                    ContentValues categoryValues = new ContentValues();
                    categoryValues.put(ShoppingContract.CategoryEntry.COLUMN_NAME, (String) pair.getKey());
                    categoryValues.put(ShoppingContract.CategoryEntry.COLUMN_SORT_ORDER, insertCategoriesCount + 1);
                    categoryValues.put(ShoppingContract.CategoryEntry.COLUMN_COLOR, colors[colorIndex++]);
                    long categoryId = db.insert(ShoppingContract.CategoryEntry.TABLE_NAME, null, categoryValues);
                    if (categoryId != -1) {
                        ++insertCategoriesCount;

                        // Insert products
                        String[] products = (String[]) pair.getValue();
                        for (String product : products) {
                            ContentValues productValues = new ContentValues();
                            productValues.put(ShoppingContract.ProductEntry.COLUMN_NAME, product);
                            productValues.put(ShoppingContract.ProductEntry.COLUMN_CATEGORY_ID, categoryId);
                            long productId = db.insert(ShoppingContract.ProductEntry.TABLE_NAME, null, productValues);
                            if (productId != -1) ++insertProductsCount;
                        }
                    }
                }
                db.setTransactionSuccessful();
            }
            finally {
                db.endTransaction();
            }
            Log.i(LOG_TAG, "DSA LOG - " + insertCategoriesCount + " categories inserted, "
                    + insertProductsCount + " products inserted");
        }

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
