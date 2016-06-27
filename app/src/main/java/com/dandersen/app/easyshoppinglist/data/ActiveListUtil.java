package com.dandersen.app.easyshoppinglist.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.dandersen.app.easyshoppinglist.R;

/**
 * Created by Dan on 24-06-2016.
 * Utility functions for active shopping list.
 */
public class ActiveListUtil {

    private final static String DESC = " DESC";
    private final static String[] LAST_USED_SHOP_PROJECTION =
            new String[] { ShoppingContract.ShoppingListProductEntry.COLUMN_SHOP_ID ,
                    ShoppingContract.ShoppingListProductEntry.COLUMN_PRODUCT_ID,
                    ShoppingContract.ShoppingListProductEntry._ID };
    private final static String LAST_USED_SHOP_ID_SELECTION =
                    ShoppingContract.ShoppingListProductEntry.COLUMN_SHOPPING_LIST_ID + " = ?";

    public static Long getActiveShoppingListId(Context context) {
        String sortOrder = ShoppingContract.ShoppingListEntry.COLUMN_CREATED + DESC;

        Cursor c = null;
        try {
            c = context.getContentResolver().query(
                    ShoppingContract.ShoppingListEntry.CONTENT_URI,
                    new String[]{ ShoppingContract.ShoppingListEntry._ID },
                    null,           // cols for "where" clause
                    null,           // values for "where" clause
                    sortOrder       // sort order
            );
            if (c == null) return null;
            if (c.moveToFirst()) {
                return c.getLong(0);
            }

            // No shopping list, so we create one
            ContentValues contentValues = new ContentValues();
            contentValues.put(ShoppingContract.ShoppingListEntry.COLUMN_NAME, context.getString(R.string.new_shopping_list_name));

            // Insert product
            Uri insertUri = context.getContentResolver().insert(ShoppingContract.ShoppingListEntry.CONTENT_URI, contentValues);
            return ContentUris.parseId(insertUri);
        }
        finally {
            if (c != null) c.close();
        }
    }

    /**
     * Find the last used shop id in the shoppinglistproducts table
     * @param context Current context
     * @return Last used shop id paired with null if the shop has products and
     *         the id of the shoppinglistproduct row otherwise.
     *         Returns null if no last used shop can be found.
     */
    public static Pair<Long, Long> getLastUsedShopId(Context context) {
        Uri uri = ShoppingContract.ShoppingListProductEntry.CONTENT_URI;

        Long activeListId = getActiveShoppingListId(context);
        if (activeListId == null) return null;

        String[] selectionArgs = new String[] { Long.toString(activeListId) };

        String sortOrder = ShoppingContract.ShoppingListProductEntry.COLUMN_TIMESTAMP + DESC + " LIMIT 1";

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri,
                    LAST_USED_SHOP_PROJECTION,
                    LAST_USED_SHOP_ID_SELECTION,
                    selectionArgs,
                    sortOrder);
            if (cursor != null && cursor.moveToFirst()) {
                Long shopId = cursor.getLong(0);
                if (cursor.isNull(1)) {
                    return Pair.create(shopId, cursor.getLong(2));
                }
                return Pair.create(shopId, null);
            }
        }
        finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public static void removeShop(@NonNull Context context, @NonNull Long shopId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ShoppingContract.ShoppingListProductEntry.COLUMN_SHOP_ID, ShoppingContract.ShopEntry.DEFAULT_STORE_ID);

        // Remove shop from the products where it is registered as shop
        int count = context.getContentResolver().update(
                ShoppingContract.ShoppingListProductEntry.CONTENT_URI,
                contentValues,
                ShoppingContract.ShoppingListProductEntry.COLUMN_SHOP_ID + "= ?",
                new String[]{ Long.toString(shopId) });
    }

    public static void addShop(@NonNull Context context, @NonNull Long shopId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ShoppingContract.ShoppingListProductEntry.COLUMN_SHOP_ID, shopId);

        // Add shop to products without a real shop
        int count = context.getContentResolver().update(
                ShoppingContract.ShoppingListProductEntry.CONTENT_URI,
                contentValues,
                ShoppingContract.ShoppingListProductEntry.COLUMN_SHOP_ID + "= ?",
                new String[]{ Long.toString(ShoppingContract.ShopEntry.DEFAULT_STORE_ID) });
        if (count == 0) {
            Long activeShoppingListId = getActiveShoppingListId(context);
            if (activeShoppingListId == null) return;

            contentValues.put(ShoppingContract.ShoppingListProductEntry.COLUMN_SHOPPING_LIST_ID, activeShoppingListId);
            context.getContentResolver().insert(
                    ShoppingContract.ShoppingListProductEntry.CONTENT_URI,
                    contentValues);
        }
    }

    public static void removeProduct(@NonNull Context context, @NonNull Long shoppingListProductId) {
        context.getContentResolver().delete(
                ShoppingContract.ShoppingListProductEntry.
                        buildShoppingListProductsUri(shoppingListProductId),
                null,
                null);
    }
}
