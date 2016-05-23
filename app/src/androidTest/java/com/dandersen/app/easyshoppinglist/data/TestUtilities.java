package com.dandersen.app.easyshoppinglist.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

/**
 * Created by Dan on 23-05-2016.
 */
public class TestUtilities extends AndroidTestCase {
    static final long TEST_DATE = 1419033600L;  // December 20th, 2014

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    static ContentValues createShoppingListValues() {
        ContentValues meterValues = new ContentValues();
        meterValues.put(ShoppingContract.ShoppingListEntry.COLUMN_AMOUNT, 245.6);
        meterValues.put(ShoppingContract.ShoppingListEntry.COLUMN_DATE, TEST_DATE);
        meterValues.put(ShoppingContract.ShoppingListEntry.COLUMN_NOTE, "Test shopping");

        return meterValues;
    }

    static ContentValues createDairyProductValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(ShoppingContract.ProductEntry.COLUMN_NAME, "Minimælk");
        testValues.put(ShoppingContract.ProductEntry.COLUMN_USE_COUNT, 12);

        return testValues;
    }

    static ContentValues createDonaldDuckShopValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(ShoppingContract.ShopEntry.COLUMN_NAME, "Donald Duck Disney Store");
        testValues.put(ShoppingContract.ShopEntry.COLUMN_ADDRESS, "1313 Webfoot Walk");
        testValues.put(ShoppingContract.ShopEntry.COLUMN_CITY, "Duckburg");
        testValues.put(ShoppingContract.ShopEntry.COLUMN_STATE, "Calisota");
        testValues.put(ShoppingContract.ShopEntry.COLUMN_COUNTRY, "USA");

        return testValues;
    }

    static ContentValues createShoppingListProductsValues(long productRowId, long shoppingListRowId, long shopRowId) {
        ContentValues meterValues = new ContentValues();
        meterValues.put(ShoppingContract.ShoppingListProductsEntry.COLUMN_PRODUCT_ID, productRowId);
        meterValues.put(ShoppingContract.ShoppingListProductsEntry.COLUMN_SHOPPING_LIST_ID, shoppingListRowId);
        meterValues.put(ShoppingContract.ShoppingListProductsEntry.COLUMN_SHOP_ID, shopRowId);

        return meterValues;
    }

}
