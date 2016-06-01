package com.dandersen.app.easyshoppinglist;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.dandersen.app.easyshoppinglist.data.ShoppingContract;

public class NewProductActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_product);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createProduct(view);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    void createProduct(View view) {
        // Get product name
        TextView textView = (TextView) findViewById(R.id.new_product_name);
        String productName = textView.getText().toString();
        if (productName.isEmpty()) {
            Snackbar.make(view, "You have to specify a product name", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        // Get category id
        Spinner spinner = (Spinner) findViewById(R.id.new_product_category);
        long categoryId = spinner.getSelectedItemId();
        if (categoryId <= 0) {
            Snackbar.make(view, "You have to select a category", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        String categoryName = ((TextView) spinner.getSelectedView().findViewById(android.R.id.text1)).getText().toString();

        // Check that product name does not already exist in this category
        String[] columns = new String[] { ShoppingContract.ProductEntry._ID };
        String where = ShoppingContract.ProductEntry.COLUMN_NAME + "= ? AND " +
                       ShoppingContract.ProductEntry.COLUMN_CATEGORY_ID + "= ?";
        String[] binds = new String[] { productName , Long.toString(categoryId) };
        Cursor c = getContentResolver().query(
                ShoppingContract.ProductEntry.CONTENT_URI,
                columns,
                where,
                binds,
                null  // sort order
        );
        if (c.moveToFirst()) {
            Snackbar.make(view, "The product '" + productName + "' already exists in category '" +
                    categoryName + "'", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(ShoppingContract.ProductEntry.COLUMN_NAME, productName);
        contentValues.put(ShoppingContract.ProductEntry.COLUMN_CATEGORY_ID, categoryId);
        contentValues.put(ShoppingContract.ProductEntry.COLUMN_CUSTOM, 1);

        // Insert product
        Uri insertedProductUri = getContentResolver().insert(ShoppingContract.ProductEntry.CONTENT_URI, contentValues);
        long newProductId = ContentUris.parseId(insertedProductUri);

        // Return result to caller
        Intent intent = new Intent();
        intent.putExtra(ProductFragment.PRODUCT_ID_TAG, newProductId);
        setResult(RESULT_OK, intent);

        finish();
    }
}
