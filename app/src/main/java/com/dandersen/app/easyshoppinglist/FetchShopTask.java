package com.dandersen.app.easyshoppinglist;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.dandersen.app.easyshoppinglist.data.ShoppingContract;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by dandersen on 05-06-2016.
 */
public class FetchShopTask extends AsyncTask<String, Void, ContentValues[]> {

    private final String LOG_TAG = FetchShopTask.class.getSimpleName();

    private final Context mContext;

    public FetchShopTask(Context context) {
        mContext = context;
    }

    @Override
    protected ContentValues[] doInBackground(String... params) {
        // If there's no zip code, there's nothing to look up.  Verify size of params.
        if (params.length == 0) {
            Log.v(LOG_TAG, "DSA LOG - doInBackground - no params");
            return null;
        }
        String location = params[0];

        // Find nearby place ids
        String nextPageToken = null;
        ArrayList nearbyPlaces = new ArrayList();

        do {
            // Request nearby places from Google API
            String placesJsonStr = getGoogleApiResponse(buildNearbySearchUri(location, nextPageToken));

            // Parse nearby places for the place ids
            nextPageToken = parseGoogleNearbyPlacesResult(placesJsonStr, nearbyPlaces);
        } while (nextPageToken != null);

        if (nearbyPlaces.isEmpty()) return null;

        // Fetch place ids from database
        HashSet<String> placeIdsInDb = new HashSet<>();
        Cursor c = mContext.getContentResolver().query(
                ShoppingContract.ShopEntry.CONTENT_URI,
                new String[] { ShoppingContract.ShopEntry.COLUMN_PLACE_ID },
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        if (c.moveToFirst()) {
            do {
                String placeId = c.getString(0);
                assert !placeId.isEmpty();
                placeIdsInDb.add(placeId);
            } while (c.moveToNext());
        }

        ArrayList<ContentValues> contentValuesArray = new ArrayList<>();
        for (int i = 0, end = nearbyPlaces.size(); i < end; ++i) {
            String placeId = (String)nearbyPlaces.get(i);
            if (!placeIdsInDb.contains(placeId)) {
                // Request place details from Google API
                String placeDetailsJsonStr = getGoogleApiResponse(buildPlaceDetailsUri(placeId));

                // Parse place details
                ContentValues contentValues = parseGooglePlaceDetailsResult(placeDetailsJsonStr);
                if (contentValues != null) {
                    contentValuesArray.add(contentValues);
                }
            }
        }

        if (!contentValuesArray.isEmpty()) {
            ContentValues[] contentValuesList = new ContentValues[contentValuesArray.size()];
            for (int i = 0, end = contentValuesArray.size(); i < end; ++i) {
                contentValuesList[i] = contentValuesArray.get(i);
            }
            return contentValuesList;
        }

        return null;
    }

    @Override
    protected void onPostExecute(ContentValues[] contentValuesList) {
        if (contentValuesList == null) return;

        final ContentValues[] fContentValuesList = contentValuesList;

        new AlertDialog.Builder(mContext)
                .setCancelable(true)
                .setTitle(R.string.dialog_import_shops_title)
                .setMessage(mContext.getString(R.string.dialog_import_shops_question, Integer.toString(contentValuesList.length)))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int insertCount = mContext.getContentResolver().bulkInsert(ShoppingContract.ShopEntry.CONTENT_URI, fContentValuesList);
                        Log.i(LOG_TAG, insertCount + " shop entries added to database");
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private Uri buildNearbySearchUri(String location, @Nullable String nextPageToken) {
        // Construct the URL for the Google Places API query
        final String PLACES_BASE_URL        = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
        final String LOCATION_PARAM         = "location";
        final String TYPE_PARAM             = "type";
        final String RADIUS_PARAM           = "radius";
        final String APP_ID                 = "key";
        final String RADIUS_VALUE           = "15000";
        final String TYPE_VALUE             = "grocery_or_supermarket";
        final String PAGE_TOKEN             = "page_token";

        if (nextPageToken == null) {
            return Uri.parse(PLACES_BASE_URL).buildUpon()
                    .appendQueryParameter(LOCATION_PARAM, location)
                    .appendQueryParameter(RADIUS_PARAM, RADIUS_VALUE)
                    .appendQueryParameter(TYPE_PARAM, TYPE_VALUE)
                    .appendQueryParameter(APP_ID, BuildConfig.GOOGLE_PLACES_API_KEY).build();
        }
        else {
            return Uri.parse(PLACES_BASE_URL).buildUpon()
                    .appendQueryParameter(PAGE_TOKEN, nextPageToken)
                    .appendQueryParameter(APP_ID, BuildConfig.GOOGLE_PLACES_API_KEY).build();
        }
    }

    private Uri buildPlaceDetailsUri(String placeId) {
        // Construct the URL for the Google Places API query
        final String PLACES_BASE_URL        = "https://maps.googleapis.com/maps/api/place/details/json?";
        final String PLACE_ID_PARAM         = "placeid";
        final String APP_ID                 = "key";

        return Uri.parse(PLACES_BASE_URL).buildUpon()
                .appendQueryParameter(PLACE_ID_PARAM, placeId)
                .appendQueryParameter(APP_ID, BuildConfig.GOOGLE_PLACES_API_KEY)
                .build();
    }

    private String getGoogleApiResponse(Uri builtUri) {
        // These need to be declared outside the try/catch so they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String placesJsonStr = null;

        try {

            URL url = new URL(builtUri.toString());

            // Log URL
            Log.v(LOG_TAG, "DSA LOG - Built URL for Google Places API " + url);

            // Create the request to Google Places, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                Log.v(LOG_TAG, "DSA LOG - doInBackground - no inputStream");
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                Log.v(LOG_TAG, "DSA LOG - doInBackground - empty stream");
                return null;
            }
            placesJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "DSA LOG - doInBackground - IOException " + e.toString(), e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return placesJsonStr;
    }

    private String parseGoogleNearbyPlacesResult(final String response, ArrayList nearbyPlaces) {
        final String RESULTS            = "results";
        final String PLACE_ID           = "place_id";
        final String NEXT_PAGE_TOKEN    = "next_page_token";

        String nextPageToken = null;
        try {
            // Make a jsonObject in order to parse the response
            JSONObject jsonObject = new JSONObject(response);

            // Try and find next_page_token
            String tmp = jsonObject.optString(NEXT_PAGE_TOKEN);
            if (!tmp.isEmpty()) {
                nextPageToken = tmp;
            }

            // Locate place ids
            if (jsonObject.has(RESULTS)) {
                JSONArray jsonArray = jsonObject.getJSONArray(RESULTS);

                for (int i = 0, end = jsonArray.length(); i < end; ++i) {
                    if (jsonArray.getJSONObject(i).has(PLACE_ID)) {
                        String placeId = jsonArray.getJSONObject(i).optString(PLACE_ID);
                        if (!placeId.isEmpty()) {
                            nearbyPlaces.add(placeId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "parseGoogleNearbyPlacesResult exception", e);
            e.printStackTrace();
        }

        return nextPageToken;
    }

    ContentValues parseGooglePlaceDetailsResult(final String response) {
        final String RESULT                 = "result";
        final String FORMATTED_ADDRESS      = "formatted_address";
        final String FORMATTED_PHONE_NUMBER = "formatted_phone_number";
        final String NAME                   = "name";
        final String PLACE_ID               = "place_id";
        final String WEBSITE                = "website";

        ContentValues contentValues = null;
        try {
            // make a jsonObject in order to parse the response
            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject.has(RESULT)) {
                contentValues = new ContentValues();

                JSONObject jsonResult = jsonObject.getJSONObject(RESULT);

                parseAddressComponents(jsonResult, contentValues);

                contentValues.put(ShoppingContract.ShopEntry.COLUMN_FORMATTED_ADDRESS, jsonResult.optString(FORMATTED_ADDRESS));
                contentValues.put(ShoppingContract.ShopEntry.COLUMN_PHONE_NUMBER, jsonResult.optString(FORMATTED_PHONE_NUMBER));

                parseGeometry(jsonResult, contentValues);

                contentValues.put(ShoppingContract.ShopEntry.COLUMN_NAME, jsonResult.optString(NAME));
                contentValues.put(ShoppingContract.ShopEntry.COLUMN_PLACE_ID, jsonResult.optString(PLACE_ID));
                contentValues.put(ShoppingContract.ShopEntry.COLUMN_WEBSITE, jsonResult.optString(WEBSITE));
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "parseGooglePlaceDetailsResult exception", e);
            e.printStackTrace();
        }
        return contentValues;

    }

    void parseAddressComponents(JSONObject jsonResult, ContentValues contentValues) {
        final String ADDRESS_COMPONENTS     = "address_components";
        final String LONG_NAME              = "long_name";
        final String TYPES                  = "types";
        final String STREET_NUMBER          = "street_number";
        final String ROUTE                  = "route";
        final String SUB_LOCALITY_1         = "sublocality_level_1";
        final String SUB_LOCALITY           = "sublocality";
        final String LOCALITY               = "locality";
        final String COUNTRY                = "country";
        final String POSTAL_CODE            = "postal_code";

        try {
            if (jsonResult.has(ADDRESS_COMPONENTS)) {
                JSONArray jsonAddressArray = jsonResult.getJSONArray(ADDRESS_COMPONENTS);
                for (int i = 0, end = jsonAddressArray.length(); i < end; ++i) {
                    String longName = jsonAddressArray.getJSONObject(i).optString(LONG_NAME);
                    if (!longName.isEmpty()) {
                        if (jsonAddressArray.getJSONObject(i).has(TYPES)) {
                            JSONArray addressTypesArray = jsonAddressArray.getJSONObject(i).getJSONArray(TYPES);
                            if (addressTypesArray.length() > 0) {
                                String firstAddressType = addressTypesArray.getString(0);
                                if (firstAddressType.equals(STREET_NUMBER)) {
                                    contentValues.put(ShoppingContract.ShopEntry.COLUMN_STREET_NUMBER, longName);
                                } else if (firstAddressType.equals(ROUTE)) {
                                    contentValues.put(ShoppingContract.ShopEntry.COLUMN_STREET, longName);
                                } else if (firstAddressType.equals(SUB_LOCALITY_1) || firstAddressType.equals(SUB_LOCALITY)) {
                                    contentValues.put(ShoppingContract.ShopEntry.COLUMN_CITY, longName);
                                } else if (firstAddressType.equals(LOCALITY)) {
                                    if (contentValues.getAsString(ShoppingContract.ShopEntry.COLUMN_CITY) == null) {
                                        contentValues.put(ShoppingContract.ShopEntry.COLUMN_CITY, longName);
                                    }
                                } else if (firstAddressType.equals(COUNTRY)) {
                                    contentValues.put(ShoppingContract.ShopEntry.COLUMN_COUNTRY, longName);
                                } else if (firstAddressType.equals(POSTAL_CODE)) {
                                    contentValues.put(ShoppingContract.ShopEntry.COLUMN_POSTAL_CODE, longName);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "parseAddressComponents exception", e);
            e.printStackTrace();
        }
    }

    void parseGeometry(JSONObject jsonResult, ContentValues contentValues) {
        final String GEOMETRY = "geometry";
        final String LOCATION = "location";
        final String LATITUDE = "lat";
        final String LONGTITUDE = "lng";

        try {
            if (jsonResult.has(GEOMETRY)) {
                JSONObject jsonGeometry = jsonResult.getJSONObject(GEOMETRY);
                if (jsonGeometry.has(LOCATION)) {
                    JSONObject jsonLocation = jsonGeometry.getJSONObject(LOCATION);
                    String latitude = jsonLocation.optString(LATITUDE);
                    String longtitude = jsonLocation.optString(LONGTITUDE);
                    if (!latitude.isEmpty() && !longtitude.isEmpty()) {
                        contentValues.put(ShoppingContract.ShopEntry.COLUMN_LOCATION, latitude + "," + longtitude);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "parseGeometry exception", e);
            e.printStackTrace();
        }
    }
}
