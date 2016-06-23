package com.dandersen.app.easyshoppinglist;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
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

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements  GoogleApiClient.ConnectionCallbacks,
                    GoogleApiClient.OnConnectionFailedListener,
                    ButtonFragment.Callback,
                    CategoryFragment.Callback,
                    ShopFragment.Callback {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private static boolean sFetchShopTaskHasRun = false;
    private GoogleApiClient mGoogleApiClient = null;
    private static FetchShopTask sFetchShopTask = null;

    // Timer for FetchShopTask
    private static Timer sFetchShopTaskTimer = new Timer();
    private static TimerTask sFetchShopTimerTask = null;
    private final int FETCH_SHOP_TIMER_INTERVAL = 5 * 60 * 1000;  // Update shops every 5th minut

    private boolean mTwoPaneLayout = false;

    private static Uri mCategoryProductsUri;

    // Tag for DetailShopFragment in two pane layout mode
    private String DETAIL_SHOP_FRAGMENT_TAG = "DETAIL_SHOP_FRAGMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize settings
        Settings.getInstance().initialize(this, PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.activity_main);

        // Reset FetchShopTask if it was saved from last MainActivity
        sFetchShopTask = (FetchShopTask) getLastCustomNonConfigurationInstance();
        if (sFetchShopTask != null) {
            sFetchShopTask.attach(this);
        }

        // Initialize timer
        initializeFetchShopTimer();

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0f);
        }
    }

    private void initializeFetchShopTimer() {
        if (sFetchShopTimerTask != null) return;

        sFetchShopTimerTask = new TimerTask() {
            @Override
            public void run() {
                // Reconnect the Google API client.
                getGoogleApiClient().reconnect();
            }
        };

        sFetchShopTaskTimer.schedule(sFetchShopTimerTask, FETCH_SHOP_TIMER_INTERVAL, FETCH_SHOP_TIMER_INTERVAL);
    }

    private void deleteFetchShopTimer() {
        if (sFetchShopTimerTask != null) {
            sFetchShopTimerTask.cancel();
            sFetchShopTimerTask = null;
        }
    }

    /**
     * Used to save the AsyncTask when this Activity is begin destroyed to give
     * to the new MainActivity. This handles eg. screen rotation.
     * The method is described on the following web page:
     * http://stackoverflow.com/questions/3821423/background-task-progress-dialog-orientation-change-is-there-any-100-working/3821998#3821998
     * @return Any Object holding the desired state to propagate to the next activity instance. In this case the FetchShopTask.
     */
    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        if (sFetchShopTask != null) {
            sFetchShopTask.detach();

            return sFetchShopTask;
        }
        return super.onRetainCustomNonConfigurationInstance();
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
    public void onActiveList() {
        Settings.getInstance().setSelectedView(SelectedViewEnum.ActiveList);

        // Create a new Fragment to be placed in the activity layout
        ActiveListFragment fragment = new ActiveListFragment();

        // In case this activity was started with special instructions from an
        // Intent, pass the Intent's extras to the fragment as arguments
        fragment.setArguments(getIntent().getExtras());

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment).commit();
    }

    @Override
    public void onCategoryItemSelected(Uri uri) {
        mCategoryProductsUri = uri;

        Settings.getInstance().setSelectedView(SelectedViewEnum.CategoryProduct);

        onCategoryProduct();
    }

    @Override
    public void onCategoryProduct() {
        // This can happen if this was the last selected view and the app is restarted from scratch
        if (mCategoryProductsUri == null) {
            onCategory();
            return;
        }


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

    /**
     * Callback from ShopFragment which is called when action mode is started and stopped.
     * We hide the button fragment in action mode so that the user cannot click the buttons
     * which messes up the action mode.
     * @param enabled Enable or disable action mode.
     */
    @Override
    public void onShopFragmentActionMode(boolean enabled) {
        showOrHideButtonFragment(enabled);
    }

    private void showOrHideButtonFragment(boolean show) {
        ButtonFragment buttonFragment = (ButtonFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_buttons);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (show) {
            transaction.hide(buttonFragment).commit();
        }
        else {
            transaction.show(buttonFragment).commit();
        }
    }

    @Override
    public void onShopItemSelected(Uri uri) {
        if (mTwoPaneLayout) {
            // In two-pane mode show the detail view in this activity by adding
            // or replacing the detail fragment using a fragment transaction
            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailShopFragment.DETAIL_URI, uri);

            DetailShopFragment detailFragment = new DetailShopFragment();
            detailFragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.shop_detail_container, detailFragment, DETAIL_SHOP_FRAGMENT_TAG)
                    .commit();
        }
        else {
            // Create and start explicit intent
            Intent intent = new Intent(this, DetailShopActivity.class).setData(uri);
            startActivity(intent);
        }
    }

    @Override
    public void onShopListUpdate() {
        getGoogleApiClient().reconnect();
    }

    /**
     * onStart method for Activity.
     * Used to connect to Google API Client
     */
    @Override
    protected void onStart() {
        super.onStart();

        Log.i(LOG_TAG, "DSA LOG - onStart");

        if (!sFetchShopTaskHasRun) {
            sFetchShopTaskHasRun = true;

            // Connect the client.
            getGoogleApiClient().connect();
        }
    }

    /**
     * onStop method for Activity.
     * Used to disconnect from Google API Client
     */
    @Override
    protected void onStop() {
        Log.i(LOG_TAG, "DSA LOG - onStop");

        // Disconnecting the client invalidates it.
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
            Log.i(LOG_TAG, "DSA LOG - onStop - GoogleApiClient disconnected");
        }

