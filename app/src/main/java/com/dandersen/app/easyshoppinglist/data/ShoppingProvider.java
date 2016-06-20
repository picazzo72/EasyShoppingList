package com.dandersen.app.easyshoppinglist.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by Dan on 26-05-2016.
 * Content provider for accessing the database.
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
    static final int SHOP_INFO = 401;               // Shop entry info
    static final int SHOPPING_LIST_PRODUCTS = 500;  // Shopping list/products/shop rel table
    static final int SHOP_CATEGORIES = 600;         // Shop categories
    static final int SHOP_CATEGORY_INFO = 601;      // Shop category info

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

    private static final SQLiteQueryBuilder sShopCategoriesQueryBuilder;

    static{
        sShopCategoriesQueryBuilder = new SQLiteQueryBuilder();

        //category INNER JOIN shop_category ON category.id = shop_category.category_id
        //         INNER JOIN shop ON shop.id = shop_category.shop_id
        sShopCategoriesQueryBuilder.setTables(
                ShoppingContract.CategoryEntry.TABLE_NAME + " INNER JOIN " +
                        ShoppingContract.ShopCategoryEntry.TABLE_NAME +
                        " ON " + ShoppingContract.CategoryEntry.TABLE_NAME +
                        "." + ShoppingContract.CategoryEntry._ID +
                        " = " + ShoppingContract.ShopCategoryEntry.TABLE_NAME +
                        "." + ShoppingContract.ShopCategoryEntry.COLUMN_CATEGORY_ID +
                        " INNER JOIN " + ShoppingContract.ShopEntry.TABLE_NAME +
                        " ON " + ShoppingContract.ShopEntry.TABLE_NAME +
                        "." + ShoppingContract.ShopEntry._ID +
                        " = " + ShoppingContract.ShopCategoryEntry.TABLE_NAME +
                        "." + ShoppingContract.ShopCategoryEntry.COLUMN_SHOP_ID);
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
        matcher.addURI(authority, ShoppingContract.PATH_SHOP + "/*", SHOP_INFO);

        matcher.addURI(authority, ShoppingContract.PATH_SHOPPING_LIST_PRODUCTS, SHOPPING_LIST_PRODUCTS);

        matcher.addURI(authority, ShoppingContract.PATH_SHOP_CATEGORY, SHOP_CATEGORIES);
        matcher.addURI(authority, ShoppingContract.PATH_SHOP_CATEGORY + "/*", SHOP_CATEGORY_INFO);

        // Return the new matcher!
        return matcher;
    }

    /*
        We create a new ShoppingDbHelper for later use
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = ShoppingDbHelper.getInstance(getContext());
        Log.i(LOG_TAG, "DSA LOG - onCreate - ShoppingDbHelper created");
        return true;
    }

    /*
        This function returns MIME types! Defines if it is a DIR og ITEM being returned
     */
    @Override
    public String getType(@NonNull Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // These cases return a single ITEM
            case SHOPPING_LIST_ACTIVE:
                return ShoppingContract.ShoppingListEntry.CONTENT_ITEM_TYPE;
            case SHOP_INFO:
                return ShoppingContract.ShopEntry.CONTENT_ITEM_TYPE;
            case SHOP_CATEGORY_INFO:
                return ShoppingContract.ShopCategoryEntry.CONTENT_ITEM_TYPE;
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
            case SHOP_CATEGORIES:
                return ShoppingContract.ShopCategoryEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    private Cursor getCategory(
            String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.i(LOG_TAG, "DSA LOG - Category list");

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
        Log.i(LOG_TAG, "DSA LOG - Product list");

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

        Log.i(LOG_TAG, "DSA LOG - Product by category '" + selection + "' binds: " + stringArrayToString(selectionArgs));

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
        Log.i(LOG_TAG, "DSA LOG - Shopping_list list");

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
        Log.i(LOG_TAG, "DSA LOG - Shop list '" + stringArrayToString(projection) + "'");

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

    private Cursor getShopEntry(Uri uri, String[] projection) {
        String shopIdFromUri = ShoppingContract.ShopEntry.getShopIdFromUri(uri);

        String selection = ShoppingContract.ShopEntry._ID + "= ?";
        String[] selectionArgs = new String[]{ shopIdFromUri };

        Log.i(LOG_TAG, "DSA LOG - Shop by id '" + selection + "' binds: " + stringArrayToString(selectionArgs));

        return mOpenHelper.getReadableDatabase().query(
                ShoppingContract.ShopEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
    }

    private Cursor getShopCategories(
            String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.i(LOG_TAG, "DSA LOG - Shop categories list");
        Log.i(LOG_TAG, "DSA LOG - '" + sShopCategoriesQueryBuilder.getTables() + "' - '" +
                selection + "' - " + stringArrayToString(selectionArgs));

        return sShopCategoriesQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getShopCategoryEntry(Uri uri, String[] projection) {
        String shopIdFromUri = ShoppingContract.ShopCategoryEntry.getShopCategoryIdFromUri(uri);

        String selection = ShoppingContract.ShopCategoryEntry._ID + "= ?";
        String[] selectionArgs = new String[]{ shopIdFromUri };

        Log.i(LOG_TAG, "DSA LOG - Shop category by id '" + selection + "' binds: " + stringArrayToString(selectionArgs));

        return mOpenHelper.getReadableDatabase().query(
                ShoppingContract.ShopCategoryEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
    }

    private Cursor getShoppingListProducts(
            String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.i(LOG_TAG, "DSA LOG - Shopping list products rel");

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
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
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
            case SHOP_INFO:
            {
                retCursor = getShopEntry(uri, projection);
                break;
            }
            case SHOP_CATEGORIES:
            {
                retCursor = getShopCategories(projection, selection, selectionArgs, sortOrder);
                break;
            }
            case SHOP_CATEGORY_INFO:
            {
                retCursor = getShopCategoryEntry(uri, projection);
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
        if (getContext() != null) {
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return retCursor;
    }

    /*
        Handles inserts
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
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
            case SHOP_CATEGORIES:
            {
                long _id = db.insert(ShoppingContract.ShopCategoryEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = ShoppingContract.ShopCategoryEntry.buildShopCategoryUri(_id);
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
                throw new UnsupportedOperationException("Unknown uri: " + uri + " Match: " + match);
        }
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return returnUri;
    }

    private int deleteShopCategoryEntry(Uri uri) {
        String shopCategoryIdFromUri = ShoppingContract.ShopCategoryEntry.getShopCategoryIdFromUri(uri);

        String selection = ShoppingContract.ShopCategoryEntry._ID + "= ?";
        String[] selectionArgs = new String[]{ shopCategoryIdFromUri };

        Log.i(LOG_TAG, "DSA LOG - Delete shop category by id '" + selection + "' binds: " + stringArrayToString(selectionArgs));

        return mOpenHelper.getWritableDatabase().delete(
                ShoppingContract.ShopCategoryEntry.TABLE_NAME,
                selection,
                selectionArgs
        );
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Start by getting a writable database
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int numberOfDeletedRows = 0;
        if (selection == null) selection = "1";

        // Use the Uri matcher to match the uri to the right type
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
                case SHOP_CATEGORIES:
                {
                    numberOfDeletedRows = db.delete(ShoppingContract.ShopCategoryEntry.TABLE_NAME, selection, selectionArgs);
                    break;
                }
                case SHOP_CATEGORY_INFO:
                {
                    numberOfDeletedRows = deleteShopCategoryEntry(uri);
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
            //db.close();
        }

        // Student: A null value deletes all rows.  In my implementation of this, I only notified
        // the uri listeners (using the content resolver) if the rowsDeleted != 0 or the selection
        // is null.
        // Oh, and you should notify the listeners here.
        if (numberOfDeletedRows != 0) {
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }

        // Student: return the actual rows deleted
        return numberOfDeletedRows;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Start by getting a writable database
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int numberOfUpdatedRows = 0;

        // Use the uriMatcher to match the URI's we are going to handle.
        // If it doesn't match these, throw an UnsupportedOperationException.
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
                case SHOP_INFO:
                {
                    numberOfUpdatedRows = db.update(ShoppingContract.ShopEntry.TABLE_NAME, values, selection, selectionArgs);
                    break;
                }
                case SHOP_CATEGORIES:
                case SHOP_CATEGORY_INFO:
                {
                    numberOfUpdatedRows = db.update(ShoppingContract.ShopCategoryEntry.TABLE_NAME, values, selection, selectionArgs);
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
            //db.close();
        }

        // Notify the listeners if rows were updated
        if (numberOfUpdatedRows != 0) {
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }

        // Student: return the actual rows deleted
        return numberOfUpdatedRows;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SHOP: {
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
                if (getContext() != null) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return returnCount;
            }
            case SHOP_CATEGORIES:
            {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(ShoppingContract.ShopCategoryEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                if (getContext() != null) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return returnCount;
            }
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
        Log.i(LOG_TAG, "DSA LOG - shutdown - Database open helper was closed");
        super.shutdown();
    }

    private String stringArrayToString(String[] selectionArgs) {
        final String COMMA_SEP = ", ";

        StringBuilder selectionArgsBuilder = new StringBuilder();
        if (selectionArgs != null) {
            for (String selectionArg : selectionArgs) {
                if (selectionArgsBuilder.length() != 0) selectionArgsBuilder.append(COMMA_SEP);
                selectionArgsBuilder.append(selectionArg);
            }
        }
        return selectionArgsBuilder.toString();
    }
}
