package com.dandersen.app.easyshoppinglist.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Created by Dan on 23-05-2016.
 * Contract for the database defining the tables.
 */
public class ShoppingContract {
    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.dandersen.app.easyshoppinglist";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.dandersen.meterreader/meter/ is a valid path for
    // looking at meter data. content://com.dandersen.meterreader/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    public static final String PATH_SHOPPING_LIST = "shoppinglist";
    public static final String PATH_CATEGORY = "category";
    public static final String PATH_PRODUCT = "product";
    public static final String PATH_SHOP = "shop";
    public static final String PATH_SHOP_CATEGORY = "shop_category";
    public static final String PATH_SHOPPING_LIST_PRODUCT = "shoppinglistproducts";

    // The following paths does not match table names
    public static final String PATH_PRODUCT_WITH_CATEGORY = "product_with_category";
    public static final String PATH_PRODUCT_ACTIVE_LIST = "product_active_shoppinglist";
    public static final String PATH_SHOP_ACTIVE_LIST = "shop_active_shoppinglist";

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    /* Inner class that defines the table contents of the meter table */
    public static final class ShoppingListEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SHOPPING_LIST).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SHOPPING_LIST;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SHOPPING_LIST;

        // Table name
        public static final String TABLE_NAME = PATH_SHOPPING_LIST;

        // Name
        public static final String COLUMN_NAME = "shopping_list_name";

        // Date, stored as long in milliseconds since the epoch
        public static final String COLUMN_CREATED = "shopping_list_created";

        // The amount
        public static final String COLUMN_AMOUNT = "shopping_list_amount";

        // Note
        public static final String COLUMN_NOTE = "shopping_list_note";

        public static Uri buildShoppingListUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildShoppingListActiveUri() {
            return CONTENT_URI.buildUpon().appendPath("active").build();
        }

        public static String getShoppingListIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static final class CategoryEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORY).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORY;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORY;

        // Table name
        public static final String TABLE_NAME = PATH_CATEGORY;

        // Name
        public static final String COLUMN_NAME = "category_name";

        // Description
        public static final String COLUMN_DESCRIPTION = "category_description";

        // Color
        public static final String COLUMN_COLOR = "category_color";

        // Sort order
        public static final String COLUMN_SORT_ORDER = "category_sort_order";

        public static Uri buildCategoryUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getCategoryIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static final class ProductEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PRODUCT).build();
        public static final Uri CONTENT_WITH_CATEGORY_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PRODUCT_WITH_CATEGORY).build();
        public static final Uri CONTENT_ACTIVE_LIST_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PRODUCT_ACTIVE_LIST).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;

        // Table name
        public static final String TABLE_NAME = PATH_PRODUCT;

        // Name
        public static final String COLUMN_NAME = "product_name";

        // Category
        public static final String COLUMN_CATEGORY_ID = "category_id";

        // Favorite - boolean (0 or 1)
        public static final String COLUMN_FAVORITE = "favorite";

        // Custom product (user defined) - boolean (0 or 1)
        public static final String COLUMN_CUSTOM = "custom";

        // Date, stored as long in milliseconds since the epoch
        public static final String COLUMN_LAST_USED = "last_used";

        // Use count
        public static final String COLUMN_USE_COUNT = "use_count";

        public static Uri buildProductCategory(String category) {
            return CONTENT_URI.buildUpon().appendPath(category).build();
        }

        public static Uri buildProductUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getCategoryFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static final class ShopEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SHOP).build();
        public static final Uri CONTENT_ACTIVE_LIST_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SHOP_ACTIVE_LIST).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SHOP;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SHOP;

        public static final String DEFAULT_STORE_PLACE_ID = "default_store_place_id";
        public static final long DEFAULT_STORE_ID = 1;

        // Table name
        public static final String TABLE_NAME = PATH_SHOP;

        // Place Id
        public static final String COLUMN_PLACE_ID = "place_id";

        // Location - latitude , longtitude
        public static final String COLUMN_LOCATION = "location";

        // Name
        public static final String COLUMN_NAME = "shop_name";

        // Formatted address
        public static final String COLUMN_FORMATTED_ADDRESS = "formatted_address";

        // Street
        public static final String COLUMN_STREET = "street";

        // Street number
        public static final String COLUMN_STREET_NUMBER = "street_number";

        // City
        public static final String COLUMN_CITY = "city";

        // Postal code
        public static final String COLUMN_POSTAL_CODE = "postal_code";

        // State
        public static final String COLUMN_STATE = "state";

        // Country
        public static final String COLUMN_COUNTRY = "country";

        // Phone number
        public static final String COLUMN_PHONE_NUMBER = "phone_number";

        // Website
        public static final String COLUMN_WEBSITE = "website";

        // Open now
        public static final String COLUMN_OPEN_NOW = "open_now";

        // Open hours
        public static final String COLUMN_OPENING_HOURS = "opening_hours";

        // Date, stored as long in milliseconds since the epoch
        public static final String COLUMN_CREATED = "shop_created";

        public static Uri buildShopUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getShopIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static final class ShopCategoryEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SHOP_CATEGORY).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SHOP_CATEGORY;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SHOP_CATEGORY;

        // Table name
        public static final String TABLE_NAME = PATH_SHOP_CATEGORY;

        // Shop id
        public static final String COLUMN_SHOP_ID = "shop_id";

        // Category id
        public static final String COLUMN_CATEGORY_ID = "category_id";

        // Sort order
        public static final String COLUMN_SORTORDER = "shop_category_sort_order";

        public static Uri buildShopCategoryUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getShopCategoryIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static final class ShoppingListProductEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SHOPPING_LIST_PRODUCT).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SHOPPING_LIST_PRODUCT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SHOPPING_LIST_PRODUCT;

        // Table name
        public static final String TABLE_NAME = PATH_SHOPPING_LIST_PRODUCT;

        // Product id
        public static final String COLUMN_PRODUCT_ID = "product_id";

        // Shopping list id
        public static final String COLUMN_SHOPPING_LIST_ID = "shopping_list_id";

        // Shop id
        public static final String COLUMN_SHOP_ID = "shop_id";

        // Sort order
        public static final String COLUMN_SORT_ORDER = "slp_sort_order";

        // Timestamp
        public static final String COLUMN_TIMESTAMP = "timestamp";

        public static Uri buildShoppingListProductsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getShoppingListProductIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}
