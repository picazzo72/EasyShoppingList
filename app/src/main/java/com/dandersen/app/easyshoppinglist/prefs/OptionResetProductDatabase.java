package com.dandersen.app.easyshoppinglist.prefs;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import com.dandersen.app.easyshoppinglist.data.ShoppingDbHelper;
import com.dandersen.app.easyshoppinglist.data.Utilities;

/**
 * Created by dandersen on 29-05-2016.
 */
public class OptionResetProductDatabase extends DialogPreference
        implements DialogInterface.OnClickListener {

    public OptionResetProductDatabase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        persistBoolean(positiveResult);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            Utilities.repopulateDatabase(getContext());
        }
    }
}
