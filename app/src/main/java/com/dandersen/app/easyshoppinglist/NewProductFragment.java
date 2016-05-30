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
import android.widget.Spinner;

import com.dandersen.app.easyshoppinglist.data.ShoppingContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class NewProductFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private SimpleCursorAdapter mSimpleCursorAdapter;

    private static final int CURSOR_CATEGORY_LOADER_ID = 0;

    public NewProductFragment() {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0)  {
            Spinner spinner = (Spinner) getActivity().findViewById(R.id.dialog_new_product_category);

            String[] adapterFromCols = new String[]{
                    ShoppingContract.CategoryEntry.COLUMN_NAME ,
                    ShoppingContract.CategoryEntry._ID };
            int[] adapterToRowViews = new int[]{ android.R.id.text1 , android.R.id.text2 };
            mSimpleCursorAdapter = new SimpleCursorAdapter(getActivity(),
                    R.layout.spinner_item_name_and_id, cursor, adapterFromCols, adapterToRowViews, 0);
            spinner.setAdapter(mSimpleCursorAdapter);
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

        return rootView;
    }
}
