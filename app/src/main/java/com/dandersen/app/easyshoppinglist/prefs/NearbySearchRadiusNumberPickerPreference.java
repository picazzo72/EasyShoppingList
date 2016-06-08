package com.dandersen.app.easyshoppinglist.prefs;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.dandersen.app.easyshoppinglist.R;

import java.util.Arrays;

/**
 * Created by dandersen on 08-06-2016.
 *
 * Specialized class for nearby search radius preference.
 * This preference is implemented as a number picker which is a dialog for selecting
 * the radius from a list of values defined in arrays.xml
 */
public class NearbySearchRadiusNumberPickerPreference extends NumberPickerDisplayedValuesPreference {

    NearbySearchRadiusNumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Get displayed values from AttributeSet defined in attrs.xml. We obtain a resource id to
        // a String array defined in arrays.xml
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.NearbySearchRadiusNumberPickerAttrs);
        int resId = attributes.getResourceId(R.styleable.NearbySearchRadiusNumberPickerAttrs_picker_values, 0);
        if (resId == 0) throw new AssertionError("Resource ID in NearbySearchRadiusNumberPickerPreference cannot be 0");
        mDisplayedValues = context.getResources().getStringArray(resId);

        // Since the displayed values were not loaded onSetInitialValue we initialize the selected value here
        if (mInitialValue == null) {
            mInitialValue = Arrays.asList(mDisplayedValues).indexOf(Integer.toString(Settings.getInstance().getNearbySearchRadius()));
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue,
                                     Object defaultValue) {
        int def = ( defaultValue instanceof Number ) ? (Integer)defaultValue
                : ( defaultValue != null ) ? Integer.parseInt(defaultValue.toString()) :
                Arrays.asList(mDisplayedValues).indexOf(Integer.toString(Settings.getInstance().getNearbySearchRadius()));
        if ( restorePersistedValue ) {
            mInitialValue = Arrays.asList(mDisplayedValues).indexOf(Integer.toString(getPersistedInt(def)));
        }
        else {
            mInitialValue = (Integer)defaultValue;
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            int port = mNumberPicker.getValue();
            if (port < mDisplayedValues.length) {
                Settings.getInstance().setNearbySearchRadius(Integer.valueOf(mDisplayedValues[port]));
            }
        }
    }
}
