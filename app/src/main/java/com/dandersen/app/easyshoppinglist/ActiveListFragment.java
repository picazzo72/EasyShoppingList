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

import com.dandersen.app.easyshoppinglist.data.ActiveListUtil;
import com.dandersen.app.easyshoppinglist.data.ShoppingContract;
import com.dandersen.app.easyshoppinglist.ui.SimpleItemTouchHelperCallback;

/**
 * Created by Dan on 20-06-2016.
 * Fragment for the active shopping list.
 */
public class ActiveListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = ActiveListFragment.class.getSimpleName();

    private ActiveListAdapter mActiveListAdapter;

    // Our category list view
    private RecyclerView mRecyclerView;

    private static final int CURSOR_LOADER_ID = 0;

    private boolean mTwoPaneLayout = false;

    // For the shop category view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] ACTIVE_LIST_COLUMNS = {
            ShoppingContract.ShoppingListProductEntry.TABLE_NAME + "." +
                    ShoppingContract.ShoppingListProductEntry._ID,
            ShoppingContract.ShoppingListProductEntry.TABLE_NAME + "." +
                    ShoppingContract.ShoppingListProductEntry.COLUMN_SHOP_ID,
            ShoppingContract.ShopEntry.COLUMN_NAME,
            ShoppingContract.CategoryEntry.TABLE_NAME + "." +
            ShoppingContract.CategoryEntry._ID,
            ShoppingContract.CategoryEntry.COLUMN_NAME,
            ShoppingContract.CategoryEntry.COLUMN_COLOR,
            ShoppingContract.ProductEntry.COLUMN_NAME,
            ShoppingContract.ShoppingListProductEntry.TABLE_NAME + "." +
            ShoppingContract.ShoppingListProductEntry.COLUMN_SHOPPING_LIST_ID
    };

    // These indices are tied to ACTIVE_LIST_COLUMNS
    static final int COL_SHOPPING_LIST_PRODUCT_ID   = 0;
    static final int COL_SHOP_ID                    = 1;
    static final int COL_SHOP_NAME                  = 2;
    static final int COL_CATEGORY_ID                = 3;
    static final int COL_CATEGORY_NAME              = 4;
    static final int COL_CATEGORY_COLOR             = 5;
    static final int COL_PRODUCT_NAME               = 6;
    static final int COL_SHOPPING_LIST_ID           = 7;

    public ActiveListFragment() {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = ShoppingContract.ProductEntry.CONTENT_ACTIVE_LIST_URI;

        Long activeListId = ActiveListUtil.getActiveShoppingListId(getActivity());
        if (activeListId == null) return null;

        String selection = ShoppingContract.ShoppingListProductEntry.TABLE_NAME + "." +
                ShoppingContract.ShoppingListProductEntry.COLUMN_SHOPPING_LIST_ID + " = ?";
        String[] selectionArgs = new String[] { Long.toString(activeListId) };

        String sortOrder = ShoppingContract.ShopEntry.COLUMN_NAME + " ASC, " +
                ShoppingContract.ShopCategoryEntry.COLUMN_SORTORDER + " ASC, " +
                ShoppingContract.ProductEntry.COLUMN_NAME + " ASC";

        return new CursorLoader(getActivity(),
                uri,                            // URI
                ACTIVE_LIST_COLUMNS,            // projection
                selection,                      // where
                selectionArgs,                  // binds
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(LOG_TAG, "DSA LOG - onLoadFinished");

        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mActiveListAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mActiveListAdapter.swapCursor(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_active_list, container, false);
        final ActiveListFragment thisPtr = this;

        // The CursorAdapter will take data from a source and
        // use it to populate the ListView it's attached to
        mActiveListAdapter = new ActiveListAdapter(getActivity(), null, 0);
        mActiveListAdapter.setTwoPaneLayout(mTwoPaneLayout);
        mActiveListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                getLoaderManager().initLoader(CURSOR_LOADER_ID, null, thisPtr).forceLoad();
            }
        });

        // Get a reference to the RecyclerView, and attach this adapter to it
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.listview_active_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mActiveListAdapter);

        // Add a Touch Helper callback to the RecyclerView
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mActiveListAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);

        // Prepare the loader.  Either re-connect with an existing one, or start a new one.
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        return rootView;
    }
}
