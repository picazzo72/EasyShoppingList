package com.dandersen.app.easyshoppinglist.data;

import android.content.SharedPreferences;

/**
 * Created by Dan on 05-06-2016.
 */
public class Settings {

    private SharedPreferences mSharedPreferences;
    private boolean mSettingsLoaded = false;

    private SelectedViewEnum mSelectedView = SelectedViewEnum.CurrentList;

    // Tag for selected button
    private static final String SELECTED_VIEW = "selected_view";

    public void initialize(SharedPreferences sharedPreferences) {
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
        assert mSettingsLoaded;

        mSelectedView = selectedView;

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(SELECTED_VIEW, mSelectedView.ordinal());
        editor.commit();
    }

    public SelectedViewEnum getSelectedView() {
        assert mSettingsLoaded;
        return mSelectedView;
    }

    void loadSettings() {
        if (mSettingsLoaded) return;

        mSelectedView = SelectedViewEnum.fromInteger(Integer.valueOf(
                mSharedPreferences.getInt(SELECTED_VIEW, SelectedViewEnum.CurrentList.ordinal())));
    }

}
