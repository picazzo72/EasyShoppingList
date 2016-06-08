package com.dandersen.app.easyshoppinglist.prefs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

/**
 * Created by dandersen on 07-06-2016.
 */
public class NumberPickerDisplayedValuesPreference extends DialogPreference {

    protected NumberPicker mNumberPicker;
    protected String[] mDisplayedValues = null;
    protected Integer mInitialValue = 0;

    public NumberPickerDisplayedValuesPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateDialogView() {
        mNumberPicker = new NumberPicker(getContext());
        mNumberPicker.setMinValue(0);
        mNumberPicker.setMaxValue(mDisplayedValues.length - 1);
        mNumberPicker.setDisplayedValues(mDisplayedValues);
        mNumberPicker.setValue(mInitialValue);
        return mNumberPicker;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        if ( which == DialogInterface.BUTTON_POSITIVE ) {
            mInitialValue = mNumberPicker.getValue();
            persistInt(mInitialValue);
            callChangeListener(Integer.valueOf(mDisplayedValues[mInitialValue]));
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 1);
    }

}