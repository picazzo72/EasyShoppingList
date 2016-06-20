package com.dandersen.app.easyshoppinglist;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dandersen.app.easyshoppinglist.data.ShoppingContract;
import com.dandersen.app.easyshoppinglist.ui.ActionModeCursorAdapter;
import com.dandersen.app.easyshoppinglist.utils.StringUtil;

import java.util.ArrayList;

/**
 * {@link ShopAdapter} exposes a list of shops
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ShopAdapter extends ActionModeCursorAdapter {

    private boolean mTwoPaneLayout = true;

    public ShopAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags, R.id.list_item_shop_shopping_cart, R.drawable.ic_shopping_cart_black_24dp);
    }

    public void setTwoPaneLayout(boolean twoPaneLayout) {
        mTwoPaneLayout = twoPaneLayout;
    }

    /**
     * Cache of the children views for a shop list item.
     */
    private static class ViewHolder {
        public final ImageView openNowView;
        public final TextView shopNameView;
        public final TextView shopAddressView;
        public final ImageView shoppingCartView;

        public ViewHolder(View view) {
            openNowView         = (ImageView) view.findViewById(R.id.list_item_shop_open_now);
            shopNameView        = (TextView) view.findViewById(R.id.list_item_shop_name);
            shopAddressView     = (TextView) view.findViewById(R.id.list_item_shop_address);
            shoppingCartView    = (ImageView) view.findViewById(R.id.list_item_shop_shopping_cart);
        }
    }

    /**
     * Remember that these views are reused as needed.
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
        super.bindView(view, context, cursor);

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Open now
        if (cursor.isNull(ShopFragment.COL_OPEN_NOW)) {
            viewHolder.openNowView.setVisibility(View.GONE);
        }
        else {
            viewHolder.openNowView.setVisibility(View.VISIBLE);
            int openNow = cursor.getInt(ShopFragment.COL_OPEN_NOW);
            if (openNow == 0) {
                viewHolder.openNowView.setImageResource(R.drawable.ic_schedule_no_black_18dp);
            }
            else {
                viewHolder.openNowView.setImageResource(R.drawable.ic_schedule_black_18dp);
            }
        }

        // Shop name
        String shopName = cursor.getString(ShopFragment.COL_SHOP_NAME);
        viewHolder.shopNameView.setText(shopName);

        String streetName = cursor.getString(ShopFragment.COL_SHOP_STREET);
        String streetNumber = cursor.getString(ShopFragment.COL_SHOP_STREET_NUMBER);
        String city = cursor.getString(ShopFragment.COL_SHOP_CITY);
        viewHolder.shopAddressView.setText(StringUtil.buildShopAddress(
                streetName, streetNumber, city));
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

    /**
     * We override this to let our activity know that action mode has changed.
     * @param actionMode Action mode (true) or not (false)
     */
    @Override
    public void setActionMode(boolean actionMode) {
        super.setActionMode(actionMode);

        if (actionMode) {
            // Notify activity about the action mode, so that ui elements may be hidden
            ((ShopFragment.Callback)mContext).onShopFragmentActionMode(true);
        }
        else {
            // Notify activity about the action mode, so that ui elements may be hidden
            ((ShopFragment.Callback)mContext).onShopFragmentActionMode(false);
        }
    }

    @Override
    protected void onDeleteEntries(StringBuilder idSelection, String[] idSelectionArgs) {
        mContext.getContentResolver().delete(
                ShoppingContract.ShopEntry.CONTENT_URI,
                ShoppingContract.ShopEntry._ID + " IN ( " + idSelection.toString() + " )",
                idSelectionArgs
        );
    }
}