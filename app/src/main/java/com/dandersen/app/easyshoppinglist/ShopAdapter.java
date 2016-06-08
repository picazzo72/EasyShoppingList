package com.dandersen.app.easyshoppinglist;

/**
 * Created by Dan on 28-05-2016.
 */

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dandersen.app.easyshoppinglist.data.ShoppingContract;
import com.dandersen.app.easyshoppinglist.data.Utilities;

import java.util.ArrayList;

/**
 * {@link ShopAdapter} exposes a list of shops
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ShopAdapter extends CursorAdapter {

    private boolean mTwoPaneLayout = true;
    private boolean mActionMode = false;

    private SparseBooleanArray mSelectedItemsIds;

    public ShopAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);

        mSelectedItemsIds = new SparseBooleanArray();
    }

    public void setTwoPaneLayout(boolean twoPaneLayout) {
        mTwoPaneLayout = twoPaneLayout;
    }

    /**
     * Cache of the children views for a shop list item.
     */
    private static class ViewHolder {
        public final TextView shopNameView;
        public final TextView shopAddressView;
        public final ImageView shoppingCartView;

        public ViewHolder(View view) {
            shopNameView = (TextView) view.findViewById(R.id.list_item_shop_name);
            shopAddressView = (TextView) view.findViewById(R.id.list_item_shop_address);
            shoppingCartView = (ImageView) view.findViewById(R.id.list_item_shop_shopping_cart);
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

        if (mActionMode) {
            int position = cursor.getPosition();
            if (mSelectedItemsIds.get(position)) {
                // Item is checked
                viewHolder.shoppingCartView.setImageResource(R.drawable.ic_check_box_black_24dp);
            }
            else {
                // Item is not checked
                viewHolder.shoppingCartView.setImageResource(R.drawable.ic_check_box_outline_blank_black_24dp);
            }
        }
        else {
            viewHolder.shoppingCartView.setImageResource(R.drawable.ic_shopping_cart_black_24dp);
        }
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

    public void setActionMode(boolean actionMode) {
        mActionMode = actionMode;
        mSelectedItemsIds.clear();
    }

    public void remove(ArrayList<Integer> itemsToDelete) {
        final String itemSep = " , ";
        final String bind = "?";

        if (!itemsToDelete.isEmpty()) {
            ArrayList<String> bindIdList = new ArrayList();
            StringBuilder sb = new StringBuilder();
            for (Integer index : itemsToDelete) {
                Cursor cursor = (Cursor) getItem(index);
                int shopId = cursor.getInt(ShopFragment.COL_SHOP_ID);
                if (shopId > 0) {
                    if (sb.length() > 0) {
                        sb.append(itemSep);
                    }
                    sb.append(bind);
                    bindIdList.add(Integer.toString(shopId));
                }
            }
            String[] bindIdStringArray = new String[bindIdList.size()];
            bindIdStringArray = bindIdList.toArray(bindIdStringArray);
            mContext.getContentResolver().delete(
                    ShoppingContract.ShopEntry.CONTENT_URI,
                    ShoppingContract.ShopEntry._ID + " IN ( " + sb.toString() + " )",
                    bindIdStringArray
            );
            mSelectedItemsIds.clear();
            notifyDataSetChanged();
        }
    }

    public void toggleSelection(int position, boolean checked) {
        selectView(position, checked);
//        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);
        notifyDataSetChanged();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }
}