package com.dandersen.app.easyshoppinglist;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Activity for shop details.
 * Is used on small screens where the details cannot fit on the screen.
 */
public class DetailShopActivity extends AppCompatActivity {

    private final String LOG_TAG = DetailShopActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_detail);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0f);
        }

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity using a fragment transaction.
            // Send the Uri from the explicit intent as an argument for DetailShopFragment to pick up
            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailShopFragment.DETAIL_URI, getIntent().getData());

            DetailShopFragment detailShopFragment = new DetailShopFragment();
            detailShopFragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.shop_detail_container, detailShopFragment)
                    .commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

}
