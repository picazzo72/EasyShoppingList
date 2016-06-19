package com.dandersen.app.easyshoppinglist;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.dandersen.app.easyshoppinglist.data.SelectedViewEnum;
import com.dandersen.app.easyshoppinglist.prefs.Settings;
import com.dandersen.app.easyshoppinglist.data.ShoppingContract;
import com.dandersen.app.easyshoppinglist.utils.StringUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by dandersen on 05-06-2016.
 * AsyncTask for fetching nearby grocery stores from Google Places API.
 */
public class FetchShopTask extends AsyncTask<String, Void, ContentValues[]> {

    private final String LOG_TAG = FetchShopTask.class.getSimpleName();

    private MainActivity mActivity;

    // Error message to display to the user when control is transferred to ui thread.
    private String mErrorMessage = null;

    public boolean mTaskFinished = false;

    // Create a trust manager that does not validate certificate chains like the
    private static TrustManager[] sTrustManager = new TrustManager[] {
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    //No need to implement.
                }
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    //No need to implement.
                }
            }
    };

    public FetchShopTask(MainActivity activity) {
        mActivity = activity;

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, sTrustManager, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            mErrorMessage = e.toString();
            Log.e(LOG_TAG, "DSA LOG - FetchShopTask exception " + e.toString(), e);
            e.printStackTrace();
        }
    }

    /**
     * The activity calls detach when the activity is being destroyed.
     */
    void detach() {
        mActivity = null;
    }

    /**
     * Used the attach the activity to this AsyncTask on re-creation of the activity class
     * managing this task. attach is called by new instance of Activity after detach
     * from destroyed instance.
     * @param activity The activity managing this task.
     */
    void attach(MainActivity activity) {
        mActivity = activity;
    }

    @Override
    protected ContentValues[] doInBackground(String... params) {
        // If there's no location, there's nothing to look up.  Verify size of params.
        if (params.length == 0) {
            Log.i(LOG_TAG, "DSA LOG - doInBackground - no params");
            return null;
        }
        String location = params[0];

        // Find nearby place ids
        String nextPageToken = null;
        HashMap<String, Boolean> nearbyPlaces = new HashMap<>();

        do {
            // Request nearby places from Google API
            String placesJsonStr = getGoogleApiResponse(buildNearbySearchUri(location, nextPageToken));

            // Parse nearby places for the place ids
            nextPageToken = parseGoogleNearbyPlacesResult(placesJsonStr, nearbyPlaces);
        } while (nextPageToken != null);

        if (nearbyPlaces.isEmpty()) {
            mTaskFinished = true;
            return null;
        }

        // Fetch place ids from database
        HashSet<String> placeIdsInDb = new HashSet<>();
        Cursor c = null;
        try {
            if (mActivity == null) return null;
            c = mActivity.getContentResolver().query(
                    ShoppingContract.ShopEntry.CONTENT_URI,
                    new String[]{ShoppingContract.ShopEntry.COLUMN_PLACE_ID},
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null  // sort order
            );
            if (c != null && c.moveToFirst()) {
                do {
                    String placeId = c.getString(0);
                    if (placeId.isEmpty()) throw new AssertionError("Place id cannot be empty");
                    placeIdsInDb.add(placeId);
                } while (c.moveToNext());
            }
        }
        finally {
            if (c != null) c.close();
        }

        // ArrayList for updates
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        // ArrayList for new inserts
        ArrayList<ContentValues> contentValuesArray = new ArrayList<>();

        // Are we searching for new stores?
        boolean searchForNewStores = !Settings.getInstance().getNearbySearchAutomaticDone();

        // Loop over stores
        Iterator<String> nearbyPlacesIterator = nearbyPlaces.keySet().iterator();
        while (nearbyPlacesIterator.hasNext()) {
            String placeId = nearbyPlacesIterator.next();
            Boolean openNow = nearbyPlaces.get(placeId);
            if (placeIdsInDb.contains(placeId)) {
                if (openNow != null) {
                    // This store is already in the db - update opening hours
                    ops.add(ContentProviderOperation.newUpdate(ShoppingContract.ShopEntry.CONTENT_URI)
                            .withSelection(ShoppingContract.ShopEntry.COLUMN_PLACE_ID + " = ?",
                                    new String[]{ placeId })
                            .withValue(ShoppingContract.ShopEntry.COLUMN_OPEN_NOW, openNow ? 1 : 0)
                            .build());
                }
            }
            else {
                if (searchForNewStores) {
                    // Request place details from Google API
                    String placeDetailsJsonStr = getGoogleApiResponse(buildPlaceDetailsUri(placeId));

                    // Parse place details
                    ContentValues contentValues = parseGooglePlaceDetailsResult(placeDetailsJsonStr);
                    if (contentValues != null) {
                        contentValuesArray.add(contentValues);
                    }
                }
            }
        }

        // Update open now
        if (!ops.isEmpty()) {
            try {
                if (mActivity == null) return null;
                mActivity.getContentResolver().applyBatch(ShoppingContract.CONTENT_AUTHORITY, ops);
            }
            catch (Exception e){
                mErrorMessage = e.toString();
                Log.e(LOG_TAG, "DSA LOG - applyBatch - Exception " + e.toString(), e);
                e.printStackTrace();
            }
        }

        if (!contentValuesArray.isEmpty()) {
            ContentValues[] shopContentValuesList = new ContentValues[contentValuesArray.size()];
            for (int i = 0, end = contentValuesArray.size(); i < end; ++i) {
                shopContentValuesList[i] = contentValuesArray.get(i);
            }
            return shopContentValuesList;
        }

        mTaskFinished = true;
        return null;
    }

    @Override
    protected void onPostExecute(ContentValues[] contentValuesList) {
        if (mActivity == null) return;

        if (contentValuesList == null) {
            if (mErrorMessage != null) {
                Toast.makeText(mActivity, mErrorMessage, Toast.LENGTH_LONG).show();
            }
            else {
                // We only search automatically for nearby grocery stores once!
                Settings.getInstance().setNearbySearchAutomaticDone(true);
            }
            return;
        }
        else {
            // We only search automatically for nearby grocery stores once!
            Settings.getInstance().setNearbySearchAutomaticDone(true);
        }

        final ContentValues[] fContentValuesList = contentValuesList;

        new AlertDialog.Builder(mActivity)
                .setCancelable(true)
                .setTitle(R.string.dialog_import_shops_title)
                .setMessage(mActivity.getString(R.string.dialog_import_shops_question, Integer.toString(contentValuesList.length)))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        synchronized (this) {
                            mTaskFinished = true;

                            // Insert shops
                            int insertCount = mActivity.getContentResolver().bulkInsert(ShoppingContract.ShopEntry.CONTENT_URI, fContentValuesList);
                            Log.i(LOG_TAG, "DSA LOG - " + insertCount + " shop entries added to database");

                            // Insert shop categories
                            insertShopCategories(fContentValuesList);
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mTaskFinished = true;
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Insert shop categories for the newly inserted shops.
     * All categories are added for each shop with the default sort order.
     * @param shopContentValuesList Content values for the inserted shops.
     */
    private void insertShopCategories(final ContentValues[] shopContentValuesList) {
        // Get ids of inserted shops
        HashSet<Integer> shopIds = getShopIds(shopContentValuesList);
        if (shopIds.isEmpty()) return;

        // Get id and sort order of all categories
        HashMap<Integer, Integer> categoryList = categories();

        // Prepare content values for shop category entries
        ContentValues[] shopCategoriesValuesList = new ContentValues[shopIds.size() * categoryList.size()];
        int shopCategoriesCounter = 0;

        for (Integer shopId : shopIds) {
            for (HashMap.Entry<Integer, Integer> category : categoryList.entrySet()) {
                ContentValues shopCategoryValues = new ContentValues();
                shopCategoryValues.put(ShoppingContract.ShopCategoryEntry.COLUMN_SHOP_ID, shopId);
                shopCategoryValues.put(ShoppingContract.ShopCategoryEntry.COLUMN_CATEGORY_ID, category.getKey());
                shopCategoryValues.put(ShoppingContract.ShopCategoryEntry.COLUMN_SORTORDER, category.getValue());
                shopCategoriesValuesList[shopCategoriesCounter++] = shopCategoryValues;
            }
        }

        int insertCount = mActivity.getContentResolver().bulkInsert(ShoppingContract.ShopCategoryEntry.CONTENT_URI, shopCategoriesValuesList);
        Log.i(LOG_TAG, "DSA LOG - " + insertCount + " shop category entries added to database");
    }

    @NonNull
    private HashSet<Integer> getShopIds(final ContentValues[] shopContentValuesList) {
        final String OR_SEP = " OR ";
        final String EQUALS = " = ?";

        ArrayList<String> placeIds = new ArrayList<>();
        HashSet<Integer> res = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        for (ContentValues contentValues : shopContentValuesList) {
            String placeId = contentValues.getAsString(ShoppingContract.ShopEntry.COLUMN_PLACE_ID);
            if (sb.length() != 0) sb.append(OR_SEP);
            sb.append(ShoppingContract.ShopEntry.COLUMN_PLACE_ID);
            sb.append(EQUALS);
            placeIds.add(placeId);
        }

        Cursor c = null;
        try {
            if (mActivity == null) return res;
            c = mActivity.getContentResolver().query(
                    ShoppingContract.ShopEntry.CONTENT_URI,
                    new String[]{ ShoppingContract.ShopEntry._ID },
                    sb.toString(),                                    // cols for "where" clause
                    placeIds.toArray(new String[placeIds.size()]),    // values for "where" clause
                    null                                              // sort order
            );
            if (c != null && c.moveToFirst()) {
                do {
                    res.add(c.getInt(0));
                } while (c.moveToNext());
            }
        }
        finally {
            if (c != null) c.close();
        }

        return res;
    }

    @NonNull
    private HashMap<Integer, Integer> categories() {
        HashMap<Integer, Integer> res = new HashMap<>();
        Cursor c = null;
        try {
            if (mActivity == null) return res;
            c = mActivity.getContentResolver().query(
                    ShoppingContract.CategoryEntry.CONTENT_URI,
                    new String[]{ ShoppingContract.CategoryEntry._ID ,
                            ShoppingContract.CategoryEntry.COLUMN_SORT_ORDER },
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null  // sort order
            );
            if (c != null && c.moveToFirst()) {
                do {
                    res.put(c.getInt(0), c.getInt(1));
                } while (c.moveToNext());
            }
        }
        finally {
            if (c != null) c.close();
        }
        return res;
    }

    private Uri buildNearbySearchUri(String location, @Nullable String nextPageToken) {
        // Construct the URL for the Google Places API query
        final String PLACES_BASE_URL        = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
        final String LOCATION_PARAM         = "location";
        final String TYPE_PARAM             = "type";
        final String RADIUS_PARAM           = "radius";
        final String APP_ID                 = "key";
        final String TYPE_VALUE             = "grocery_or_supermarket";
        final String PAGE_TOKEN             = "pagetoken";

        Uri.Builder uriBuilder = Uri.parse(PLACES_BASE_URL).buildUpon()
                .appendQueryParameter(LOCATION_PARAM, location)
                .appendQueryParameter(RADIUS_PARAM, Integer.toString(Settings.getInstance().getNearbySearchRadius()))
                .appendQueryParameter(TYPE_PARAM, TYPE_VALUE)
                .appendQueryParameter(APP_ID, BuildConfig.GOOGLE_PLACES_API_KEY);

        if (nextPageToken != null) {
            uriBuilder.appendQueryParameter(PAGE_TOKEN, nextPageToken);
        }

        return uriBuilder.build();
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
        final String NEWLINE = "\n";

        // These need to be declared outside the try/catch so they can be closed in the finally block.
        HttpsURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String placesJsonStr = null;

        try {
            URL url = new URL(builtUri.toString());

            // Log URL
            Log.i(LOG_TAG, "DSA LOG - Built URL for Google Places API " + url);

            // Create the request to Google Places, and open the connection
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder stringBuilder = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                Log.i(LOG_TAG, "DSA LOG - getGoogleApiResponse - no inputStream");
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                stringBuilder.append(line);
                stringBuilder.append(NEWLINE);
            }

            if (stringBuilder.length() == 0) {
                // Stream was empty.  No point in parsing.
                Log.i(LOG_TAG, "DSA LOG - getGoogleApiResponse - empty stream");
                return null;
            }
            placesJsonStr = stringBuilder.toString();
        } catch (IOException e) {
            mErrorMessage = e.toString();
            Log.e(LOG_TAG, "DSA LOG - getGoogleApiResponse - IOException " + e.toString(), e);
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

    private String parseGoogleNearbyPlacesResult(final String response,
                                                 HashMap<String, Boolean> nearbyPlaces) {
        final String RESULTS            = "results";
        final String PLACE_ID           = "place_id";
        final String NEXT_PAGE_TOKEN    = "next_page_token";
        final String OPENING_HOURS      = "opening_hours";
        final String OPEN_NOW           = "open_now";

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
                    String placeId = null;
                    Boolean openNow = null;
                    if (jsonArray.getJSONObject(i).has(PLACE_ID)) {
                        placeId = jsonArray.getJSONObject(i).optString(PLACE_ID);
                        if (placeId.isEmpty()) placeId = null;
                    }
                    if (jsonArray.getJSONObject(i).has(OPENING_HOURS)) {
                        JSONObject openingHoursObject = jsonArray.getJSONObject(i).getJSONObject(OPENING_HOURS);
                        if (openingHoursObject.has(OPEN_NOW)) {
                            openNow = openingHoursObject.optBoolean(OPEN_NOW);
                        }
                    }
                    if (placeId != null) {
                        nearbyPlaces.put(placeId, openNow);
                    }
                }
            }
        } catch (Exception e) {
            mErrorMessage = e.toString();
            Log.e(LOG_TAG, "parseGoogleNearbyPlacesResult exception", e);
            e.printStackTrace();
        }

        return nextPageToken;
    }

    /**
     * Parses the Google API Place details result.
     * https://developers.google.com/places/web-service/details#PlaceDetailsResults
     * @param response The JSON text from the API
     * @return ContentValues to insert an entry into the shop table
     */
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

                addValueToContentValues(contentValues, ShoppingContract.ShopEntry.COLUMN_FORMATTED_ADDRESS, jsonResult.optString(FORMATTED_ADDRESS));
                addValueToContentValues(contentValues, ShoppingContract.ShopEntry.COLUMN_PHONE_NUMBER, jsonResult.optString(FORMATTED_PHONE_NUMBER));

                parseGeometry(jsonResult, contentValues);

                addValueToContentValues(contentValues, ShoppingContract.ShopEntry.COLUMN_NAME, jsonResult.optString(NAME));
                addValueToContentValues(contentValues, ShoppingContract.ShopEntry.COLUMN_WEBSITE, jsonResult.optString(WEBSITE));

                // Place id must exist
                contentValues.put(ShoppingContract.ShopEntry.COLUMN_PLACE_ID, jsonResult.getString(PLACE_ID));

                // Parse opening hours
                parseOpeningHours(jsonResult, contentValues);

                // Fix address components
                fixAddressComponents(contentValues);
            }
        } catch (Exception e) {
            mErrorMessage = e.toString();
            Log.e(LOG_TAG, "parseGooglePlaceDetailsResult exception", e);
            e.printStackTrace();
        }
        return contentValues;

    }

    private void addValueToContentValues(ContentValues contentValues, String columnName, String value) {
        if (!value.isEmpty()) {
            contentValues.put(columnName, value);
        }
    }

    private void parseAddressComponents(JSONObject jsonResult, ContentValues contentValues) {
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
                                switch (firstAddressType) {
                                    case STREET_NUMBER:
                                        contentValues.put(ShoppingContract.ShopEntry.COLUMN_STREET_NUMBER, longName);
                                        break;
                                    case ROUTE:
                                        contentValues.put(ShoppingContract.ShopEntry.COLUMN_STREET, longName);
                                        break;
                                    case SUB_LOCALITY:     // fall through
                                    case SUB_LOCALITY_1:
                                        contentValues.put(ShoppingContract.ShopEntry.COLUMN_CITY, longName);
                                        break;
                                    case LOCALITY:
                                        if (contentValues.getAsString(ShoppingContract.ShopEntry.COLUMN_CITY) == null) {
                                            contentValues.put(ShoppingContract.ShopEntry.COLUMN_CITY, longName);
                                        }
                                        break;
                                    case COUNTRY:
                                        contentValues.put(ShoppingContract.ShopEntry.COLUMN_COUNTRY, longName);
                                        break;
                                    case POSTAL_CODE:
                                        contentValues.put(ShoppingContract.ShopEntry.COLUMN_POSTAL_CODE, longName);
                                        break;
                                    default:
                                        // Do nothing
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            mErrorMessage = e.toString();
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
            mErrorMessage = e.toString();
            Log.e(LOG_TAG, "parseGeometry exception", e);
            e.printStackTrace();
        }
    }

    void parseOpeningHours(JSONObject jsonResult, ContentValues contentValues) {
        final String OPENING_HOURS          = "opening_hours";
        final String OPEN_NOW               = "open_now";
        final String PERIODS                = "periods";
        final String CLOSE                  = "close";
        final String OPEN                   = "open";
        final String DAY                    = "day";
        final String TIME                   = "time";

        try {
            if (jsonResult.has(OPENING_HOURS)) {
                JSONObject openingHoursObject = jsonResult.getJSONObject(OPENING_HOURS);
                if (openingHoursObject.has(OPEN_NOW)) {
                    Boolean openNow = openingHoursObject.optBoolean(OPEN_NOW);
                    contentValues.put(ShoppingContract.ShopEntry.COLUMN_OPEN_NOW, openNow ? 1 : 0);
                }
                if (openingHoursObject.has(PERIODS)) {
                    String periods = "";
                    JSONArray periodsArray = openingHoursObject.getJSONArray(PERIODS);
                    for (int i = 0, end = periodsArray.length(); i < end; ++i) {
                        JSONObject dayObject = periodsArray.getJSONObject(i);
                        JSONObject openObject = dayObject.getJSONObject(OPEN);
                        if (!periods.isEmpty()) periods += StringUtil.DAY_SEP;
                        periods += openObject.optString(DAY);
                        periods += StringUtil.DAY_TIME_SEP;
                        periods += openObject.optString(TIME);
                        if (dayObject.has(CLOSE)) {
                            // If there is no close section, the store is always open
                            periods += StringUtil.OPEN_CLOSE_SEP;
                            JSONObject closeObject = dayObject.getJSONObject(CLOSE);
                            periods += closeObject.optString(DAY);
                            periods += StringUtil.DAY_TIME_SEP;
                            periods += closeObject.optString(TIME);
                        }
                    }
                    if (!periods.isEmpty()) {
                        contentValues.put(ShoppingContract.ShopEntry.COLUMN_OPENING_HOURS, periods);
                    }
                }
            }
        } catch (Exception e) {
            mErrorMessage = e.toString();
            Log.e(LOG_TAG, "parseOpeningHours exception", e);
            e.printStackTrace();
        }
    }

    void fixAddressComponents(ContentValues contentValues) {
        String formattedAddressStr = contentValues.getAsString(ShoppingContract.ShopEntry.COLUMN_FORMATTED_ADDRESS);
        if (formattedAddressStr.isEmpty()) return;
        StringUtil.FormattedAddress formattedAddress = StringUtil.formatAddress(formattedAddressStr);
        String streetName = contentValues.getAsString(ShoppingContract.ShopEntry.COLUMN_STREET);
        String streetNumber = contentValues.getAsString(ShoppingContract.ShopEntry.COLUMN_STREET_NUMBER);
        String postalCode = contentValues.getAsString(ShoppingContract.ShopEntry.COLUMN_POSTAL_CODE);
        String city = contentValues.getAsString(ShoppingContract.ShopEntry.COLUMN_CITY);
        String country = contentValues.getAsString(ShoppingContract.ShopEntry.COLUMN_COUNTRY);
        if (streetName == null) {
            contentValues.put(ShoppingContract.ShopEntry.COLUMN_STREET, formattedAddress.mStreet);
        }
        if (streetNumber == null) {
            contentValues.put(ShoppingContract.ShopEntry.COLUMN_STREET_NUMBER, formattedAddress.mStreetNumber);
        }
        if (postalCode == null) {
            contentValues.put(ShoppingContract.ShopEntry.COLUMN_POSTAL_CODE, formattedAddress.mPostalCode);
        }
        if (city == null) {
            contentValues.put(ShoppingContract.ShopEntry.COLUMN_CITY, formattedAddress.mCity);
        }
        if (country == null) {
            contentValues.put(ShoppingContract.ShopEntry.COLUMN_COUNTRY, formattedAddress.mCountry);
        }
    }
}
