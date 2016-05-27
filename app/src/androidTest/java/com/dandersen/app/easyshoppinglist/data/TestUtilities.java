package com.dandersen.app.easyshoppinglist.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.dandersen.app.easyshoppinglist.util.PollingCheck;

import java.util.Map;
import java.util.Set;

/**
 * Created by Dan on 23-05-2016.
 */
public class TestUtilities extends AndroidTestCase {
    static final String TEST_CATEGORY = "Dairy products";
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

    static ContentValues createDairyCategoryValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(ShoppingContract.CategoryEntry.COLUMN_NAME, TestUtilities.TEST_CATEGORY);
        testValues.put(ShoppingContract.CategoryEntry.COLUMN_DESCRIPTION, "Produkter baseret på mælk");

        return testValues;
    }

    static long insertDairyCategoryValues(Context context) {
        // insert our test records into the database
        ShoppingDbHelper dbHelper = new ShoppingDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createDairyCategoryValues();

        long locationRowId;
        locationRowId = db.insert(ShoppingContract.CategoryEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert Dairy category values", locationRowId != -1);

        return locationRowId;
    }

    static ContentValues createDairyProductValues(long categoryRowId) {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(ShoppingContract.ProductEntry.COLUMN_NAME, "Minimælk");
        testValues.put(ShoppingContract.ProductEntry.COLUMN_CATEGORY_ID, categoryRowId);
        testValues.put(ShoppingContract.ProductEntry.COLUMN_FAVORITE, 1);
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
        ContentValues testValues = new ContentValues();
        testValues.put(ShoppingContract.ShoppingListProductsEntry.COLUMN_PRODUCT_ID, productRowId);
        testValues.put(ShoppingContract.ShoppingListProductsEntry.COLUMN_SHOPPING_LIST_ID, shoppingListRowId);
        testValues.put(ShoppingContract.ShoppingListProductsEntry.COLUMN_SHOP_ID, shopRowId);
        testValues.put(ShoppingContract.ShoppingListProductsEntry.COLUMN_SORT_ORDER, 1);

        return testValues;
    }

    /*
        The functions inside of TestProvider use this utility class to test the ContentObserver
        callbacks using the PollingCheck class that we grabbed from the Android CTS tests.

        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }

}
