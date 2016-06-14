package com.dandersen.app.easyshoppinglist.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

/**
 * Created by Dan on 23-05-2016.
 * Testing the database.
 */
public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(ShoppingDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    /*
        Students: Uncomment this test once you've written the code to create the Location
        table.  Note that you will have to have chosen the same column names that I did in
        my solution for this test to compile, so if you haven't yet done that, this is
        a good time to change your column names to match mine.

        Note that this only tests that the Location table has the correct columns, since we
        give you the code for the weather table.  This test does not look at the
     */
    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<>();
        tableNameHashSet.add(ShoppingContract.ShoppingListEntry.TABLE_NAME);
        tableNameHashSet.add(ShoppingContract.CategoryEntry.TABLE_NAME);
        tableNameHashSet.add(ShoppingContract.ProductEntry.TABLE_NAME);
        tableNameHashSet.add(ShoppingContract.ShopEntry.TABLE_NAME);
        tableNameHashSet.add(ShoppingContract.ShoppingListProductsEntry.TABLE_NAME);

        mContext.deleteDatabase(ShoppingDbHelper.DATABASE_NAME);
        SQLiteDatabase db = ShoppingDbHelper.getInstance(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = null;
        try {
            c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

            assertTrue("Error: This means that the database has not been created correctly",
                    c.moveToFirst());

            // verify that the tables have been created
            do {
                tableNameHashSet.remove(c.getString(0));
            } while( c.moveToNext() );

            // check that all tables are created
            assertTrue("Error: Your database was created without one or more of the required tables",
                    tableNameHashSet.isEmpty());
        }
        finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }

        try {
            c = db.rawQuery("PRAGMA table_info(" + ShoppingContract.ShoppingListEntry.TABLE_NAME + ")",
                    null);

            assertTrue("Error: This means that we were unable to query the database for shopping list table information.",
                    c.moveToFirst());

            // Build a HashSet of all of the column names we want to look for
            final HashSet<String> columnHashSet = new HashSet<>();
            columnHashSet.add(ShoppingContract.ShoppingListEntry._ID);
            columnHashSet.add(ShoppingContract.ShoppingListEntry.COLUMN_DATE);
            columnHashSet.add(ShoppingContract.ShoppingListEntry.COLUMN_AMOUNT);
            columnHashSet.add(ShoppingContract.ShoppingListEntry.COLUMN_NOTE);

            int columnNameIndex = c.getColumnIndex("name");

            do {
                String columnName = c.getString(columnNameIndex);
                columnHashSet.remove(columnName);
            } while (c.moveToNext());

            // if this fails, it means that your database doesn't contain all of the required location
            // entry columns
            assertTrue("Error: The database doesn't contain all of the required shopping list entry columns",
                    columnHashSet.isEmpty());
        }
        finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }

        try {
            c = db.rawQuery("PRAGMA table_info(" + ShoppingContract.CategoryEntry.TABLE_NAME + ")",
                    null);

            assertTrue("Error: This means that we were unable to query the database for category table information.",
                    c.moveToFirst());

            // Build a HashSet of all of the column names we want to look for
            final HashSet<String> columnHashSet = new HashSet<>();
            columnHashSet.add(ShoppingContract.CategoryEntry._ID);
            columnHashSet.add(ShoppingContract.CategoryEntry.COLUMN_NAME);
            columnHashSet.add(ShoppingContract.CategoryEntry.COLUMN_DESCRIPTION);

            int columnNameIndex = c.getColumnIndex("name");

            do {
                String columnName = c.getString(columnNameIndex);
                columnHashSet.remove(columnName);
            } while (c.moveToNext());

            // if this fails, it means that your database doesn't contain all of the required location
            // entry columns
            assertTrue("Error: The database doesn't contain all of the required category entry columns",
                    columnHashSet.isEmpty());
        }
        finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }

        try {
            c = db.rawQuery("PRAGMA table_info(" + ShoppingContract.ProductEntry.TABLE_NAME + ")",
                    null);

            assertTrue("Error: This means that we were unable to query the database for product table information.",
                    c.moveToFirst());

            // Build a HashSet of all of the column names we want to look for
            final HashSet<String> columnHashSet = new HashSet<>();
            columnHashSet.add(ShoppingContract.ProductEntry._ID);
            columnHashSet.add(ShoppingContract.ProductEntry.COLUMN_NAME);
            columnHashSet.add(ShoppingContract.ProductEntry.COLUMN_CATEGORY_ID);
            columnHashSet.add(ShoppingContract.ProductEntry.COLUMN_FAVORITE);
            columnHashSet.add(ShoppingContract.ProductEntry.COLUMN_CUSTOM);
            columnHashSet.add(ShoppingContract.ProductEntry.COLUMN_LAST_USED);
            columnHashSet.add(ShoppingContract.ProductEntry.COLUMN_USE_COUNT);

            int columnNameIndex = c.getColumnIndex("name");

            do {
                String columnName = c.getString(columnNameIndex);
                columnHashSet.remove(columnName);
            } while (c.moveToNext());

            // if this fails, it means that your database doesn't contain all of the required location
            // entry columns
            assertTrue("Error: The database doesn't contain all of the required product entry columns",
                    columnHashSet.isEmpty());
        }
        finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }

        try {
            // now, do our tables contain the correct columns?
            c = db.rawQuery("PRAGMA table_info(" + ShoppingContract.ShopEntry.TABLE_NAME + ")",
                    null);

            assertTrue("Error: This means that we were unable to query the database for shop table information.",
                    c.moveToFirst());

            // Build a HashSet of all of the column names we want to look for
            final HashSet<String> columnHashSet = new HashSet<>();
            columnHashSet.add(ShoppingContract.ShopEntry._ID);
            columnHashSet.add(ShoppingContract.ShopEntry.COLUMN_NAME);
            columnHashSet.add(ShoppingContract.ShopEntry.COLUMN_STREET);
            columnHashSet.add(ShoppingContract.ShopEntry.COLUMN_CITY);
            columnHashSet.add(ShoppingContract.ShopEntry.COLUMN_STATE);
            columnHashSet.add(ShoppingContract.ShopEntry.COLUMN_COUNTRY);

            int columnNameIndex = c.getColumnIndex("name");

            do {
                String columnName = c.getString(columnNameIndex);
                columnHashSet.remove(columnName);
            } while (c.moveToNext());

            // if this fails, it means that your database doesn't contain all of the required location
            // entry columns
            assertTrue("Error: The database doesn't contain all of the required shop entry columns",
                    columnHashSet.isEmpty());
        }
        finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }

        try {
            c = db.rawQuery("PRAGMA table_info(" + ShoppingContract.ShoppingListProductsEntry.TABLE_NAME + ")",
                    null);

            assertTrue("Error: This means that we were unable to query the database for shopping list products table information.",
                    c.moveToFirst());

            // Build a HashSet of all of the column names we want to look for
            final HashSet<String> columnHashSet = new HashSet<>();
            columnHashSet.add(ShoppingContract.ShoppingListProductsEntry._ID);
            columnHashSet.add(ShoppingContract.ShoppingListProductsEntry.COLUMN_PRODUCT_ID);
            columnHashSet.add(ShoppingContract.ShoppingListProductsEntry.COLUMN_SHOPPING_LIST_ID);
            columnHashSet.add(ShoppingContract.ShoppingListProductsEntry.COLUMN_SHOP_ID);
            columnHashSet.add(ShoppingContract.ShoppingListProductsEntry.COLUMN_SORT_ORDER);

            int columnNameIndex = c.getColumnIndex("name");

            do {
                String columnName = c.getString(columnNameIndex);
                columnHashSet.remove(columnName);
            } while (c.moveToNext());

            // if this fails, it means that your database doesn't contain all of the required location
            // entry columns
            assertTrue("Error: The database doesn't contain all of the required shopping list products entry columns",
                    columnHashSet.isEmpty());
        }
        finally {
            if (c != null) {
                c.close();
            }
        }

        db.close();
    }

    private long insertShoppingList(SQLiteDatabase db) {
        // Create ContentValues of what you want to insert
        ContentValues contentValues = TestUtilities.createShoppingListValues();

        // Insert ContentValues into database and get a row ID back
        long rowId = db.insert(ShoppingContract.ShoppingListEntry.TABLE_NAME, null, contentValues);
        assertTrue("Error: Database insert of shopping list row failed: " + Long.toString(rowId), rowId > 0);

        // Query the database and receive a Cursor back
        Cursor c = db.query(ShoppingContract.ShoppingListEntry.TABLE_NAME,
                null,
                ShoppingContract.ShoppingListEntry._ID + " = ?",
                new String[]{ Long.toString(rowId) },
                null, null,
                null);

        // Move the cursor to a valid database row
        assertTrue("Error: No records returned from shopping list query", c.moveToFirst());

        // Validate data in resulting Cursor with the original ContentValues
        TestUtilities.validateCurrentRecord("Error: Shopping list values are not correct", c, contentValues);

        // Finally, close the cursor
        c.close();

        return rowId;
    }

    /*
        Test that we can insert and query the shopping list table.
    */
    public void testShoppingListTable() {
        // First step: Get reference to writable database
        SQLiteDatabase db = ShoppingDbHelper.getInstance(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // Insert ContentValues into database and get a row ID back
        insertShoppingList(db);

        // Finally, close the database
        db.close();
    }

    private long insertCategory(SQLiteDatabase db) {
        // Create ContentValues of what you want to insert
        ContentValues contentValues = TestUtilities.createDairyCategoryValues();

        // Insert ContentValues into database and get a row ID back
        long rowId = db.insert(ShoppingContract.CategoryEntry.TABLE_NAME, null, contentValues);
        assertTrue("Error: Database insert of category row failed: " + Long.toString(rowId), rowId > 0);

        // Query the database and receive a Cursor back
        Cursor c = db.query(ShoppingContract.CategoryEntry.TABLE_NAME,
                null,
                ShoppingContract.CategoryEntry._ID + " = ?",
                new String[]{ Long.toString(rowId) },
                null, null,
                null);

        // Move the cursor to a valid database row
        assertTrue("Error: No records returned from category query", c.moveToFirst());

        // Validate data in resulting Cursor with the original ContentValues
        TestUtilities.validateCurrentRecord("Error: Category values are not correct", c, contentValues);

        // Finally, close the cursor
        c.close();

        return rowId;
    }

    /*
        Test that we can insert and query the category table.
    */
    public void testCategoryTable() {
        // First step: Get reference to writable database
        SQLiteDatabase db = ShoppingDbHelper.getInstance(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // Insert ContentValues into database and get a row ID back
        insertCategory(db);

        // Finally, close the database
        db.close();
    }

    private long insertProduct(SQLiteDatabase db) {
        // Insert product into database and get a row ID back
        long categoryId = insertCategory(db);

        // Create ContentValues of what you want to insert
        ContentValues contentValues = TestUtilities.createDairyProductValues(categoryId);

        // Insert ContentValues into database and get a row ID back
        long rowId = db.insert(ShoppingContract.ProductEntry.TABLE_NAME, null, contentValues);
        assertTrue("Error: Database insert of product row failed: " + Long.toString(rowId), rowId > 0);

        // Query the database and receive a Cursor back
        Cursor c = db.query(ShoppingContract.ProductEntry.TABLE_NAME,
                null,
                ShoppingContract.ProductEntry._ID + " = ?",
                new String[]{ Long.toString(rowId) },
                null, null,
                null);

        // Move the cursor to a valid database row
        assertTrue("Error: No records returned from product query", c.moveToFirst());

        // Validate data in resulting Cursor with the original ContentValues
        TestUtilities.validateCurrentRecord("Error: Product values are not correct", c, contentValues);

        // Finally, close the cursor
        c.close();

        return rowId;
    }

    /*
        Test that we can insert and query the product table.
    */
    public void testProductTable() {
        // First step: Get reference to writable database
        SQLiteDatabase db = ShoppingDbHelper.getInstance(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // Insert ContentValues into database and get a row ID back
        insertProduct(db);

        // Finally, close the database
        db.close();
    }

    private long insertShop(SQLiteDatabase db) {
        // Create ContentValues of what you want to insert
        ContentValues contentValues = TestUtilities.createDonaldDuckShopValues();

        // Insert ContentValues into database and get a row ID back
        long rowId = db.insert(ShoppingContract.ShopEntry.TABLE_NAME, null, contentValues);
        assertTrue("Error: Database insert of shop row failed: " + Long.toString(rowId), rowId > 0);

        // Query the database and receive a Cursor back
        Cursor c = db.query(ShoppingContract.ShopEntry.TABLE_NAME,
                null,
                ShoppingContract.ShopEntry._ID + " = ?",
                new String[]{ Long.toString(rowId) },
                null, null,
                null);

        // Move the cursor to a valid database row
        assertTrue("Error: No records returned from shop query", c.moveToFirst());

        // Validate data in resulting Cursor with the original ContentValues
        TestUtilities.validateCurrentRecord("Error: Shop values are not correct", c, contentValues);

        // Finally, close the cursor
        c.close();

        return rowId;
    }

    /*
        Test that we can insert and query the product table.
    */
    public void testShopTable() {
        // First step: Get reference to writable database
        SQLiteDatabase db = ShoppingDbHelper.getInstance(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // Insert ContentValues into database and get a row ID back
        insertShop(db);

        // Finally, close the database
        db.close();
    }

    /*
        Test that we can insert and query the shopping list products table
     */
    public void testShoppingListProductsTable() {
        // First step: Get reference to writable database
        SQLiteDatabase db = ShoppingDbHelper.getInstance(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // Insert shopping into database and get a row ID back
        long shoppingListId = insertShoppingList(db);

        // Insert product into database and get a row ID back
        long productId = insertProduct(db);

        // Insert shop into database and get a row ID back
        long shopId = insertShop(db);

        // Create ContentValues of what you want to insert
        ContentValues contentValues = TestUtilities.createShoppingListProductsValues(shoppingListId, productId, shopId);

        // Insert ContentValues into database and get a row ID back
        long rowId = db.insert(ShoppingContract.ShoppingListProductsEntry.TABLE_NAME, null, contentValues);
        assertTrue("Error: Database insert of shopping list products entry failed: " + Long.toString(rowId), rowId > 0);

        // Query the database and receive a Cursor back
        Cursor c = db.query(ShoppingContract.ShoppingListProductsEntry.TABLE_NAME,
                null,
                ShoppingContract.ShoppingListProductsEntry._ID + " = ?",
                new String[]{ Long.toString(rowId) },
                null, null,
                null);

        // Move the cursor to a valid database row
        assertTrue("Error: No records returned from shopping list products query", c.moveToFirst());

        // Validate data in resulting Cursor with the original ContentValues
        TestUtilities.validateCurrentRecord("Error: Shopping list products entry values are not correct", c, contentValues);

        // Finally, close the cursor and database
        c.close();
        db.close();
    }
}
