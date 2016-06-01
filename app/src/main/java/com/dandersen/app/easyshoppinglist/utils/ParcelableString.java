package com.dandersen.app.easyshoppinglist.utils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Dan on 01-06-2016.
 */
public class ParcelableString implements Parcelable {

    private String mString;

    public ParcelableString(String str) {
        mString = str;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mString);
    }

    public String getString() {
        return mString;
    }

    private static ParcelableString readFromParcel(Parcel in) { // (4)
        String str = in.readString();
        return new ParcelableString(str);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() // (5)
    {
        public ParcelableString createFromParcel(Parcel in) // (6)
        {
            return readFromParcel(in);
        }

        public ParcelableString[] newArray(int size) { // (7)
            return new ParcelableString[size];
        }
    };
}
