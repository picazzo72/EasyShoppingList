package com.dandersen.app.easyshoppinglist;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.method.KeyListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.dandersen.app.easyshoppinglist.data.ShoppingContract;
import com.dandersen.app.easyshoppinglist.utils.ImplicitIntentUtil;
import com.dandersen.app.easyshoppinglist.utils.ParcelableString;
import com.dandersen.app.easyshoppinglist.utils.StringUtil;

import java.util.ArrayList;

/**
 * Created by Dan on 09-06-2016.
 * Detail shop fragment which shows shop details.
 */
public class DetailShopFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = DetailShopFragment.class.getSimpleName();

    // Tag for URI sent from caller
    static final String DETAIL_URI = "URI";

    // Tag for Shop id
    static final String SHOP_ID_TAG = "SHOP_ID";

    // URI used by this fragment to retrieve shop details
    private Uri mUri;

    // Loader id
    private static final int DETAIL_LOADER_ID = 0;

    // Used for showing place on Map in external app
    private static String mLocation = null;
    private static String mLocationLabel;

    // Used for showing website in browser
    private static String mWebsite = null;

    // Used for dialing phone number
    private static String mPhoneNumber = null;

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] DETAIL_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            ShoppingContract.ShopEntry.TABLE_NAME + "." + ShoppingContract.ShopEntry._ID,
            ShoppingContract.ShopEntry.COLUMN_NAME,
            ShoppingContract.ShopEntry.COLUMN_STREET,
            ShoppingContract.ShopEntry.COLUMN_STREET_NUMBER,
            ShoppingContract.ShopEntry.COLUMN_POSTAL_CODE,
            ShoppingContract.ShopEntry.COLUMN_CITY,
            ShoppingContract.ShopEntry.COLUMN_STATE,
            ShoppingContract.ShopEntry.COLUMN_COUNTRY,
            ShoppingContract.ShopEntry.COLUMN_PHONE_NUMBER,
            ShoppingContract.ShopEntry.COLUMN_WEBSITE,
            ShoppingContract.ShopEntry.COLUMN_LOCATION,
            ShoppingContract.ShopEntry.COLUMN_OPENING_HOURS,
            ShoppingContract.ShopEntry.COLUMN_OPEN_NOW
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_SHOP_ID                        = 0;
    static final int COL_SHOP_NAME                      = 1;
    static final int COL_SHOP_STREET                    = 2;
    static final int COL_SHOP_STREET_NUMBER             = 3;
    static final int COL_SHOP_POSTAL_CODE               = 4;
    static final int COL_SHOP_CITY                      = 5;
    static final int COL_SHOP_STATE                     = 6;
    static final int COL_SHOP_COUNTRY                   = 7;
    static final int COL_SHOP_PHONE_NUMBER              = 8;
    static final int COL_SHOP_WEBSITE                   = 9;
    static final int COL_SHOP_LOCATION                  = 10;
    static final int COL_SHOP_OPENING_HOURS             = 11;
    static final int COL_SHOP_OPEN_NOW                  = 12;

    public DetailShopFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private static class ViewHolder {
        public final EditText titleView;
        public final EditText addressView;
        public final EditText countryView;
        public final EditText websiteView;
        public final EditText phoneNumberView;

        public final TextView openingHoursDaysView;
        public final TextView openingHoursHoursView;

        public final ImageView addressIconView;
        public final ImageView websiteIconView;
        public final ImageView phoneNumberIconView;
        public final ImageView openingHoursIconView;

        public final Button categoryListButton;

        private ArrayList<EditText> editTextArrayList = new ArrayList<>();

        ViewHolder(View view) {
            titleView               = (EditText) view.findViewById(R.id.shop_detail_name);
            addressView             = (EditText) view.findViewById(R.id.shop_detail_address);
            countryView             = (EditText) view.findViewById(R.id.shop_detail_country);
            websiteView             = (EditText) view.findViewById(R.id.shop_detail_website);
            phoneNumberView         = (EditText) view.findViewById(R.id.shop_detail_phone_number);

            openingHoursDaysView    = (TextView) view.findViewById(R.id.shop_detail_opening_hours_days);
            openingHoursHoursView   = (TextView) view.findViewById(R.id.shop_detail_opening_hours_hours);

            addressIconView         = (ImageView) view.findViewById(R.id.shop_detail_address_icon);
            websiteIconView         = (ImageView) view.findViewById(R.id.shop_detail_website_icon);
            phoneNumberIconView     = (ImageView) view.findViewById(R.id.shop_detail_phone_number_icon);
            openingHoursIconView    = (ImageView) view.findViewById(R.id.shop_detail_opening_hours_icon);

            categoryListButton      = (Button) view.findViewById(R.id.shop_detail_categories_button);

            editTextArrayList.add(titleView);
            editTextArrayList.add(addressView);
            editTextArrayList.add(countryView);
            editTextArrayList.add(websiteView);
            editTextArrayList.add(phoneNumberView);

            setTextEditable(false);
            setOnClickListeners();
        }

        public void setTextEditable(boolean editable) {
            if (editable) {
                for (EditText editText : editTextArrayList) {
                    editText.setKeyListener((KeyListener) editText.getTag());
                }
            }
            else {
                for (EditText editText : editTextArrayList) {
                    editText.setTag(editText.getKeyListener());
                    editText.setKeyListener(null);
                }
            }
        }

        private void setOnClickListeners() {
            // OnClick listener for map location
            addressView.setOnClickListener(createOnClickListenerForLocation());
            countryView.setOnClickListener(createOnClickListenerForLocation());
            addressIconView.setOnClickListener(createOnClickListenerForLocation());

            // OnClick listener for website
            websiteView.setOnClickListener(createOnClickListenerForWebsite());
            websiteIconView.setOnClickListener(createOnClickListenerForWebsite());

            // OnClick listener for phone number
            phoneNumberView.setOnClickListener(createOnClickListenerForPhoneNumber());
            phoneNumberIconView.setOnClickListener(createOnClickListenerForPhoneNumber());
        }

        private View.OnClickListener createOnClickListenerForLocation() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mLocation != null) {
                        ImplicitIntentUtil.openLocationInMap(v.getContext(),
                                mLocation, mLocationLabel);
                    }
                }
            };
        }

        private View.OnClickListener createOnClickListenerForWebsite() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mWebsite != null) {
                        ImplicitIntentUtil.openUrlInBrowser(v.getContext(), mWebsite);
                    }
                }
            };
        }

        private View.OnClickListener createOnClickListenerForPhoneNumber() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPhoneNumber != null) {
                        ImplicitIntentUtil.openPhoneNumberInDialer(v.getContext(), mPhoneNumber);
                    }
                }
            };
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Get Uri from the arguments sent from the caller
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailShopFragment.DETAIL_URI);
        }

        View view = inflater.inflate(R.layout.fragment_shop_detail, container, false);

        // Set view holder for easy access to view items
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        if (mUri != null) {
            // OnClick listener for category list button
            viewHolder.categoryListButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Create and start explicit intent
                    Intent intent = new Intent(getActivity(), ShopCategoriesActivity.class).setData(mUri);
                    intent.putExtra(DetailShopFragment.SHOP_ID_TAG,
                            ShoppingContract.ShopEntry.getShopIdFromUri(mUri));
                    startActivity(intent);
                }
            });
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_shop_detail, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            if (getView() != null) {
                // Get view holder
                ViewHolder viewHolder = (ViewHolder) getView().getTag();

                // Make text editable
                viewHolder.setTextEditable(true);
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) return;
        if (getView() == null) return;

        // Get view holder
        ViewHolder viewHolder = (ViewHolder) getView().getTag();

        // Name
        String name = data.getString(COL_SHOP_NAME);
        viewHolder.titleView.setText(name);

        // Address
        StringUtil.FormattedAddress formattedAddress = StringUtil.formattedAddress(
                data.getString(COL_SHOP_STREET),
                data.getString(COL_SHOP_STREET_NUMBER),
                data.getString(COL_SHOP_POSTAL_CODE),
                data.getString(COL_SHOP_CITY),
                data.getString(COL_SHOP_STATE),
                null
        );
        viewHolder.addressView.setText(formattedAddress.toString());

        // Country
        String country = data.getString(COL_SHOP_COUNTRY);
        viewHolder.countryView.setText(country);

        // Website
        if (!data.isNull(COL_SHOP_WEBSITE)) {
            String website = data.getString(COL_SHOP_WEBSITE);
            viewHolder.websiteView.setText(website);

            // Prepare for website in browser
            mWebsite = website;
        }

        // Phone number
        if (!data.isNull(COL_SHOP_PHONE_NUMBER)) {
            String phoneNumber = data.getString(COL_SHOP_PHONE_NUMBER);
            viewHolder.phoneNumberView.setText(phoneNumber);

            // Prepare for phone number
            mPhoneNumber = phoneNumber;
        }

        // Opening hours
        if (!data.isNull(COL_SHOP_OPENING_HOURS)) {
            String openingHours = data.getString(COL_SHOP_OPENING_HOURS);
            String[] openingHoursDisplay = StringUtil.buildOpeningHours(getContext(), openingHours);
            viewHolder.openingHoursDaysView.setVisibility(View.VISIBLE);
            viewHolder.openingHoursHoursView.setVisibility(View.VISIBLE);
            viewHolder.openingHoursDaysView.setText(Html.fromHtml(openingHoursDisplay[0]));
            viewHolder.openingHoursHoursView.setText(Html.fromHtml(openingHoursDisplay[1]));
        }
        else {
            viewHolder.openingHoursDaysView.setVisibility(View.GONE);
            viewHolder.openingHoursHoursView.setVisibility(View.GONE);
        }

        // Open now
        if (!data.isNull(COL_SHOP_OPEN_NOW)) {
            viewHolder.openingHoursIconView.setVisibility(View.VISIBLE);
            if (data.getInt(COL_SHOP_OPEN_NOW) == 0) {
                // TODO
                viewHolder.openingHoursIconView.setImageResource(R.drawable.ic_schedule_no_black_18dp);
            }
            else {
                viewHolder.openingHoursIconView.setImageResource(R.drawable.ic_schedule_black_24dp);
            }
        }
        else {
            viewHolder.openingHoursIconView.setVisibility(View.GONE);
        }

        // Prepare for location on Map
        mLocation = data.getString(COL_SHOP_LOCATION);
        mLocationLabel = name;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mUri != null) {
            return new CursorLoader(getActivity(),
                    mUri,                  // URI
                    DETAIL_COLUMNS,        // projection
                    null,                  // where
                    null,                  // binds
                    null);
        }

        return null;
    }

//    void onLocationChanged(String newLocation) {
//        // replace the uri, since the location has changed
//        if (mUri != null) {
//            long date = WeatherContract.WeatherEntry.getDateFromUri(mUri);
//            mUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
//            getLoaderManager().restartLoader(DETAIL_LOADER_ID, null, this);
//        }
//    }
}
