package com.dandersen.app.easyshoppinglist.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.dandersen.app.easyshoppinglist.R;

/**
 * Created by Dan on 12-06-2016.
 * Utilities for using other apps with implicit intents.
 * - Maps from geo location
 * - Browser showing url
 * - Phone dialer with phone number
 */
public class ImplicitIntentUtil {

    public static void openLocationInMap(Context context,
                                         String location,
                                         String label) {
        // Using the URI scheme for showing a location found on a map.  This super-handy
        // intent can is detailed in the "Common Intents" page of Android's developer site:
        // http://developer.android.com/guide/components/intents-common.html#Maps
        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", location + "(" + label + ")")
                .build();

        // Create implicit intent
        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
        mapIntent.setData(geoLocation);

        // Verify that the intent will resolve to an activity
        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mapIntent);
        }
        else {
            Toast.makeText(context, context.getString(R.string.error_msg_no_map), Toast.LENGTH_SHORT).show();
        }
    }

    public static void openUrlInBrowser(Context context, String url) {
        Uri uri = Uri.parse(url);

        // Create implicit intent
        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        browserIntent.setData(uri);

        // Verify that the intent will resolve to an activity
        if (browserIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(browserIntent);
        }
        else {
            Toast.makeText(context, context.getString(R.string.error_msg_no_browser), Toast.LENGTH_SHORT).show();
        }
    }

    public static void openPhoneNumberInDialer(Context context, String phoneNumber) {
        final String TEL_PREFIX = "tel:";

        Uri uri = Uri.parse(TEL_PREFIX + phoneNumber);

        // Create implicit intent
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(uri);

        // Verify that the intent will resolve to an activity
        if (dialIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(dialIntent);
        }
        else {
            Toast.makeText(context, context.getString(R.string.error_msg_no_phone), Toast.LENGTH_SHORT).show();
        }
    }

}
