package com.dandersen.app.easyshoppinglist.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.dandersen.app.easyshoppinglist.R;
import com.dandersen.app.easyshoppinglist.data.SelectedViewEnum;

/**
 * Created by Dan on 05-06-2016.
 * Settings manager.
 */
public class Settings {

    private SharedPreferences mSharedPreferences;
    private boolean mSettingsLoaded = false;

    // Tag for selected view
    private final String SELECTED_VIEW = "selected_view";

    // Which view were last selected by the user?
    private SelectedViewEnum mSelectedView = SelectedViewEnum.ActiveList;

    // Tag for nearby search automatic
    private final String NEARBY_SEARCH_AUTOMATIC = "nearby_search_automatic";

    // Have nearby search already run automatically?
    private boolean mNearbySearchAutomatic = false;

    // The search radius for Google Places API nearby search
    private int mNearbySearchRadius = 5000;

    // Key for nearby search radius
    private String mNearbySearchRadiusKey;

    public void initialize(Context context, SharedPreferences sharedPreferences) {
        // Load preference keys
        mNearbySearchRadiusKey = context.getString(R.string.pref_key_nearby_radius);

        mSharedPreferences = sharedPreferences;
        loadSettings();
    }

    private static class SettingsLoader {
        private static final Settings INSTANCE = new Settings();
    }

    private Settings() {
        if (SettingsLoader.INSTANCE != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }

    public static Settings getInstance() {
        return SettingsLoader.INSTANCE;
    }

    public void setSelectedView(SelectedViewEnum selectedView) {
        if (!mSettingsLoaded) throw new AssertionError("Settings have not been loaded");

        mSelectedView = selectedView;

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(SELECTED_VIEW, mSelectedView.ordinal());
        editor.apply();
    }

    public SelectedViewEnum getSelectedView() {
        if (!mSettingsLoaded) throw new AssertionError("Settings have not been loaded");

        return mSelectedView;
    }

    /**
     * Sets if the automatic nearby search for grocery stores have been done.
     * @param done true if the search have been done.
     */
    public void setNearbySearchAutomaticDone(boolean done) {
        mNearbySearchAutomatic = done;

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(NEARBY_SEARCH_AUTOMATIC, mNearbySearchAutomatic);
        editor.apply();
    }

    /**
     * Gets if the automatic nearby search for grocery stores have been done.
     * @return true if automatic nearby search have been done.
     */
    public boolean getNearbySearchAutomaticDone() {
        return mNearbySearchAutomatic;
    }

    public void setNearbySearchRadius(int nearbySearchRadius) {
        if (!mSettingsLoaded) throw new AssertionError("Settings have not been loaded");

        mNearbySearchRadius = nearbySearchRadius;

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(mNearbySearchRadiusKey, mNearbySearchRadius);
        editor.apply();
    }

    public int getNearbySearchRadius() {
        if (!mSettingsLoaded) throw new AssertionError("Settings have not been loaded");

        return mNearbySearchRadius;
    }

    void loadSettings() {
        synchronized (this) {
            if (mSettingsLoaded) return;

            mSelectedView = SelectedViewEnum.fromInteger(
                    mSharedPreferences.getInt(SELECTED_VIEW, SelectedViewEnum.ActiveList.ordinal()));

            mNearbySearchAutomatic = mSharedPreferences.getBoolean(NEARBY_SEARCH_AUTOMATIC, false);

            mNearbySearchRadius = mSharedPreferences.getInt(mNearbySearchRadiusKey, 5000);

            mSettingsLoaded = true;
        }
    }

}
