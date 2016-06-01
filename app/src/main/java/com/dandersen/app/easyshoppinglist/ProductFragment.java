package com.dandersen.app.easyshoppinglist;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.dandersen.app.easyshoppinglist.data.ShoppingContract;

/**
 * Created by Dan on 28-05-2016.
 */
public class ProductFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = ProductFragment.class.getSimpleName();

    // Tags for bundle
    public static String PRODUCT_ID_TAG = "product_id";

    private ProductAdapter mProductAdapter;

    private boolean mTwoPaneLayout = false;

    // Our product list view
    private ListView mListView;

    // Keeping track of the selected item
    private int mPosition = ListView.INVALID_POSITION;

    // Product id to select after load has finished
    private long mProductId = 0;

    // Tag for save instance bundle
    private static final String SELECTED_KEY = "selected_position";

    // Tag for saving the URI for the product fragment in single pane (phone) mode
    static final String PRODUCT_FRAGMENT_URI = "URI";

    private static final int CURSOR_LOADER_ID = 0;

    // The Uri from caller, or by default all products Uri
    private Uri mUri;

    private boolean mRestartingLoader = false;

    // For the category view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] PRODUCT_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            ShoppingContract.ProductEntry.TABLE_NAME + "." + ShoppingContract.ProductEntry._ID,
            ShoppingContract.ProductEntry.COLUMN_NAME,
            ShoppingContract.CategoryEntry.COLUMN_NAME
    };

    // These indices are tied to CATEGORY_COLUMNS.  If CATEGORY_COLUMNS changes, these
    // must change.
    static final int COL_PRODUCT_ID = 0;
    static final int COL_PRODUCT_NAME = 1;
    static final int COL_CATEGORY_NAME = 2;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * ProductFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }


    public ProductFragment() {
    }

    public void setTwoPaneLayout(boolean twoPaneLayout) {
        mTwoPaneLayout = twoPaneLayout;
        if (mProductAdapter != null) {
            mProductAdapter.setTwoPaneLayout(twoPaneLayout);
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
        // Get Uri from the arguments sent from the caller
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(ProductFragment.PRODUCT_FRAGMENT_URI);
        }
        if (mUri == null) {
            mUri = ShoppingContract.ProductEntry.CONTENT_WITH_CATEGORY_URI;
        }

        // The CursorAdapter will take data from a source and
        // use it to populate the ListView it's attached to
        mProductAdapter = new ProductAdapter(getActivity(), null, 0);
        mProductAdapter.setTwoPaneLayout(mTwoPaneLayout);

        View rootView = inflater.inflate(R.layout.fragment_product, container, false);

        // Get a reference to the ListView, and attach this adapter to it
        mListView = (ListView) rootView.findViewById(R.id.listview_products);
        mListView.setAdapter(mProductAdapter);

//        // Create on item click listener for the ListView
//        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
//                // CursorAdapter returns a cursor at the correct position for getItem(), or null
//                // if it cannot seek to that position.
//                Cursor c = (Cursor) adapterView.getItemAtPosition(position);
//                if (c != null) {
//                    String locationSetting = Utility.getPreferredLocation(getActivity());
//                    Callback cb = (Callback) getActivity();
//                    cb.onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
//                            locationSetting, c.getLong(COL_WEATHER_DATE)
//                    ));
//                }
//                mPosition = position;
//            }
//        });

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
        getLoaderManager().initLoader(CURSOR_LOADER_ID, arguments, this).forceLoad();

        return rootView;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mProductAdapter.swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "DSA LOG - onLoadFinished");

        // Select first item, last selected or new product if one of these are available
        if (mPosition != ListView.INVALID_POSITION || mProductId > 0) {
            class SelectListItem implements Runnable {
                int mListSelection;
                long mProductId;
                SelectListItem(int sel, long productId) { mListSelection = sel; mProductId = productId; }
                public void run() {
                    if (mProductId > 0) {
                        mListSelection = mProductAdapter.getItemPosition(mProductId);
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
            mListView.post(new SelectListItem(mPosition, mProductId));
        }

        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mProductAdapter.swapCursor(data);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arguments) {
        // Sort order: Acending, by name
        String sortOrder = PRODUCT_COLUMNS[COL_PRODUCT_NAME] + " ASC";

        Log.v(LOG_TAG, "DSA LOG - onCreateLoader - URI for product list: " + mUri.toString());

        return new CursorLoader(getActivity(),
                mUri,                  // URI
                PRODUCT_COLUMNS,       // projection
                null,                  // where
                null,                  // binds
                sortOrder);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_new, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_new) {
            createNewProduct();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createNewProduct() {
        // Create and start explicit intent
        Intent intent = new Intent(getActivity(), NewProductActivity.class);
        int requestCode = 1;
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            mProductId = data.getLongExtra(ProductFragment.PRODUCT_ID_TAG, 0);
            if (mProductId > 0) {
                getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this).forceLoad();
            }
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.toString(),
                    Toast.LENGTH_SHORT).show();
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
