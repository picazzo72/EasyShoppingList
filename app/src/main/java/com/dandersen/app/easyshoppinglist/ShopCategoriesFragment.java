package com.dandersen.app.easyshoppinglist;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dandersen.app.easyshoppinglist.data.ShoppingContract;
import com.dandersen.app.easyshoppinglist.ui.SimpleItemTouchHelperCallback;

/**
 * Created by Dan on 15-06-2016.
 * Fragment for shop categories activity. Used to setup categories for a shop.
 */
public class ShopCategoriesFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = ShopCategoriesFragment.class.getSimpleName();

    private ShopCategoriesAdapter mShopCategoryAdapter;

    // Our category list view
    private RecyclerView mRecyclerView;

    private static final int CURSOR_CATEGORY_LOADER_ID = 0;

    private boolean mTwoPaneLayout = false;

    // For the shop category view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] SHOP_CATEGORY_COLUMNS = {
            ShoppingContract.CategoryEntry.TABLE_NAME + "." + ShoppingContract.CategoryEntry._ID,
            ShoppingContract.ShopCategoryEntry.TABLE_NAME + "." + ShoppingContract.ShopCategoryEntry._ID,
            ShoppingContract.CategoryEntry.COLUMN_NAME,
            ShoppingContract.CategoryEntry.COLUMN_COLOR
    };

    // These indices are tied to SHOP_CATEGORY_COLUMNS
    static final int COL_CATEGORY_ID                = 0;
    static final int COL_SHOP_CATEGORY_ID           = 1;
    static final int COL_CATEGORY_NAME              = 2;
    static final int COL_COLOR                      = 3;

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
        View rootView = inflater.inflate(R.layout.fragment_shop_categories, container, false);

        // The CursorAdapter will take data from a source and
        // use it to populate the ListView it's attached to
        mShopCategoryAdapter = new ShopCategoriesAdapter(getActivity(), null, 0);
        mShopCategoryAdapter.setTwoPaneLayout(mTwoPaneLayout);

        // Get a reference to the ListView, and attach this adapter to it
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.listview_categories);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mShopCategoryAdapter);

        // Add a Touch Helper callback to the recycler view
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mShopCategoryAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);

        // Prepare the loader.  Either re-connect with an existing one, or start a new one.
        getLoaderManager().initLoader(CURSOR_CATEGORY_LOADER_ID, null, this);

        return rootView;
    }
}
