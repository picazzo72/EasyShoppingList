package com.dandersen.app.easyshoppinglist;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.dandersen.app.easyshoppinglist.data.ShoppingContract;
import com.dandersen.app.easyshoppinglist.prefs.Settings;
import com.dandersen.app.easyshoppinglist.ui.ActionModeListView;

import java.util.ArrayList;

/**
 * Created by Dan on 28-05-2016.
 * Shop fragment
 */
public class ShopFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
                   ActionModeListView.CallBack {

    private final String LOG_TAG = ShopFragment.class.getSimpleName();

    // Tags for bundle
    public static String SHOP_ID_TAG = "shop_id";

    // Tag for fragment
    public static String SHOP_FRAGMENT_TAG = "ShopFragment";

    private ShopAdapter mShopAdapter;

    private boolean mTwoPaneLayout = false;

    // Our shop list view
    private ListView mListView;

    // Keeping track of the selected item
    private int mPosition = ListView.INVALID_POSITION;

    // Shop id to select after load has finished
    private long mShopId = 0;

    // Tag for save instance bundle
    private static final String SELECTED_KEY = "selected_position";

    private static final int CURSOR_LOADER_ID = 0;

    // For the shop view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] SHOP_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            ShoppingContract.ShopEntry.TABLE_NAME + "." + ShoppingContract.ProductEntry._ID,
            ShoppingContract.ShopEntry.COLUMN_NAME,
            ShoppingContract.ShopEntry.COLUMN_STREET,
            ShoppingContract.ShopEntry.COLUMN_STREET_NUMBER,
            ShoppingContract.ShopEntry.COLUMN_CITY,
            ShoppingContract.ShopEntry.COLUMN_OPEN_NOW
    };

    // These indices are tied to SHOP_COLUMNS. If this changes, these indices must change.
    static final int COL_SHOP_ID                    = 0;
    static final int COL_SHOP_NAME                  = 1;
    static final int COL_SHOP_STREET                = 2;
    static final int COL_SHOP_STREET_NUMBER         = 3;
    static final int COL_SHOP_CITY                  = 4;
    static final int COL_OPEN_NOW                   = 5;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * ShopFragmentCallback for when an item has been selected.
         */
        void onShopItemSelected(Uri dateUri);
        void onShopFragmentActionMode(boolean enabled);
        void onShopListUpdate();
    }


    public ShopFragment() {
    }

    public void setTwoPaneLayout(boolean twoPaneLayout) {
        mTwoPaneLayout = twoPaneLayout;
        if (mShopAdapter != null) {
            mShopAdapter.setTwoPaneLayout(twoPaneLayout);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // The following line makes this fragment handle menu events
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(LOG_TAG, "DSA LOG - onCreateView");

        // The CursorAdapter will take data from a source and
        // use it to populate the ListView it's attached to
        mShopAdapter = new ShopAdapter(getActivity(), null, 0);
        mShopAdapter.setTwoPaneLayout(mTwoPaneLayout);

        View rootView = inflater.inflate(R.layout.fragment_shop, container, false);

        // Get a reference to the ListView, and attach this adapter to it
        mListView = (ListView) rootView.findViewById(R.id.listview_shops);
        mListView.setAdapter(mShopAdapter);

        // Setup item long click for item deletion
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionModeListView.setupItemLongClickClistener(this, activity, mListView, mShopAdapter);

        // Create on item click listener for the ListView
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                Log.i(LOG_TAG, "DSA LOG - onItemClick " + position);

                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor c = (Cursor) adapterView.getItemAtPosition(position);
                if (c != null) {
                    Callback cb = (Callback) getActivity();
                    cb.onShopItemSelected(ShoppingContract.ShopEntry.buildShopUri(
                            c.getLong(COL_SHOP_ID)
                    ));
                }
                mPosition = position;
            }
        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things. It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        // Prepare the loader.  Either re-connect with an existing one, or start a new one.
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this).forceLoad();

        return rootView;
    }

    @Override
    public void onActionModeFinished(final ArrayList<Integer> itemsToDelete) {
        final ShopFragment fShopFragment = this;

        new AlertDialog.Builder(getActivity())
                .setCancelable(true)
                .setTitle(R.string.dialog_delete_shops_title)
                .setMessage((itemsToDelete.size() == 1) ?
                        getActivity().getString(R.string.dialog_delete_shop_categories_question_one,
                                Integer.toString(itemsToDelete.size())) :
                        getActivity().getString(R.string.dialog_delete_shops_question,
                                Integer.toString(itemsToDelete.size()))
                )
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mShopAdapter.deleteEntries(itemsToDelete, COL_SHOP_ID);

                        // Re-initalize loader after data was removed
                        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, fShopFragment).forceLoad();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mShopAdapter.swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(LOG_TAG, "DSA LOG - onLoadFinished");

        //mListView.invalidateViews();

        // Select first item, last selected or new product if one of these are available
        if (mPosition != ListView.INVALID_POSITION || mShopId > 0) {
            class SelectListItem implements Runnable {
                int mListSelection;
                long mShopId;
                SelectListItem(int sel, long shopId) { mListSelection = sel; mShopId = shopId; }
                public void run() {
                    if (mShopId > 0) {
                        mListSelection = mShopAdapter.getItemPosition(mShopId);
                    }
                    if (mListSelection < mListView.getCount()) {
                        mPosition = mListSelection;
                        if (mTwoPaneLayout) {
                            mListView.performItemClick(
                                    mListView.getChildAt(mPosition),
                                    mPosition,
                                    mListView.getChildAt(mPosition).getId());
                        }
                        mListView.setSelection(mPosition);
                    }
                }
            }
            mListView.post(new SelectListItem(mPosition, mShopId));
        }

        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mShopAdapter.swapCursor(data);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arguments) {
        // Sort order: Ascending, by name
        String sortOrder = SHOP_COLUMNS[COL_SHOP_NAME] + " ASC";

        Uri uri = ShoppingContract.ShopEntry.CONTENT_URI;

        Log.i(LOG_TAG, "DSA LOG - onCreateLoader - URI for shop list: " + uri.toString());

        return new CursorLoader(getActivity(),
                uri,                   // URI
                SHOP_COLUMNS,          // projection
                null,                  // where
                null,                  // binds
                sortOrder);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_new, menu);
        inflater.inflate(R.menu.menu_refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_new) {
            createNewShop();
            return true;
        }
        else if (id == R.id.action_refresh) {
            ((ShopFragment.Callback)getActivity()).onShopListUpdate();
            return true;
        }
        else if (id == R.id.action_reload) {
            // First let settings know that we wish to reset automatic search
            Settings.getInstance().setNearbySearchAutomaticDone(false);

            ((ShopFragment.Callback)getActivity()).onShopListUpdate();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createNewShop() {
        // Create and start explicit intent
        Intent intent = new Intent(getActivity(), NewProductActivity.class);
        int requestCode = 1;
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            if (data != null) {
                mShopId = data.getLongExtra(ShopFragment.SHOP_ID_TAG, 0);
                if (mShopId > 0) {
                    getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this).forceLoad();
                }
            }
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (mPosition != ListView.INVALID_POSITION) {
            savedInstanceState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

}
