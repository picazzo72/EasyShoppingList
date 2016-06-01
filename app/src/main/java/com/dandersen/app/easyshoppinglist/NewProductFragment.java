package com.dandersen.app.easyshoppinglist;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.Spinner;
import android.widget.TextView;

import com.dandersen.app.easyshoppinglist.data.ShoppingContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class NewProductFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
                   AdapterView.OnItemSelectedListener {

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
            Spinner spinner = (Spinner) getActivity().findViewById(R.id.new_product_category);

            String[] adapterFromCols = new String[]{ ShoppingContract.CategoryEntry.COLUMN_NAME };
            int[] adapterToRowViews = new int[]{ android.R.id.text1 };
            mSimpleCursorAdapter = new SimpleCursorAdapter(getActivity(),
                    R.layout.spinner_item, cursor, adapterFromCols, adapterToRowViews, 0);
            spinner.setAdapter(mSimpleCursorAdapter);

            spinner.setOnItemSelectedListener(this);
            spinner.requestFocus();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_new_product, container, false);

        EditText newProductText = (EditText) rootView.findViewById(R.id.new_product_name);
        newProductText.setEnabled(false);

        // Prepare the loader.  Either re-connect with an existing one, or start a new one.
        getLoaderManager().initLoader(CURSOR_CATEGORY_LOADER_ID, null, this);

        return rootView;
    }

    private void setupProductAutoCompletion() {
        // Create adapter
        final String[] adapterFromCols = new String[]{
                ShoppingContract.ProductEntry.COLUMN_NAME ,
                ShoppingContract.ProductEntry.TABLE_NAME + "." + ShoppingContract.ProductEntry._ID };
        int[] adapterToRowViews = new int[]{ android.R.id.text1 };
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
                R.layout.spinner_item,
                null,
                adapterFromCols,
                adapterToRowViews,
                0);

        // This will provide the labels for the choices to be displayed in the AutoCompleteTextView
        adapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            @Override
            public CharSequence convertToString(Cursor cursor) {
                final int colIndex = cursor.getColumnIndexOrThrow(ShoppingContract.ProductEntry.COLUMN_NAME);
                return cursor.getString(colIndex);
            }
        });


        // This will run a query to find the descriptions for a given vehicle.
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence productNameFragment) {
                if (productNameFragment != null) {
                    String categoryName = getCategoryName();
                    if (!categoryName.isEmpty()) {
                        StringBuffer selection = new StringBuffer(ShoppingContract.ProductEntry.COLUMN_NAME)
                                .append(" LIKE '")
                                .append(productNameFragment)
                                .append("%'");
                        String sortOrder = ShoppingContract.ProductEntry.COLUMN_NAME;

                        Cursor managedCursor = getActivity().getContentResolver().query(
                                ShoppingContract.ProductEntry.buildProductCategory(categoryName),
                                adapterFromCols,
                                selection.toString(),
                                null,
                                sortOrder);
                        return managedCursor;
                    }
                }
                return null;
            }
        });

        // Set adapter on text view
        AutoCompleteTextView productTextView = (AutoCompleteTextView) getActivity()
                .findViewById(R.id.new_product_name);
        productTextView.setAdapter(adapter);
        productTextView.clearFocus();
        productTextView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER /*66*/) {
                    ((NewProductActivity) getActivity()).createProduct(getView());
                    return true;
                }
                return false;
            }
        });
    }

    String getCategoryName() {
        String categoryName = "";
        Spinner spinner = (Spinner) getActivity().findViewById(R.id.new_product_category);
        if (spinner != null) {
            TextView textView = (TextView) spinner.getSelectedView().findViewById(android.R.id.text1);
            if (textView != null) {
                categoryName = textView.getText().toString();
            }
        }
        return categoryName;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        EditText newProductText = (EditText) getActivity().findViewById(R.id.new_product_name);
        newProductText.setEnabled(true);
        setupProductAutoCompletion();

        // Show keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(newProductText, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        EditText newProductText = (EditText) getActivity().findViewById(R.id.new_product_name);
        newProductText.setEnabled(false);
    }

}
