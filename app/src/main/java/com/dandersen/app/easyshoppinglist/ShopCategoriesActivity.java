package com.dandersen.app.easyshoppinglist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by Dan on 15-06-2016.
 * Activity for setting up categories for a shop.
 */
public class ShopCategoriesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_categories);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create a new Fragment to be placed in the activity layout
        ShopCategoriesFragment fragment = new ShopCategoriesFragment();

        // In case this activity was started with special instructions from an
        // Intent, pass the Intent's extras to the fragment as arguments
        fragment.setArguments(getIntent().getExtras());

        // Add the fragment to the 'fragment_shop_categories' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_shop_categories, fragment)
                .commit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

}
