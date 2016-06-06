package com.dandersen.app.easyshoppinglist;

/**
 * Created by Dan on 28-05-2016.
 */

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dandersen.app.easyshoppinglist.data.Utilities;

/**
 * {@link ShopAdapter} exposes a list of shops
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ShopAdapter extends CursorAdapter {

    private static boolean mTwoPaneLayout = true;

    public ShopAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public void setTwoPaneLayout(boolean twoPaneLayout) {
        mTwoPaneLayout = twoPaneLayout;
    }

    /**
     * Cache of the children views for a category list item.
     */
    private static class ViewHolder {
        public final TextView shopNameView;
        public final TextView shopAddressView;

        public ViewHolder(View view) {
            shopNameView = (TextView) view.findViewById(R.id.list_item_shop_name);
            shopAddressView = (TextView) view.findViewById(R.id.list_item_shop_address);
        }
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = R.layout.list_item_shop;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        // set view holder for easy access to the view elements
        view.setTag(new ViewHolder(view));

        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    /**
     * This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Shop name
        String shopName = cursor.getString(ShopFragment.COL_SHOP_NAME);
        viewHolder.shopNameView.setText(shopName);

        String streetName = cursor.getString(ShopFragment.COL_SHOP_STREET);
        String streetNumber = cursor.getString(ShopFragment.COL_SHOP_STREET_NUMBER);
        String city = cursor.getString(ShopFragment.COL_SHOP_CITY);
        String formattedAddress = cursor.getString(ShopFragment.COL_FORMATTED_ADDRESS);
        viewHolder.shopAddressView.setText(Utilities.buildShopAddress(
                streetName, streetNumber, city, formattedAddress));
    }

    /**
     * Used to find the position of a given product in the list of products
     * @param id of product
     * @return position in the list
     */
    public int getItemPosition(long id) {
        for (int position=0, count = getCount(); position < count; ++position)
            if (this.getItemId(position) == id)
                return position;
        return -1;
    }
}