package com.dandersen.app.easyshoppinglist;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.dandersen.app.easyshoppinglist.data.ShoppingContract;
import com.dandersen.app.easyshoppinglist.data.ShoppingProvider;

public class MainActivity extends AppCompatActivity
        implements ButtonFragment.Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // If we're being restored from a previous state, then we don't need to do anything
        // and should return or else we could end up with overlapping fragments.
        if (savedInstanceState == null) {
            // Create a new Fragment to be placed in the activity layout
            CategoryFragment firstFragment = new CategoryFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            firstFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, firstFragment).commit();
        }
    }

    @Override
    public void onShopBtn(View view) {

    }

    @Override
    public void onProductBtn(View view) {
        // Create a new Fragment to be placed in the activity layout
        ProductFragment fragment = new ProductFragment();

        // In case this activity was started with special instructions from an
        // Intent, pass the Intent's extras to the fragment as arguments
        fragment.setArguments(getIntent().getExtras());

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment).commit();
    }

    @Override
    public void onCategoryBtn(View view) {
        // Create a new Fragment to be placed in the activity layout
        CategoryFragment fragment = new CategoryFragment();

        // In case this activity was started with special instructions from an
        // Intent, pass the Intent's extras to the fragment as arguments
        fragment.setArguments(getIntent().getExtras());

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment).commit();
    }

    @Override
    public void onShoppingListBtn(View view) {

    }

    @Override
    public void onCurrentListBtn(View view) {

    }
}
