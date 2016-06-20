package com.dandersen.app.easyshoppinglist;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dandersen.app.easyshoppinglist.data.ShoppingContract;
import com.dandersen.app.easyshoppinglist.ui.ItemTouchHelperAdapter;

import java.util.ArrayList;

/**
 * Created by Dan on 16-06-2016.
 * {@link ShopCategoriesAdapter} exposes a list of shop categories
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 * The RecyclerView method used is described here:
 * https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf#.7mhftpy4t
 */
public class ShopCategoriesAdapter extends RecyclerView.Adapter
        implements ItemTouchHelperAdapter {

    private final String LOG_TAG = ShopCategoriesAdapter.class.getSimpleName();

    private static boolean mTwoPaneLayout = true;

    private Context mContext;
    private CursorAdapter mCursorAdapter;

    public ShopCategoriesAdapter(Context context, Cursor cursor, int flags) {
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
                ViewHolder viewHolder = (ViewHolder) view.getTag();

                // Category name
                String name = cursor.getString(ShopCategoriesFragment.COL_CATEGORY_NAME);
                viewHolder.nameView.setText(name);

                // Category color
                String color = cursor.getString(ShopCategoriesFragment.COL_COLOR);
                String[] colorItems = color.split(",");
                if (colorItems.length == 3) {
                    int r = Integer.valueOf(colorItems[0]);
                    int g = Integer.valueOf(colorItems[1]);
                    int b = Integer.valueOf(colorItems[2]);
                    view.setBackgroundColor(Color.argb(100, r, g, b));
                }
            }

            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(R.layout.list_item_shop_category, parent, false);
            }
        };
    }

    public void setTwoPaneLayout(boolean twoPaneLayout) {
        mTwoPaneLayout = twoPaneLayout;
    }

    /**
     * Cache of the children views for a category list item.
     */
    private static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView nameView;

        public ViewHolder(View view) {
            super(view);

            nameView = (TextView) view.findViewById(R.id.list_item_category_name);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        mCursorAdapter.getCursor().moveToPosition(position);
        mCursorAdapter.bindView(holder.itemView, mContext, mCursorAdapter.getCursor());
    }

    @Override
    public int getItemCount() {
        return mCursorAdapter.getCount();
    }

    public Cursor swapCursor(Cursor newCursor) {
        Cursor cursor = mCursorAdapter.swapCursor(newCursor);

        notifyDataSetChanged();

        return cursor;
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
        int id = cursor.getInt(ShopCategoriesFragment.COL_SHOP_CATEGORY_ID);
        ops.add(ContentProviderOperation.newUpdate(ShoppingContract.ShopCategoryEntry.CONTENT_URI)
                .withSelection(ShoppingContract.ShopCategoryEntry._ID + " = ?",
                        new String[]{Integer.toString(id)})
                .withValue(ShoppingContract.ShopCategoryEntry.COLUMN_SORTORDER, order)
                .build());
    }

    @Override
    public void onItemDismiss(int position) {
        Cursor cursor = mCursorAdapter.getCursor();
        cursor.moveToPosition(position);
        int id = cursor.getInt(ShopCategoriesFragment.COL_SHOP_CATEGORY_ID);

        mContext.getContentResolver().delete(
                ShoppingContract.ShopCategoryEntry.buildShopCategoryUri(id),
                null,
                null
        );

        notifyItemRemoved(position);
    }
}