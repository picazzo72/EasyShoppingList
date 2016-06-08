package com.dandersen.app.easyshoppinglist;

import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.dandersen.app.easyshoppinglist.data.SelectedViewEnum;
import com.dandersen.app.easyshoppinglist.prefs.Settings;

import com.dandersen.app.easyshoppinglist.prefs.SettingsActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity
        implements  GoogleApiClient.ConnectionCallbacks,
                    GoogleApiClient.OnConnectionFailedListener,
                    ButtonFragment.Callback,
                    CategoryFragment.Callback {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private static GoogleApiClient mGoogleApiClient;
    private FetchShopTask mFetchShopTask = null;

    private boolean mTwoPaneLayout = false;

    private static Uri mCategoryProductsUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize settings
        Settings.getInstance().initialize(this, PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.activity_main);

        mFetchShopTask = (FetchShopTask) getLastCustomNonConfigurationInstance();

        if (mFetchShopTask == null) {
            // Create an instance of GoogleApiClient.
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
            }
        }
        else {
            mFetchShopTask.attach(this);
        }

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(0f);

//        // If we're being restored from a previous state, then we don't need to do anything
//        // and should return or else we could end up with overlapping fragments.
//        if (savedInstanceState == null) {
//            // Create a new Fragment to be placed in the activity layout
//            CategoryFragment firstFragment = new CategoryFragment();
//
//            // In case this activity was started with special instructions from an
//            // Intent, pass the Intent's extras to the fragment as arguments
//            firstFragment.setArguments(getIntent().getExtras());
//
//            // Add the fragment to the 'fragment_container' FrameLayout
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, firstFragment).commit();
//        }
    }

    /**
     * Used to save the AsyncTask when this Activity is begin destroyed to give
     * to the new MainActivity. This handles eg. screen rotation.
     * @return: Any Object holding the desired state to propagate to the next activity instance. In this case the FetchShopTask.
     */
    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        if (mFetchShopTask != null) {
            mFetchShopTask.detach();

            return mFetchShopTask;
        }
        return super.onRetainCustomNonConfigurationInstance();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mFetchShopTask != null) {
            mFetchShopTask.detach();
        }
    }

    @Override
    public void onBackPressed() {
        if (Settings.getInstance().getSelectedView() == SelectedViewEnum.Category) {
            ProductFragment fragment = (ProductFragment) getSupportFragmentManager().findFragmentByTag(ProductFragment.PRODUCT_FRAGMENT_TAG);
            if (fragment != null) {
                if (fragment.mChildFragment) {
                    onCategory();
                    return;
                }
            }
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Start settings activity with explicit intent
            Intent openSettingsActivityIntent = new Intent(this, SettingsActivity.class);
            startActivity(openSettingsActivityIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onShop() {
        Settings.getInstance().setSelectedView(SelectedViewEnum.Shop);

        // Create a new Fragment to be placed in the activity layout
        ShopFragment fragment = new ShopFragment();

        // In case this activity was started with special instructions from an
        // Intent, pass the Intent's extras to the fragment as arguments
        fragment.setArguments(getIntent().getExtras());

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment).commit();
    }

    @Override
    public void onProduct() {
        Settings.getInstance().setSelectedView(SelectedViewEnum.Product);

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
    public void onCategory() {
        Settings.getInstance().setSelectedView(SelectedViewEnum.Category);

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
    public void onShoppingList() {
        Settings.getInstance().setSelectedView(SelectedViewEnum.ShoppingList);
    }

    @Override
    public void onCurrentList() {
        Settings.getInstance().setSelectedView(SelectedViewEnum.CurrentList);
    }

    @Override
    public void onCategoryItemSelected(Uri uri) {
        mCategoryProductsUri = uri;

        Settings.getInstance().setSelectedView(SelectedViewEnum.CategoryProduct);

        onCategoryProduct();
    }

    @Override
    public void onCategoryProduct() {
        if (mCategoryProductsUri == null) throw new AssertionError("Category products URI cannot be null");

        Bundle arguments = new Bundle();
        arguments.putParcelable(ProductFragment.PRODUCT_FRAGMENT_URI, mCategoryProductsUri);

        ProductFragment productFragment = new ProductFragment();
        productFragment.setArguments(arguments);

        if (mTwoPaneLayout) {
            // In two-pane mode show the detail view in this activity by adding
            // or replacing the detail fragment using a fragment transaction

            // TODO
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.weather_detail_container, detailFragment, DETAILFRAGMENT_TAG)
//                    .commit();
        }
        else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, productFragment, ProductFragment.PRODUCT_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v(LOG_TAG, "DSA LOG - Connected to Google Location Api");

        try {
            android.location.Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (location != null) {
                String latitude = Double.toString(location.getLatitude());
                String longtitude = Double.toString(location.getLongitude());

                Log.v(LOG_TAG, "DSA LOG - Latitude: " + latitude + " - Longtitude: " + longtitude);

                mFetchShopTask = new FetchShopTask(this, getContentResolver());

                // Execute fetch weather task
                mFetchShopTask.execute(latitude + "," + longtitude);
            }
        }
        catch (SecurityException ex) {
            Toast.makeText(this, ex.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "DSA LOG - GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "DSA LOG - GoogleApiClient connection has failed");
    }

}
