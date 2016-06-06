package com.dandersen.app.easyshoppinglist.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Dan on 26-05-2016.
 */
public class ShoppingProvider extends ContentProvider {

    private static final String LOG_TAG = ShoppingProvider.class.getSimpleName();

    // The URI matcher used by this content provider
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    // Database helper
    private ShoppingDbHelper mOpenHelper;

    // URI definitions
    static final int CATEGORY = 100;                // Category list
    static final int PRODUCT = 200;                 // Product list
    static final int PRODUCT_BY_CATEGORY = 201;     // Product list for category
    static final int PRODUCT_WITH_CATEGORY = 202;   // Product list with category (with category columns)
    static final int SHOPPING_LIST = 300;           // Shopping lists
    static final int SHOPPING_LIST_ACTIVE = 301;    // Currently active shopping list
    static final int SHOP = 400;                    // Shop list
    static final int SHOPPING_LIST_PRODUCTS = 500;  // Shopping list/products/shop rel table

    private static final SQLiteQueryBuilder sProductByCategoryQueryBuilder;

    static{
        sProductByCategoryQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //product INNER JOIN category ON product.category_id = category._id
        sProductByCategoryQueryBuilder.setTables(
                ShoppingContract.ProductEntry.TABLE_NAME + " INNER JOIN " +
                        ShoppingContract.CategoryEntry.TABLE_NAME +
                        " ON " + ShoppingContract.ProductEntry.TABLE_NAME +
                        "." + ShoppingContract.ProductEntry.COLUMN_CATEGORY_ID +
                        " = " + ShoppingContract.CategoryEntry.TABLE_NAME +
                        "." + ShoppingContract.CategoryEntry._ID);
    }

    //category.name = ?
    private static final String sCategorySelection =
            ShoppingContract.CategoryEntry.TABLE_NAME+
                    "." + ShoppingContract.CategoryEntry.COLUMN_NAME + " = ? ";

    /*
        This UriMatcher will match each URI definition defined above.
     */
    static UriMatcher buildUriMatcher() {
        // The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ShoppingContract.CONTENT_AUTHORITY;

        // The addURI function is used to match each of the types
        matcher.addURI(authority, ShoppingContract.PATH_CATEGORY, CATEGORY);

        matcher.addURI(authority, ShoppingContract.PATH_PRODUCT, PRODUCT);
        matcher.addURI(authority, ShoppingContract.PATH_PRODUCT + "/*", PRODUCT_BY_CATEGORY);

        matcher.addURI(authority, ShoppingContract.PATH_PRODUCT_WITH_CATEGORY, PRODUCT_WITH_CATEGORY);

        matcher.addURI(authority, ShoppingContract.PATH_SHOPPING_LIST, SHOPPING_LIST);
        matcher.addURI(authority, ShoppingContract.PATH_SHOPPING_LIST + "/*", SHOPPING_LIST_ACTIVE);

        matcher.addURI(authority, ShoppingContract.PATH_SHOP, SHOP);

        matcher.addURI(authority, ShoppingContract.PATH_SHOPPING_LIST_PRODUCTS, SHOPPING_LIST_PRODUCTS);

        // Return the new matcher!
        return matcher;
    }