//        if (sFetchShopTask != null) {
//            if (sFetchShopTask.getStatus() == AsyncTask.Status.RUNNING) {
//                Log.i(LOG_TAG, "DSA LOG - onStop - FetchShopTask cancelled");
//                sFetchShopTask.cancel(true /*mayInterruptIfRunning*/);
//            }
//        }

        // Stop the timer
        deleteFetchShopTimer();

        super.onStop();
    }

    private GoogleApiClient getGoogleApiClient() {
        if (mGoogleApiClient == null) {
            // Create an instance of GoogleApiClient.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            Log.i(LOG_TAG, "DSA LOG - New Google Api Client created");
        }
        return mGoogleApiClient;
    }
    /**
     * Callback from Google API Client when we are connected to Google Services
     * @param bundle Bundle from API
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.i(LOG_TAG, "DSA LOG - Connected to Google Location Api");

        try {
            // Check that the task is not already running
            if (sFetchShopTask != null) {
                if (sFetchShopTask.getStatus() == AsyncTask.Status.RUNNING) {
                    Log.i(LOG_TAG, "DSA LOG - onConnected - FetchShopTask already running");
                    return;
                }
            }

            android.location.Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (location != null) {
                String latitude = Double.toString(location.getLatitude());
                String longtitude = Double.toString(location.getLongitude());

                Log.i(LOG_TAG, "DSA LOG - Latitude: " + latitude + " - Longtitude: " + longtitude);

                if (sFetchShopTask == null || sFetchShopTask.getStatus() == AsyncTask.Status.FINISHED) {
                    sFetchShopTask = new FetchShopTask(this);
                    Log.i(LOG_TAG, "DSA LOG - onConnected - new FetchShopTask created");
                }

                if (Settings.getInstance().getNearbySearchAutomaticDone()) {
                    Toast.makeText(this, getString(R.string.toast_updating_stores), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this, getString(R.string.toast_fetching_stores), Toast.LENGTH_SHORT).show();
                }

                // Execute fetch weather task
                sFetchShopTask.execute(latitude + "," + longtitude);
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "DSA LOG - GoogleApiClient connection has failed " + connectionResult);
    }

}
