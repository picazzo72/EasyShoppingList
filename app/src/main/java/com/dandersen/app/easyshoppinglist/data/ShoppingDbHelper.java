package com.dandersen.app.easyshoppinglist.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Dan on 23-05-2016.
 */
public class ShoppingDbHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = ShoppingDbHelper.class.getSimpleName();

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 5;

    static final String DATABASE_NAME = "shopping.db";

    public ShoppingDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create databases
        createDatabases(sqLiteDatabase);

        // Insert default values
        populateDatabase(sqLiteDatabase);
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

                // Name of owner
                ShoppingContract.ShopEntry.COLUMN_NAME + " TEXT NOT NULL, " +

                // Address
                ShoppingContract.ShopEntry.COLUMN_ADDRESS + " TEXT NOT NULL, " +

                // City
                ShoppingContract.ShopEntry.COLUMN_CITY + " TEXT NOT NULL, " +

                // State
                ShoppingContract.ShopEntry.COLUMN_STATE + " TEXT DEFAULT NULL, " +

                // Country
                ShoppingContract.ShopEntry.COLUMN_COUNTRY + " TEXT DEFAULT NULL, " +

                // Homepage
                ShoppingContract.ShopEntry.COLUMN_HOMEPAGE + " TEXT DEFAULT NULL);";


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

    private static Map mCategoryProducts = new HashMap();

    private void populateDatabase(SQLiteDatabase db) {
        if (mCategoryProducts.isEmpty()) {
            mCategoryProducts.put("Mælkeprodukter", new String[]{
                    "Minimælk", "Letmælk", "Sødmælk", "Pære/banan yoghurt", "Yoghurt naturel",
                    "Peach melba yoghurt", "Piskefløde 45%", "Cremefine"});

            mCategoryProducts.put("Grøntsager", new String[]{
                    "Agurk", "Icebergsalat", "Tomat", "Avocado", "Squash", "Salathoved",
                    "Forårsløg", "Champignon", "Hvidløg", "Kartofler", "Løg" ,
                    "Asparges" , "Gulerødder" });


            mCategoryProducts.put("Frugt", new String[]{
                    "Æble", "Banan", "Pære", "Jordbær", "Vandmelon", "Honningmelon",
                    "Appelsin", "Citron", "Vindruer"});


            mCategoryProducts.put("Pålæg", new String[]{
                    "Leverpostej", "Spegepølse", "Rullepølse", "Hamburgerryg", "Blodpølse",
                    "Røget medister"});

            mCategoryProducts.put("Fersk kød", new String[]{
                    "Hk. oksekød", "Hk. svinekød", "Medisterpølse", "Svinekotelet",
                    "Entrecote"});

            mCategoryProducts.put("Fersk fisk", new String[]{
                    "Røget makret", "Laksefilet", "Torskefilet", "Rødspættefilet"});

            mCategoryProducts.put("Delikatesse", new String[]{
                    "Leverpostej, varm", "Spegepølse", "Roastbeef", "Kylling, stegt"});

            mCategoryProducts.put("Frosne grøntsager", new String[]{
                    "Ærter", "Karotter", "Bønner", "Suppeurter"});

            mCategoryProducts.put("Kaffe/the", new String[]{
                    "Karat kaffe", "Karat instant", "Kaffebønner", "Kamillete"});

            mCategoryProducts.put("Øl/vand", new String[]{
                    "Carlsberg", "Tuborg", "Tuborg Classic", "Coca/cola", "Harboe cola" ,
                    "Citronvand" , "Rød sodavand" });

            mCategoryProducts.put("Konserves", new String[]{
                    "Makrel i tomat", "Torskerogn", "Hk. tomater", "Flåede tomater",
                    "Tomatpure", "Asparges", "Aspargessnittet", "Ananas i skiver"});

            mCategoryProducts.put("Morgenmadsprodukter", new String[]{
                    "Guldmüsli", "Økologisk müsli", "Havregryn", "Chokolademüsli",
                    "Corn flakes"});

            mCategoryProducts.put("Brød", new String[]{
                    "Rugbrød", "Franskbrød"});
        }

        { // Insert categories (bulk)
            int insertCategoriesCount = 0;
            int insertProductsCount = 0;

            try {
                db.beginTransaction();
                Iterator it = mCategoryProducts.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();

                    // Insert category
                    ContentValues categoryValues = new ContentValues();
                    categoryValues.put(ShoppingContract.CategoryEntry.COLUMN_NAME, (String) pair.getKey());
                    long categoryId = db.insert(ShoppingContract.CategoryEntry.TABLE_NAME, null, categoryValues);
                    if (categoryId != -1) {
                        ++insertCategoriesCount;

                        // Insert products
                        String[] products = (String[]) pair.getValue();
                        for (int i=0, count = products.length; i < count; ++i) {
                            ContentValues productValues = new ContentValues();
                            productValues.put(ShoppingContract.ProductEntry.COLUMN_NAME, products[i]);
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
            Log.v(LOG_TAG, "DSA LOG - " + insertCategoriesCount + " categories inserted, "
                    + insertProductsCount + " products inserted");
        }
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
