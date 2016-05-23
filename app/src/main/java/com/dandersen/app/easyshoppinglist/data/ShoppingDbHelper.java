package com.dandersen.app.easyshoppinglist.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Dan on 23-05-2016.
 */
public class ShoppingDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "shopping.db";

    public ShoppingDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_SHOPPING_LIST_TABLE = "CREATE TABLE " + ShoppingContract.ShoppingListEntry.TABLE_NAME + " (" +
                ShoppingContract.ShoppingListEntry._ID + " INTEGER PRIMARY KEY," +

                // The shopping list date
                ShoppingContract.ShoppingListEntry.COLUMN_DATE + " INTEGER NOT NULL, " +

                // Amount
                ShoppingContract.ShoppingListEntry.COLUMN_AMOUNT + " REAL, " +

                // Note
                ShoppingContract.ShoppingListEntry.COLUMN_NOTE + " TEXT);";


        final String SQL_CREATE_PRODUCT_TABLE = "CREATE TABLE " + ShoppingContract.ProductEntry.TABLE_NAME + " (" +
                ShoppingContract.ProductEntry._ID + " INTEGER PRIMARY KEY," +

                // Product name
                ShoppingContract.ProductEntry.COLUMN_NAME + " TEXT NOT NULL, " +

                // Date last used, stored as long in milliseconds since the epoch
                ShoppingContract.ProductEntry.COLUMN_LAST_USED + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +

                // Use count
                ShoppingContract.ProductEntry.COLUMN_USE_COUNT + " INTEGER);";


        final String SQL_CREATE_SHOP_TABLE = "CREATE TABLE " + ShoppingContract.ShopEntry.TABLE_NAME + " (" +
                ShoppingContract.ShopEntry._ID + " INTEGER PRIMARY KEY," +

                // Name of owner
                ShoppingContract.ShopEntry.COLUMN_NAME + " TEXT NOT NULL, " +

                // Address
                ShoppingContract.ShopEntry.COLUMN_ADDRESS + " TEXT NOT NULL, " +

                // City
                ShoppingContract.ShopEntry.COLUMN_CITY + " TEXT NOT NULL, " +

                // State
                ShoppingContract.ShopEntry.COLUMN_STATE + " TEXT, " +

                // Country
                ShoppingContract.ShopEntry.COLUMN_COUNTRY + " TEXT, " +

                // Homepage
                ShoppingContract.ShopEntry.COLUMN_HOMEPAGE + " TEXT);";


        final String SQL_CREATE_SHOPPING_LIST_PRODUCTS_TABLE = "CREATE TABLE " +
                ShoppingContract.ShoppingListProductsEntry.TABLE_NAME + " (" +
                ShoppingContract.ShoppingListProductsEntry._ID + " INTEGER PRIMARY KEY," +

                // Product id
                ShoppingContract.ShoppingListProductsEntry.COLUMN_PRODUCT_ID + " INTEGER NOT NULL, " +

                // Shopping list id
                ShoppingContract.ShoppingListProductsEntry.COLUMN_SHOPPING_LIST_ID + " INTEGER NOT NULL, " +

                // Shop id
                ShoppingContract.ShoppingListProductsEntry.COLUMN_SHOP_ID + " INTEGER, " +

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
        sqLiteDatabase.execSQL(SQL_CREATE_PRODUCT_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SHOP_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SHOPPING_LIST_PRODUCTS_TABLE);
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
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WeatherEntry.TABLE_NAME);
//        onCreate(sqLiteDatabase);
    }
}