    /*
        We create a new ShoppingDbHelper for later use
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new ShoppingDbHelper(getContext());
        return true;
    }

    /*
        This function returns MIME types! Defines if it is a DIR og ITEM being returned
     */
    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // This case is the only one returning a single ITEM
            case SHOPPING_LIST_ACTIVE:
                return ShoppingContract.ShoppingListEntry.CONTENT_ITEM_TYPE;
            // The rest are DIR cases
            case CATEGORY:
                return ShoppingContract.CategoryEntry.CONTENT_TYPE;
            case PRODUCT:
                return ShoppingContract.ProductEntry.CONTENT_TYPE;
            case PRODUCT_BY_CATEGORY:
                return ShoppingContract.ProductEntry.CONTENT_TYPE;
            case PRODUCT_WITH_CATEGORY:
                return ShoppingContract.ProductEntry.CONTENT_TYPE;
            case SHOPPING_LIST:
                return ShoppingContract.ShoppingListEntry.CONTENT_TYPE;
            case SHOP:
                return ShoppingContract.ShopEntry.CONTENT_TYPE;
            case SHOPPING_LIST_PRODUCTS:
                return ShoppingContract.ShoppingListProductsEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    private Cursor getCategory(
            String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.v(LOG_TAG, "DSA LOG - Category list");

        return mOpenHelper.getReadableDatabase().query(
                ShoppingContract.CategoryEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getProduct(
            String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.v(LOG_TAG, "DSA LOG - Product list");

        return mOpenHelper.getReadableDatabase().query(
                ShoppingContract.ProductEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getProductWithCategory(
            String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.v(LOG_TAG, "DSA LOG - Product list");

        return sProductByCategoryQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getProductByCategory(Uri uri, String[] projection, String selection, String sortOrder) {
        String category = ShoppingContract.ProductEntry.getCategoryFromUri(uri);

        String[] selectionArgs = new String[]{ category };
        if (selection == null) {
            selection = "";
        }
        else if (!selection.isEmpty()) {
            selection += " AND ";
        }
        selection += sCategorySelection;

        StringBuilder selectionArgsBuilder = new StringBuilder();
        for (int i = 0; i < selectionArgs.length; i++) {
            selectionArgsBuilder.append(selectionArgs[i] + ", ");
        }
        Log.v(LOG_TAG, "DSA LOG - Product by category '" + selection + "' binds: " + selectionArgsBuilder.toString());

        return sProductByCategoryQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getShoppingList(
            String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.v(LOG_TAG, "DSA LOG - Shopping_list list");

        return mOpenHelper.getReadableDatabase().query(
                ShoppingContract.ShoppingListEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getShopList(
            String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.v(LOG_TAG, "DSA LOG - Shop list");

        return mOpenHelper.getReadableDatabase().query(
                ShoppingContract.ShopEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getShoppingListProducts(
            String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.v(LOG_TAG, "DSA LOG - Shopping list products rel");

        return mOpenHelper.getReadableDatabase().query(
                ShoppingContract.ShoppingListProductsEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "category"
            case CATEGORY:
            {
                retCursor = getCategory(projection, selection, selectionArgs, sortOrder);
                break;
            }
            // "product"
            case PRODUCT:
            {
                retCursor = getProduct(projection, selection, selectionArgs, sortOrder);
                break;
            }
            // "product/*"
            case PRODUCT_BY_CATEGORY:
            {
                retCursor = getProductByCategory(uri, projection, selection, sortOrder);
                break;
            }
            // "product_with_category"
            case PRODUCT_WITH_CATEGORY:
            {
                retCursor = getProductWithCategory(projection, selection, selectionArgs, sortOrder);
                break;
            }
            // "shopping_list"
            case SHOPPING_LIST:
            {
                retCursor = getShoppingList(projection, selection, selectionArgs, sortOrder);
                break;
            }
            // "shopping_list/*"
            case SHOPPING_LIST_ACTIVE: // TODO
            {
                retCursor = getShoppingList(projection, selection, selectionArgs, sortOrder);
                break;
            }
            case SHOP:
            {
                retCursor = getShopList(projection, selection, selectionArgs, sortOrder);
                break;
            }
            case SHOPPING_LIST_PRODUCTS:
            {
                retCursor = getShoppingListProducts(projection, selection, selectionArgs, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /*
        Handles inserts
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case CATEGORY:
            {
                long _id = db.insert(ShoppingContract.CategoryEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = ShoppingContract.CategoryEntry.buildCategoryUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case PRODUCT:
            {
                long _id = db.insert(ShoppingContract.ProductEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = ShoppingContract.ProductEntry.buildProductUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case SHOPPING_LIST:
            {
                long _id = db.insert(ShoppingContract.ShoppingListEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = ShoppingContract.ShoppingListEntry.buildShoppingListUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case SHOP:
            {
                long _id = db.insert(ShoppingContract.ShopEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = ShoppingContract.ShopEntry.buildShopUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case SHOPPING_LIST_PRODUCTS:
            {
                long _id = db.insert(ShoppingContract.ShoppingListProductsEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = ShoppingContract.ShoppingListProductsEntry.buildShoppingListProductsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Student: Start by getting a writable database
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int numberOfDeletedRows = 0;
        if (selection == null) selection = "1";

        // Student: Use the uriMatcher to match the WEATHER and LOCATION URI's we are going to
        // handle.  If it doesn't match these, throw an UnsupportedOperationException.
        final int match = sUriMatcher.match(uri);
        try {
            switch (match) {
                case CATEGORY:
                {
                    numberOfDeletedRows = db.delete(ShoppingContract.CategoryEntry.TABLE_NAME, selection, selectionArgs);
                    break;
                }
                case PRODUCT:
                {
                    numberOfDeletedRows = db.delete(ShoppingContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                    break;
                }
                case SHOPPING_LIST:
                {
                    numberOfDeletedRows = db.delete(ShoppingContract.ShoppingListEntry.TABLE_NAME, selection, selectionArgs);
                    break;
                }
                case SHOP:
                {
                    numberOfDeletedRows = db.delete(ShoppingContract.ShopEntry.TABLE_NAME, selection, selectionArgs);
                    break;
                }
                case SHOPPING_LIST_PRODUCTS:
                {
                    numberOfDeletedRows = db.delete(ShoppingContract.ShoppingListProductsEntry.TABLE_NAME, selection, selectionArgs);
                    break;
                }
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        finally {
            db.close();
        }

        // Student: A null value deletes all rows.  In my implementation of this, I only notified
        // the uri listeners (using the content resolver) if the rowsDeleted != 0 or the selection
        // is null.
        // Oh, and you should notify the listeners here.
        if (numberOfDeletedRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Student: return the actual rows deleted
        return numberOfDeletedRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Start by getting a writable database
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int numberOfUpdatedRows = 0;

        // Use the uriMatcher to match the WEATHER and LOCATION URI's we are going to
        // handle.  If it doesn't match these, throw an UnsupportedOperationException.
        final int match = sUriMatcher.match(uri);
        try {
            switch (match) {
                case CATEGORY:
                {
                    numberOfUpdatedRows = db.update(ShoppingContract.CategoryEntry.TABLE_NAME, values, selection, selectionArgs);
                    break;
                }
                case PRODUCT:
                {
                    numberOfUpdatedRows = db.update(ShoppingContract.ProductEntry.TABLE_NAME, values, selection, selectionArgs);
                    break;
                }
                case SHOPPING_LIST:
                {
                    numberOfUpdatedRows = db.update(ShoppingContract.ShoppingListEntry.TABLE_NAME, values, selection, selectionArgs);
                    break;
                }
                case SHOP:
                {
                    numberOfUpdatedRows = db.update(ShoppingContract.ShopEntry.TABLE_NAME, values, selection, selectionArgs);
                    break;
                }
                case SHOPPING_LIST_PRODUCTS:
                {
                    numberOfUpdatedRows = db.update(ShoppingContract.ShoppingListProductsEntry.TABLE_NAME, values, selection, selectionArgs);
                    break;
                }
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        finally {
            db.close();
        }

        // Notify the listeners if rows were updated
        if (numberOfUpdatedRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Student: return the actual rows deleted
        return numberOfUpdatedRows;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SHOP:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(ShoppingContract.ShopEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // This is a method specifically to assist the testing framework in running smoothly.
    // You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
