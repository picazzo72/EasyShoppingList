package com.dandersen.app.easyshoppinglist;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.util.Pair;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dandersen.app.easyshoppinglist.data.ShoppingContract;
import com.dandersen.app.easyshoppinglist.ui.ItemTouchHelperAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Dan on 16-06-2016.
 * {@link ActiveListAdapter} exposes a list of stores and products
 * from a {@link android.database.Cursor} to a {@link android.support.v7.widget.RecyclerView}.
 * The RecyclerView method used is described here:
 * https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf#.7mhftpy4t
 */
public class ActiveListAdapter extends RecyclerView.Adapter
        implements ItemTouchHelperAdapter {

    private final String LOG_TAG = ActiveListAdapter.class.getSimpleName();

    private static boolean mTwoPaneLayout = true;

    private Context mContext;
    private CursorAdapter mCursorAdapter;

    private class ShopPositionMap extends HashMap<Integer, Pair<Long, String> > {  }

    private ShopPositionMap mShopPositionMap = new ShopPositionMap();

    private final int PRODUCT_VIEW_TYPE     = 1;
    private final int SHOP_VIEW_TYPE        = 2;

    public ActiveListAdapter(Context context, Cursor cursor, int flags) {
        super();

        mContext = context;

        mCursorAdapter = new CursorAdapter(context, cursor, flags) {
            @Override
            public int getViewTypeCount() {
                return 1;
            }

            /**
             * This is where we fill-in the views with the contents of the cursor.
             */
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                ProductViewHolder productViewHolder = (ProductViewHolder) view.getTag();

                // Product name
                String productName = cursor.getString(ActiveListFragment.COL_PRODUCT_NAME);
                productViewHolder.productNameView.setText(productName);

                // Category name
                String categoryName = cursor.getString(ActiveListFragment.COL_CATEGORY_NAME);
                productViewHolder.categoryNameView.setText(categoryName);

                // Category color
                String color = cursor.getString(ActiveListFragment.COL_CATEGORY_COLOR);
                String[] colorItems = color.split(",");
                if (colorItems.length == 3) {
                    int r = Integer.valueOf(colorItems[0]);
                    int g = Integer.valueOf(colorItems[1]);
                    int b = Integer.valueOf(colorItems[2]);
                    view.setBackgroundColor(Color.argb(50, r, g, b));
                }
            }

            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(R.layout.list_item_product_active_list, parent, false);
            }
        };
    }

    public void setTwoPaneLayout(boolean twoPaneLayout) {
        mTwoPaneLayout = twoPaneLayout;
    }

    /**
     * Cache of the children views for a product list item.
     */
    private static class ProductViewHolder extends RecyclerView.ViewHolder {
        public final TextView productNameView;
        public final TextView categoryNameView;

        public ProductViewHolder(View view) {
            super(view);

            productNameView     = (TextView) view.findViewById(R.id.list_item_product_name);
            categoryNameView    = (TextView) view.findViewById(R.id.list_item_category_name);
        }
    }

    /**
     * Cache of the children views for a shop list item.
     */
    private static class ShopViewHolder extends RecyclerView.ViewHolder {
        public final ImageView openNowView;
        public final TextView shopNameView;

        public ShopViewHolder(View view) {
            super(view);

            openNowView         = (ImageView) view.findViewById(R.id.list_item_shop_open_now);
            shopNameView        = (TextView) view.findViewById(R.id.list_item_shop_name);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mShopPositionMap.get(position) == null) {
            return PRODUCT_VIEW_TYPE;
        }
        return SHOP_VIEW_TYPE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case PRODUCT_VIEW_TYPE:
            {
                View view = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
                ProductViewHolder productViewHolder = new ProductViewHolder(view);
                view.setTag(productViewHolder);
                return productViewHolder;
            }
            case SHOP_VIEW_TYPE:
            {
                View view = LayoutInflater.from(mContext).inflate(
                        R.layout.list_item_shop_active_list, parent, false);
                ShopViewHolder shopViewHolder = new ShopViewHolder(view);
                view.setTag(shopViewHolder);
                return shopViewHolder;
            }
            default:
                throw new UnsupportedOperationException("View type unknown: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Pair<Long, String> shopPair = mShopPositionMap.get(position);
        if (shopPair == null) {
            mCursorAdapter.getCursor().moveToPosition(getRealPosition(position));
            mCursorAdapter.bindView(holder.itemView, mContext, mCursorAdapter.getCursor());
        }
        else {
            ShopViewHolder shopViewHolder = (ShopViewHolder) holder.itemView.getTag();

            // Shop name
            shopViewHolder.shopNameView.setText(shopPair.second);
        }
    }

    @Override
    public int getItemCount() {
        return mCursorAdapter.getCount() + mShopPositionMap.size();
    }

    public Cursor swapCursor(Cursor newCursor) {
        // Calculate shop positions from the products
        updateShopPositions(newCursor);

        Cursor cursor = mCursorAdapter.swapCursor(newCursor);

        notifyDataSetChanged();

        return cursor;
    }

    private void updateShopPositions(Cursor cursor) {
        mShopPositionMap.clear();
        if (cursor != null && cursor.moveToFirst()) {
            // Load all different shop positions from cursor
            int index = 0;
            Set<Long> shopIds = new HashSet<>();
            do {
                Long shopId = cursor.getLong(ActiveListFragment.COL_SHOP_ID);
                if (shopId != ShoppingContract.ShopEntry.DEFAULT_STORE_ID) {
                    if (!shopIds.contains(shopId)) {
                        shopIds.add(shopId);
                        String shopName = cursor.getString(ActiveListFragment.COL_SHOP_NAME);
                        mShopPositionMap.put(index, Pair.create(shopId, shopName));
                        ++index;
                    }
                }
                ++index;
            } while (cursor.moveToNext());
        }
    }

    private int getRealPosition(int position) {
        int res = position;
        for (Integer shopPosition : mShopPositionMap.keySet()) {
            if (position >= shopPosition) {
                --res;
            }
        }
        return res;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onDrop(int fromPosition, int toPosition) {
        Log.i(LOG_TAG, "DSA LOG - onItemMove - " + fromPosition + " to " + toPosition);

        // ArrayList for updates
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        int order = toPosition;
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i <= toPosition; ++i) {
                addUpdateOperation(ops, i, order);
                if (i == fromPosition) {
                    order = fromPosition;
                } else {
                    order++;
                }
            }
        }
        else {
            for (int i = fromPosition; i >= toPosition; --i) {
                addUpdateOperation(ops, i, order);
                if (i == fromPosition) {
                    order = fromPosition;
                } else {
                    order--;
                }
            }
        }

        if (!ops.isEmpty()) {
            try {
                mContext.getContentResolver().applyBatch(ShoppingContract.CONTENT_AUTHORITY, ops);
            }
            catch (Exception e){
                Log.e(LOG_TAG, "DSA LOG - applyBatch - Exception " + e.toString(), e);
                e.printStackTrace();
            }
        }

        notifyItemMoved(fromPosition, toPosition);
    }

    private void addUpdateOperation(ArrayList<ContentProviderOperation> ops, int position, int order) {
        Cursor cursor = mCursorAdapter.getCursor();
        cursor.moveToPosition(position);
        int id = cursor.getInt(ActiveListFragment.COL_SHOPPING_LIST_PRODUCTS_ID);
        ops.add(ContentProviderOperation.newUpdate(ShoppingContract.ShoppingListProductEntry.CONTENT_URI)
                .withSelection(ShoppingContract.ShoppingListProductEntry._ID + " = ?",
                        new String[]{Integer.toString(id)})
                .withValue(ShoppingContract.ShoppingListProductEntry.COLUMN_SORT_ORDER, order)
                .build());
    }

    @Override
    public void onItemDismiss(int position) {
        Cursor cursor = mCursorAdapter.getCursor();
        cursor.moveToPosition(position);
        int id = cursor.getInt(ActiveListFragment.COL_SHOPPING_LIST_PRODUCTS_ID);

        mContext.getContentResolver().delete(
                ShoppingContract.ShoppingListProductEntry.buildShoppingListProductsUri(id),
                null,
                null
        );

        notifyItemRemoved(position);
    }
}