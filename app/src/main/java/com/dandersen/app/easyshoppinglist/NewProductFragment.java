package com.dandersen.app.easyshoppinglist;

import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;

import com.dandersen.app.easyshoppinglist.data.ShoppingContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class NewProductFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private SimpleCursorAdapter mSimpleCursorAdapter;

    private static final int CURSOR_CATEGORY_LOADER_ID = 0;
    private static final int CURSOR_PRODUCT_LOADER_ID  = 1;

    public NewProductFragment() {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case CURSOR_CATEGORY_LOADER_ID:
            {
                Uri categoryUri = ShoppingContract.CategoryEntry.CONTENT_URI;

                String[] projection = { ShoppingContract.CategoryEntry._ID ,
                        ShoppingContract.CategoryEntry.COLUMN_NAME };

                return new CursorLoader(getActivity(),
                        categoryUri,           // URI
                        projection,            // projection
                        null,                  // where
                        null,                  // binds
                        ShoppingContract.CategoryEntry.COLUMN_NAME);
            }
            case CURSOR_PRODUCT_LOADER_ID:
            {
                Uri productUri = ShoppingContract.ProductEntry.CONTENT_URI;

                String[] projection = { ShoppingContract.ProductEntry._ID ,
                        ShoppingContract.ProductEntry.COLUMN_NAME };

                return new CursorLoader(getActivity(),
                        productUri,            // URI
                        projection,            // projection
                        null,                  // where
                        null,                  // binds
                        ShoppingContract.ProductEntry.COLUMN_NAME);
            }
            default:
                throw new UnsupportedOperationException("Unknown loader id: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0)  {
            switch (loader.getId()) {
                case CURSOR_CATEGORY_LOADER_ID:
                {
                    Spinner spinner = (Spinner) getActivity().findViewById(R.id.new_product_category);

                    String[] adapterFromCols = new String[]{ ShoppingContract.CategoryEntry.COLUMN_NAME };
                    int[] adapterToRowViews = new int[]{ android.R.id.text1 };
                    mSimpleCursorAdapter = new SimpleCursorAdapter(getActivity(),
                            android.R.layout.simple_spinner_item, cursor, adapterFromCols, adapterToRowViews, 0);
                    spinner.setAdapter(mSimpleCursorAdapter);
                    break;
                }
                case CURSOR_PRODUCT_LOADER_ID:
                {
                    // Create adapter
                    String[] adapterFromCols = new String[]{ ShoppingContract.ProductEntry.COLUMN_NAME };
                    int[] adapterToRowViews = new int[]{ android.R.id.text1 };
                    SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
                            android.R.layout.simple_dropdown_item_1line, cursor, adapterFromCols, adapterToRowViews, 0);

                    // Set adapter on text view
                    AutoCompleteTextView productTextView = (AutoCompleteTextView) getActivity()
                            .findViewById(R.id.new_product_name);
                    productTextView.setAdapter(adapter);
                    break;
                }
                default:
                    throw new UnsupportedOperationException("Unknown loader id: " + loader.getId());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_new_product, container, false);

        // Prepare the loader.  Either re-connect with an existing one, or start a new one.
        getLoaderManager().initLoader(CURSOR_CATEGORY_LOADER_ID, null, this);
        getLoaderManager().initLoader(CURSOR_PRODUCT_LOADER_ID, null, this);

        return rootView;
    }
}
