package com.dandersen.app.easyshoppinglist.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Dan on 23-05-2016.
 */
public class ShoppingDbHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = ShoppingDbHelper.class.getSimpleName();

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 8;

    static final String DATABASE_NAME = "shopping.db";

    public ShoppingDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create databases
        createDatabases(sqLiteDatabase);

        // Insert default values
        Utilities.populateDatabase(sqLiteDatabase);
    }

    private void createDatabases(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_SHOPPING_LIST_TABLE = "CREATE TABLE " + ShoppingContract.ShoppingListEntry.TABLE_NAME + " (" +
                ShoppingContract.ShoppingListEntry._ID + " INTEGER PRIMARY KEY," +

                // The shopping list date
                ShoppingContract.ShoppingListEntry.COLUMN_DATE + " INTEGER NOT NULL, " +

                // Amount
                ShoppingContract.ShoppingListEntry.COLUMN_AMOUNT + " REAL, " +

                // Note
                ShoppingContract.ShoppingListEntry.COLUMN_NOTE + " TEXT);";


        final String SQL_CREATE_CATEGORY_TABLE = "CREATE TABLE " + ShoppingContract.CategoryEntry.TABLE_NAME + " (" +
                ShoppingContract.CategoryEntry._ID + " INTEGER PRIMARY KEY," +

                // Category name
                ShoppingContract.CategoryEntry.COLUMN_NAME + " TEXT NOT NULL, " +

                // Category description
                ShoppingContract.CategoryEntry.COLUMN_DESCRIPTION + " TEXT, " +

                // To assure the application have just one category with a specific name
                // we create a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + ShoppingContract.CategoryEntry.COLUMN_NAME + ") ON CONFLICT REPLACE);";


        final String SQL_CREATE_PRODUCT_TABLE = "CREATE TABLE " + ShoppingContract.ProductEntry.TABLE_NAME + " (" +
                ShoppingContract.ProductEntry._ID + " INTEGER PRIMARY KEY," +

                // Product name
                ShoppingContract.ProductEntry.COLUMN_NAME + " TEXT NOT NULL, " +

                // Category id
                ShoppingContract.ProductEntry.COLUMN_CATEGORY_ID + " INTEGER NOT NULL, " +

                // Favorite - boolean (0 or 1)
                ShoppingContract.ProductEntry.COLUMN_FAVORITE + " INTEGER NOT NULL DEFAULT 0, " +

                // Custom product (user defined) - boolean (0 or 1)
                ShoppingContract.ProductEntry.COLUMN_CUSTOM + " INTEGER NOT NULL DEFAULT 0, " +

                // Date last used, stored as long in milliseconds since the epoch
                ShoppingContract.ProductEntry.COLUMN_LAST_USED + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +

                // Use count
                ShoppingContract.ProductEntry.COLUMN_USE_COUNT + " INTEGER NOT NULL DEFAULT 0, " +

                // Set up the housing column as a foreign key to housing table.
                " FOREIGN KEY (" + ShoppingContract.ProductEntry.COLUMN_CATEGORY_ID + ") REFERENCES " +
                ShoppingContract.CategoryEntry.TABLE_NAME + " (" + ShoppingContract.CategoryEntry._ID + "), " +

                // To assure the application have just one product name per category
                // we create a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + ShoppingContract.ProductEntry.COLUMN_NAME + ", " +
                ShoppingContract.ProductEntry.COLUMN_CATEGORY_ID + ") ON CONFLICT REPLACE);";


        final String SQL_CREATE_SHOP_TABLE = "CREATE TABLE " + ShoppingContract.ShopEntry.TABLE_NAME + " (" +
                ShoppingContract.ShopEntry._ID + " INTEGER PRIMARY KEY," +

                // Place Id
                ShoppingContract.ShopEntry.COLUMN_PLACE_ID + " TEXT NOT NULL, " +

                // Location
                ShoppingContract.ShopEntry.COLUMN_LOCATION + " TEXT DEFAULT NULL, " +

                // Shop name
                ShoppingContract.ShopEntry.COLUMN_NAME + " TEXT NOT NULL, " +

                // Formatted address
                ShoppingContract.ShopEntry.COLUMN_FORMATTED_ADDRESS + " TEXT DEFAULT NULL, " +

                // Street
                ShoppingContract.ShopEntry.COLUMN_STREET + " TEXT DEFAULT NULL, " +

                // Street number
                ShoppingContract.ShopEntry.COLUMN_STREET_NUMBER + " TEXT DEFAULT NULL, " +

                // City
                ShoppingContract.ShopEntry.COLUMN_CITY + " TEXT DEFAULT NULL, " +

                // Postal code
                ShoppingContract.ShopEntry.COLUMN_POSTAL_CODE + " TEXT DEFAULT NULL, " +

                // State
                ShoppingContract.ShopEntry.COLUMN_STATE + " TEXT DEFAULT NULL, " +

                // Country
                ShoppingContract.ShopEntry.COLUMN_COUNTRY + " TEXT DEFAULT NULL, " +

                // Phone number
                ShoppingContract.ShopEntry.COLUMN_PHONE_NUMBER + " TEXT DEFAULT NULL, " +

                // Website
                ShoppingContract.ShopEntry.COLUMN_WEBSITE + " TEXT DEFAULT NULL, " +

                // Date created, stored as long in milliseconds since the epoch
                ShoppingContract.ShopEntry.COLUMN_CREATED + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +

                // To assure the application have just one shop with a certain PLACE_ID
                // we create a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + ShoppingContract.ShopEntry.COLUMN_PLACE_ID + ") ON CONFLICT REPLACE);";



        final String SQL_CREATE_SHOPPING_LIST_PRODUCTS_TABLE = "CREATE TABLE " +
                ShoppingContract.ShoppingListProductsEntry.TABLE_NAME + " (" +
                ShoppingContract.ShoppingListProductsEntry._ID + " INTEGER PRIMARY KEY," +

                // Product id
                ShoppingContract.ShoppingListProductsEntry.COLUMN_PRODUCT_ID + " INTEGER NOT NULL, " +

                // Shopping list id
                ShoppingContract.ShoppingListProductsEntry.COLUMN_SHOPPING_LIST_ID + " INTEGER NOT NULL, " +

                // Shop id
                ShoppingContract.ShoppingListProductsEntry.COLUMN_SHOP_ID + " INTEGER, " +

                // Sort order
                ShoppingContract.ShoppingListProductsEntry.COLUMN_SORT_ORDER + " INTEGER NOT NULL, " +

                // Set up the supplier column as a foreign key to supplier table.
                " FOREIGN KEY (" + ShoppingContract.ShoppingListProductsEntry.COLUMN_PRODUCT_ID + ") REFERENCES " +
                ShoppingContract.ProductEntry.TABLE_NAME + " (" + ShoppingContract.ProductEntry._ID + "), " +

                // Set up the supplier column as a foreign key to supplier table.
                " FOREIGN KEY (" + ShoppingContract.ShoppingListProductsEntry.COLUMN_PRODUCT_ID + ") REFERENCES " +
                ShoppingContract.ProductEntry.TABLE_NAME + " (" + ShoppingContract.ProductEntry._ID + "), " +

                // Set up the housing column as a foreign key to housing table.
                " FOREIGN KEY (" + ShoppingContract.ShoppingListProductsEntry.COLUMN_SHOP_ID + ") REFERENCES " +
                ShoppingContract.ShopEntry.TABLE_NAME + " (" + ShoppingContract.ShopEntry._ID + "));";

        sqLiteDatabase.execSQL(SQL_CREATE_SHOPPING_LIST_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_CATEGORY_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_PRODUCT_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SHOP_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SHOPPING_LIST_PRODUCTS_TABLE);

        Log.v(LOG_TAG, "DSA LOG - Databases have been created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        // TODO
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ShoppingContract.CategoryEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ShoppingContract.ProductEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ShoppingContract.ShopEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ShoppingContract.ShoppingListEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ShoppingContract.ShoppingListProductsEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
