package com.dandersen.app.easyshoppinglist.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by dandersen on 29-05-2016.
 */
public class Utilities {

    private static String LOG_TAG = Utilities.class.getSimpleName();

    private static void deleteAllRecordsFromDatabase(Context context) {
        context.getContentResolver().delete(
                ShoppingContract.ProductEntry.CONTENT_URI,
                null,
                null
        );
        context.getContentResolver().delete(
                ShoppingContract.CategoryEntry.CONTENT_URI,
                null,
                null
        );
        context.getContentResolver().delete(
                ShoppingContract.ShoppingListEntry.CONTENT_URI,
                null,
                null
        );
        context.getContentResolver().delete(
                ShoppingContract.ShopEntry.CONTENT_URI,
                null,
                null
        );
        context.getContentResolver().delete(
                ShoppingContract.ShoppingListProductsEntry.CONTENT_URI,
                null,
                null
        );
    }

    private static Map mCategoryProducts = new HashMap();

    static void populateDatabase(SQLiteDatabase db) {
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

    public static void repopulateDatabase(Context context) {
        // Delete all records from database through content provider
        deleteAllRecordsFromDatabase(context);

        // Get reference to writable database
        SQLiteDatabase db = new ShoppingDbHelper(context).getWritableDatabase();
        assert db.isOpen() == true;

        populateDatabase(db);
    }
}
