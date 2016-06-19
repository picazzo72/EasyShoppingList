package com.dandersen.app.easyshoppinglist;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.dandersen.app.easyshoppinglist.data.ShoppingContract;

/**
 * Created by Dan on 15-06-2016.
 * Fragment for shop categories activity. Used to setup categories for a shop.
 */
public class ShopCategoriesFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = ShopCategoriesFragment.class.getSimpleName();

    private ShopCategoriesAdapter mShopCategoryAdapter;

    // Our category list view
    private ListView mListView;

    private static final int CURSOR_CATEGORY_LOADER_ID = 0;

    // For the shop category view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] SHOP_CATEGORY_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            ShoppingContract.CategoryEntry.TABLE_NAME + "." + ShoppingContract.CategoryEntry._ID,
            ShoppingContract.ShopCategoryEntry.TABLE_NAME + "." + ShoppingContract.ShopCategoryEntry._ID,
            ShoppingContract.CategoryEntry.COLUMN_NAME,
            ShoppingContract.ShopCategoryEntry.COLUMN_SORTORDER,
            ShoppingContract.CategoryEntry.COLUMN_COLOR
    };

    // These indices are tied to SHOP_CATEGORY_COLUMNS
    static final int COL_CATEGORY_ID                = 0;
    static final int COL_SHOP_CATEGORY_ID           = 1;
    static final int COL_CATEGORY_NAME              = 2;
    static final int COL_SORT_ORDER                 = 3;
    static final int COL_COLOR                      = 4;

    public ShopCategoriesFragment() {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Get shop id from caller
        Bundle arguments = getArguments();
        if (arguments == null) return null;
        String shopId = arguments.getString(DetailShopFragment.SHOP_ID_TAG);
        if (shopId == null) return null;

        Uri shopCategoryUri = ShoppingContract.ShopCategoryEntry.CONTENT_URI;

        String selection = ShoppingContract.ShopEntry.TABLE_NAME + "." +
                ShoppingContract.ShopEntry._ID + " = ?";
        String[] selectionArgs = new String[] { shopId };

        return new CursorLoader(getActivity(),
                shopCategoryUri,                // URI
                SHOP_CATEGORY_COLUMNS,          // projection
                selection,                      // where
                selectionArgs,                  // binds
                ShoppingContract.ShopCategoryEntry.COLUMN_SORTORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(LOG_TAG, "DSA LOG - onLoadFinished");

        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mShopCategoryAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mShopCategoryAdapter.swapCursor(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // The CursorAdapter will take data from a source and
        // use it to populate the ListView it's attached to
        mShopCategoryAdapter = new ShopCategoriesAdapter(getActivity(), null, 0);
        // TODO mShopCategoryAdapter.setTwoPaneLayout(mTwoPaneLayout);

        View rootView = inflater.inflate(R.layout.fragment_shop_categories, container, false);

        // Get a reference to the ListView, and attach this adapter to it
        mListView = (ListView)rootView.findViewById(R.id.listview_categories);
        mListView.setAdapter(mShopCategoryAdapter);

        // Prepare the loader.  Either re-connect with an existing one, or start a new one.
        getLoaderManager().initLoader(CURSOR_CATEGORY_LOADER_ID, null, this);

        return rootView;
    }

}
