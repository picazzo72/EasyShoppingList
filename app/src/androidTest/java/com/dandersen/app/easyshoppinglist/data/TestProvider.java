package com.dandersen.app.easyshoppinglist.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

/**
 * Created by Dan on 26-05-2016.
 * Tests for the provider
 */
/*
    Note: This is not a complete set of tests of the ContentProvider, but it does test
    that at least the basic functionality has been implemented correctly.

    Students: Uncomment the tests in this class as you implement the functionality in your
    ContentProvider to make sure that you've implemented things reasonably correctly.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    /*
       This helper function deletes all records from database tables using the ContentProvider.
       It also queries the ContentProvider to make sure that the database has been successfully
       deleted
     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                ShoppingContract.ProductEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                ShoppingContract.CategoryEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                ShoppingContract.ShoppingListEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                ShoppingContract.ShopEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                ShoppingContract.ShoppingListProductEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                ShoppingContract.ProductEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Products table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                ShoppingContract.CategoryEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Category table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                ShoppingContract.ShoppingListEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Shopping List table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                ShoppingContract.ShopEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Shop table during delete", 0, cursor.getCount());
        cursor.close();
    }

    /*
        Delete all records
     */
    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /*
        This test checks to make sure that the content provider is registered correctly.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                ShoppingProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: ShoppingProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + ShoppingContract.CONTENT_AUTHORITY,
                    providerInfo.authority, ShoppingContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: ShoppingProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /*
        This test doesn't touch the database.  It verifies that the ContentProvider returns
        the correct type for each type of URI that it can handle.
    */
    public void testGetType() {
        // content://com.dandersen.app.easyshoppinglist/category/
        String type = mContext.getContentResolver().getType(ShoppingContract.CategoryEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.dandersen.app.easyshoppinglist/category
        assertEquals("Error: the CategoryEntry CONTENT_URI should return CategoryEntry.CONTENT_TYPE",
                ShoppingContract.CategoryEntry.CONTENT_TYPE, type);

        // content://com.dandersen.app.easyshoppinglist/product/
        type = mContext.getContentResolver().getType(ShoppingContract.ProductEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.dandersen.app.easyshoppinglist/product
        assertEquals("Error: the ProductEntry CONTENT_URI should return ProductEntry.CONTENT_TYPE",
                ShoppingContract.ProductEntry.CONTENT_TYPE, type);

        // content://com.dandersen.app.easyshoppinglist/product/dairy
        type = mContext.getContentResolver().getType(
                ShoppingContract.ProductEntry.buildProductCategory(TestUtilities.TEST_CATEGORY));
        // vnd.android.cursor.dir/com.dandersen.app.easyshoppinglist/product/dairy
        assertEquals("Error: the ProductEntry CONTENT_URI with category should return ProductEntry.CONTENT_TYPE",
                ShoppingContract.ProductEntry.CONTENT_TYPE, type);

        // content://com.dandersen.app.easyshoppinglist/shopping_list/
        type = mContext.getContentResolver().getType(ShoppingContract.ShoppingListEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.dandersen.app.easyshoppinglist/weather
        assertEquals("Error: the ShoppingListEntry CONTENT_URI should return ShoppingListEntry.CONTENT_TYPE",
                ShoppingContract.ShoppingListEntry.CONTENT_TYPE, type);


        // content://com.dandersen.app.easyshoppinglist/shop/
        type = mContext.getContentResolver().getType(ShoppingContract.ShopEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.dandersen.app.easyshoppinglist/shop
        assertEquals("Error: the ShopEntry CONTENT_URI should return ShopEntry.CONTENT_TYPE",
                ShoppingContract.ShopEntry.CONTENT_TYPE, type);
    }


    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.  Uncomment this test to see if the basic product query functionality
        given in the ContentProvider is working correctly.
     */
    public void testBasicProductQuery() {
        // insert our test records into the database
        ShoppingDbHelper dbHelper = ShoppingDbHelper.getInstance(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long categoryRowId = TestUtilities.insertDairyCategoryValues(mContext);

        // Fantastic.  Now that we have a category, add a product!
        ContentValues productValues = TestUtilities.createDairyProductValues(categoryRowId);

        long productRowId = db.insert(ShoppingContract.ProductEntry.TABLE_NAME, null, productValues);
        assertTrue("Unable to insert product into the Database", productRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor c = mContext.getContentResolver().query(
                ShoppingContract.ProductEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicProductQuery", c, productValues);
    }

    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.  Uncomment this test to see if your location queries are
        performing correctly.
     */
//    public void testBasicLocationQueries() {
//        // insert our test records into the database
//        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//        ContentValues testValues = TestUtilities.createNorthPoleLocationValues();
//        long locationRowId = TestUtilities.insertNorthPoleLocationValues(mContext);
//
//        // Test the basic content provider query
//        Cursor locationCursor = mContext.getContentResolver().query(
//                LocationEntry.CONTENT_URI,
//                null,
//                null,
//                null,
//                null
//        );
//
//        // Make sure we get the correct cursor out of the database
//        TestUtilities.validateCursor("testBasicLocationQueries, location query", locationCursor, testValues);
//
//        // Has the NotificationUri been set correctly? --- we can only test this easily against API
//        // level 19 or greater because getNotificationUri was added in API level 19.
//        if ( Build.VERSION.SDK_INT >= 19 ) {
//            assertEquals("Error: Location Query did not properly set NotificationUri",
//                    locationCursor.getNotificationUri(), LocationEntry.CONTENT_URI);
//        }
//    }

    /*
        This test uses the provider to insert and then update the data.
     */
    public void testUpdateCategory() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities.createDairyCategoryValues();

        Uri categoryUri = mContext.getContentResolver().insert(ShoppingContract.CategoryEntry.CONTENT_URI, values);
        long categoryRowId = ContentUris.parseId(categoryUri);

        // Verify we got a row back.
        assertTrue(categoryRowId != -1);
        Log.d(LOG_TAG, "New row id: " + categoryRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(ShoppingContract.CategoryEntry._ID, categoryRowId);
        updatedValues.put(ShoppingContract.CategoryEntry.COLUMN_NAME, "vegetables");

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers as expected
        {
            Cursor c = mContext.getContentResolver().query(ShoppingContract.CategoryEntry.CONTENT_URI, null, null, null, null);

            TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
            c.registerContentObserver(tco);

            int count = mContext.getContentResolver().update(
                    ShoppingContract.CategoryEntry.CONTENT_URI,
                    updatedValues,
                    ShoppingContract.CategoryEntry._ID + "= ?",
                    new String[]{Long.toString(categoryRowId)});
            assertEquals(count, 1);

            // Test to make sure our observer is called.  If not, we throw an assertion.
            //
            // If your code is failing here, it means that your content provider
            // isn't calling getContext().getContentResolver().notifyChange(uri, null);
            tco.waitForNotificationOrFail();

            c.unregisterContentObserver(tco);
            c.close();
        }

        { // A cursor is your primary interface to the query results.
            Cursor c = mContext.getContentResolver().query(
                    ShoppingContract.CategoryEntry.CONTENT_URI,
                    null,   // projection
                    ShoppingContract.CategoryEntry._ID + " = " + categoryRowId,
                    null,   // Values for the "where" clause
                    null    // sort order

            );

            TestUtilities.validateCursor("testUpdateCategory.  Error validating category entry update.",
                    c, updatedValues);

            c.close();
        }
    }


    private long testInsertReadCategory() {
        ContentValues testValues = TestUtilities.createDairyCategoryValues();

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(
                ShoppingContract.CategoryEntry.CONTENT_URI, true, tco);
        Uri categoryUri = mContext.getContentResolver().insert(
                ShoppingContract.CategoryEntry.CONTENT_URI, testValues);

        // Did our content observer get called? If this fails, your insert location
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long categoryId = ContentUris.parseId(categoryUri);

        // Verify we got a row back.
        assertTrue(categoryId != -1);

        // Data's inserted. IN THEORY. Now pull some out to verify it made the round trip.

        Cursor c = mContext.getContentResolver().query(
                ShoppingContract.CategoryEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating CategoryEntry.",
                c, testValues);

        return categoryId;
    }

    private long testInsertReadProduct(long categoryId) {
        ContentValues productValues = TestUtilities.createDairyProductValues(categoryId);
        // The TestContentObserver is a one-shot class
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(
                ShoppingContract.ProductEntry.CONTENT_URI, true, tco);

        Uri productInsertUri = mContext.getContentResolver()
                .insert(ShoppingContract.ProductEntry.CONTENT_URI, productValues);
        assertTrue(productInsertUri != null);

        // Did our content observer get called?  If this fails, your insert product
        // in your ContentProvider isn't calling
        // getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long productId = ContentUris.parseId(productInsertUri);

        // A cursor is your primary interface to the query results.
        Cursor productCursor = mContext.getContentResolver().query(
                ShoppingContract.ProductEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating ProductEntry insert.",
                productCursor, productValues);

        // Add the location values in with the weather data so that we can make
        // sure that the join worked and we actually get all the values back
        ContentValues testValues = TestUtilities.createDairyCategoryValues();
        productValues.putAll(testValues);

        // Get the joined Product and Category data
        productCursor = mContext.getContentResolver().query(
                ShoppingContract.ProductEntry.buildProductCategory(TestUtilities.TEST_CATEGORY),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider. Error validating joined Product and Category data.",
                productCursor, productValues);

        return productId;
    }

    private long testInsertReadShop() {
        ContentValues testValues = TestUtilities.createDonaldDuckShopValues();

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(
                ShoppingContract.ShopEntry.CONTENT_URI, true, tco);
        Uri shopUri = mContext.getContentResolver().insert(
                ShoppingContract.ShopEntry.CONTENT_URI, testValues);

        // Did our content observer get called? If this fails, your insert location
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long shopId = ContentUris.parseId(shopUri);

        // Verify we got a row back.
        assertTrue(shopId != -1);

        // Data's inserted. IN THEORY. Now pull some out to verify it made the round trip.

        Cursor c = mContext.getContentResolver().query(
                ShoppingContract.ShopEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating ShopEntry.",
                c, testValues);

        return shopId;
    }

    private long testInsertReadShoppingList() {
        ContentValues testValues = TestUtilities.createShoppingListValues();

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(
                ShoppingContract.ShoppingListEntry.CONTENT_URI, true, tco);
        Uri shoppingListUri = mContext.getContentResolver().insert(
                ShoppingContract.ShoppingListEntry.CONTENT_URI, testValues);

        // Did our content observer get called? If this fails, your insert location
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long shoppingListId = ContentUris.parseId(shoppingListUri);

        // Verify we got a row back.
        assertTrue(shoppingListId != -1);

        // Data's inserted. IN THEORY. Now pull some out to verify it made the round trip.

        Cursor c = mContext.getContentResolver().query(
                ShoppingContract.ShoppingListEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating ShoppingListEntry.",
                c, testValues);

        return shoppingListId;
    }

    // Make sure we can still delete after adding/updating stuff
    public void testInsertReadProvider() {
        // Insert and read a test category
        long categoryId = testInsertReadCategory();

        // Insert and read a test product
        long productId = testInsertReadProduct(categoryId);

        // Insert and read a test shop
        long shopId = testInsertReadShop();

        // Insert and read a test shopping list
        long shoppingListId = testInsertReadShoppingList();

//        // Get the joined Weather and Location data with a start date
//        productCursor = mContext.getContentResolver().query(
//                WeatherEntry.buildWeatherLocationWithStartDate(
//                        TestUtilities.TEST_LOCATION, TestUtilities.TEST_DATE),
//                null, // leaving "columns" null just returns all the columns.
//                null, // cols for "where" clause
//                null, // values for "where" clause
//                null  // sort order
//        );
//        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Weather and Location Data with start date.",
//                productCursor, productValues);
//
//        // Get the joined Weather data for a specific date
//        productCursor = mContext.getContentResolver().query(
//                WeatherEntry.buildWeatherLocationWithDate(TestUtilities.TEST_LOCATION, TestUtilities.TEST_DATE),
//                null,
//                null,
//                null,
//                null
//        );
//        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Weather and Location data for a specific date.",
//                productCursor, productValues);
    }

    // Make sure we can still delete after adding/updating stuff
    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for our product delete.
        TestUtilities.TestContentObserver productObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(
                ShoppingContract.ProductEntry.CONTENT_URI, true, productObserver);

        // Register a content observer for our category delete.
        TestUtilities.TestContentObserver categoryObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(
                ShoppingContract.CategoryEntry.CONTENT_URI, true, categoryObserver);

        // Register a content observer for our shop delete.
        TestUtilities.TestContentObserver shopObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(
                ShoppingContract.ShopEntry.CONTENT_URI, true, shopObserver);

        // Register a content observer for our shopping list delete.
        TestUtilities.TestContentObserver shoppingListObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(
                ShoppingContract.ShoppingListEntry.CONTENT_URI, true, shoppingListObserver);

        deleteAllRecordsFromProvider();

        // If either of these fail, you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // delete.  (only if the insertReadProvider is succeeding)
        productObserver.waitForNotificationOrFail();
        categoryObserver.waitForNotificationOrFail();
        shopObserver.waitForNotificationOrFail();
        shoppingListObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(productObserver);
        mContext.getContentResolver().unregisterContentObserver(categoryObserver);
        mContext.getContentResolver().unregisterContentObserver(shopObserver);
        mContext.getContentResolver().unregisterContentObserver(shoppingListObserver);
    }


//    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;
//    static ContentValues[] createBulkInsertWeatherValues(long locationRowId) {
//        long currentTestDate = TestUtilities.TEST_DATE;
//        long millisecondsInADay = 1000*60*60*24;
//        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];
//
//        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, currentTestDate+= millisecondsInADay ) {
//            ContentValues weatherValues = new ContentValues();
//            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationRowId);
//            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_CREATED, currentTestDate);
//            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, 1.1);
//            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, 1.2 + 0.01 * (float) i);
//            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, 1.3 - 0.01 * (float) i);
//            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, 75 + i);
//            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, 65 - i);
//            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
//            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, 5.5 + 0.2 * (float) i);
//            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, 321);
//            returnContentValues[i] = weatherValues;
//        }
//        return returnContentValues;
//    }
//
//    // Student: Uncomment this test after you have completed writing the BulkInsert functionality
//    // in your provider.  Note that this test will work with the built-in (default) provider
//    // implementation, which just inserts records one-at-a-time, so really do implement the
//    // BulkInsert ContentProvider function.
//    public void testBulkInsert() {
//        // first, let's create a location value
//        ContentValues testValues = TestUtilities.createNorthPoleLocationValues();
//        Uri locationUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, testValues);
//        long locationRowId = ContentUris.parseId(locationUri);
//
//        // Verify we got a row back.
//        assertTrue(locationRowId != -1);
//
//        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
//        // the round trip.
//
//        // A cursor is your primary interface to the query results.
//        Cursor cursor = mContext.getContentResolver().query(
//                LocationEntry.CONTENT_URI,
//                null, // leaving "columns" null just returns all the columns.
//                null, // cols for "where" clause
//                null, // values for "where" clause
//                null  // sort order
//        );
//
//        TestUtilities.validateCursor("testBulkInsert. Error validating LocationEntry.",
//                cursor, testValues);
//
//        // Now we can bulkInsert some weather.  In fact, we only implement BulkInsert for weather
//        // entries.  With ContentProviders, you really only have to implement the features you
//        // use, after all.
//        ContentValues[] bulkInsertContentValues = createBulkInsertWeatherValues(locationRowId);
//
//        // Register a content observer for our bulk insert.
//        TestUtilities.TestContentObserver weatherObserver = TestUtilities.getTestContentObserver();
//        mContext.getContentResolver().registerContentObserver(WeatherEntry.CONTENT_URI, true, weatherObserver);
//
//        int insertCount = mContext.getContentResolver().bulkInsert(WeatherEntry.CONTENT_URI, bulkInsertContentValues);
//
//        // Students:  If this fails, it means that you most-likely are not calling the
//        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
//        // ContentProvider method.
//        weatherObserver.waitForNotificationOrFail();
//        mContext.getContentResolver().unregisterContentObserver(weatherObserver);
//
//        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);
//
//        // A cursor is your primary interface to the query results.
//        cursor = mContext.getContentResolver().query(
//                WeatherEntry.CONTENT_URI,
//                null, // leaving "columns" null just returns all the columns.
//                null, // cols for "where" clause
//                null, // values for "where" clause
//                WeatherEntry.COLUMN_CREATED + " ASC"  // sort order == by DATE ASCENDING
//        );
//
//        // we should have as many records in the database as we've inserted
//        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);
//
//        // and let's make sure they match the ones we created
//        cursor.moveToFirst();
//        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
//            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating WeatherEntry " + i,
//                    cursor, bulkInsertContentValues[i]);
//        }
//        cursor.close();
//    }
}
