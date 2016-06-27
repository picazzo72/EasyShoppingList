package com.dandersen.app.easyshoppinglist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dandersen.app.easyshoppinglist.data.ActiveListUtil;
import com.dandersen.app.easyshoppinglist.data.ShoppingContract;
import com.dandersen.app.easyshoppinglist.ui.ActionModeCursorAdapter;
import com.dandersen.app.easyshoppinglist.utils.StringUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link ShopAdapter} exposes a list of shops
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ShopAdapter extends ActionModeCursorAdapter {

    private final String LOG_TAG = ShopAdapter.class.getSimpleName();

    private boolean mTwoPaneLayout = true;

    // Set to keep track on which shops are on active list. This is updated on swapCursor
    private HashSet<Long> mShopOnActiveListSet = new HashSet<>();

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

        // Shopping cart icon
        Long shopId = cursor.getLong(ShopFragment.COL_SHOP_ID);
        viewHolder.shoppingCartView.setTag(R.id.shop_id_tag, shopId);

        // Check shopping list product table for this shop
        if (mShopOnActiveListSet.contains(shopId)) {
            setShopAsOnActiveList(viewHolder.shoppingCartView);
        }
        else {
            setShopAsOffActiveList(viewHolder.shoppingCartView);
        }

        setupAddToActiveListClickListener(viewHolder);
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        // Load shop ids which are on the active shopping list
        mShopOnActiveListSet.clear();
        if (newCursor != null && newCursor.moveToFirst()) {
            // Load all shops from cursor
            Set<Long> shopIdSet = new HashSet<>();
            do {
                Long shopId = newCursor.getLong(ShopFragment.COL_SHOP_ID);
                shopIdSet.add(shopId);
            } while (newCursor.moveToNext());

            if (!shopIdSet.isEmpty()) {
                // Build in statement for all shops
                final String COMMA = "," , QUESTIONMARK = "?";
                String selection = ShoppingContract.ShoppingListProductEntry.COLUMN_SHOP_ID + " IN (";
                String[] selectionArgs = new String[shopIdSet.size()];
                int index = 0;
                for (Long shopId : shopIdSet) {
                    if (index != 0) selection += COMMA;
                    selection += QUESTIONMARK;
                    selectionArgs[index++] = Long.toString(shopId);
                }
                selection += ")";

                // Load all unique shop ids from shopping list product
                Cursor shoppingListProductCursor = null;
                try {
                    shoppingListProductCursor = mContext.getContentResolver().query(
                            ShoppingContract.ShoppingListProductEntry.CONTENT_URI,
                            new String[] { ShoppingContract.ShoppingListProductEntry.COLUMN_SHOP_ID },
                            selection,
                            selectionArgs,
                            null);
                    if (shoppingListProductCursor != null && shoppingListProductCursor.moveToFirst()) {
                        do {
                            Long shopId = shoppingListProductCursor.getLong(0);
                            mShopOnActiveListSet.add(shopId);
                        } while (shoppingListProductCursor.moveToNext());
                    }
                }
                finally {
                    if (shoppingListProductCursor != null) shoppingListProductCursor.close();
                }
            }
        }

        return super.swapCursor(newCursor);
    }

    private void setupAddToActiveListClickListener(ViewHolder viewHolder) {
        viewHolder.shoppingCartView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Long shopId = (Long) view.getTag(R.id.shop_id_tag);
                if (shopId == null) return;

                ImageView shoppingCartView = (ImageView) view;
                if ((int)shoppingCartView.getTag(R.id.drawable_id_tag) == R.drawable.ic_shopping_cart_red_24dp) {
                    ActiveListUtil.removeShop(mContext, shopId);

                    mShopOnActiveListSet.remove(shopId);

                    setShopAsOffActiveList(shoppingCartView);
                }
                else {
                    ActiveListUtil.addShop(mContext, shopId);

                    mShopOnActiveListSet.add(shopId);

                    setShopAsOnActiveList(shoppingCartView);
                }
            }
        });
    }

    private void setShopAsOffActiveList(ImageView shoppingCartView) {
        shoppingCartView.setImageResource(R.drawable.ic_shopping_cart_black_24dp);
        shoppingCartView.setTag(R.id.drawable_id_tag, R.drawable.ic_shopping_cart_black_24dp);
    }

    private void setShopAsOnActiveList(ImageView shoppingCartView) {
        shoppingCartView.setImageResource(R.drawable.ic_shopping_cart_red_24dp);
        shoppingCartView.setTag(R.id.drawable_id_tag, R.drawable.ic_shopping_cart_red_24dp);
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