package com.dandersen.app.easyshoppinglist;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dandersen.app.easyshoppinglist.data.ShoppingContract;
import com.dandersen.app.easyshoppinglist.ui.ActionModeCursorAdapter;

/**
 * Created by Dan on 16-06-2016.
 * {@link ShopCategoriesAdapter} exposes a list of shop categories
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ShopCategoriesAdapter extends ActionModeCursorAdapter {

    private static boolean mTwoPaneLayout = true;

    public ShopCategoriesAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags, R.id.list_item_category_reorder, R.drawable.ic_reorder_black_24dp);
    }

    public void setTwoPaneLayout(boolean twoPaneLayout) {
        mTwoPaneLayout = twoPaneLayout;
    }

    /**
     * Cache of the children views for a category list item.
     */
    private static class ViewHolder {
        public final TextView nameView;

        public ViewHolder(View view) {
            nameView = (TextView) view.findViewById(R.id.list_item_category_name);
        }
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_shop_category, parent, false);

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
    protected void onDeleteEntries(StringBuilder idSelection, String[] idSelectionArgs) {
        mContext.getContentResolver().delete(
                ShoppingContract.ShopCategoryEntry.CONTENT_URI,
                ShoppingContract.ShopCategoryEntry._ID + " IN ( " + idSelection.toString() + " )",
                idSelectionArgs
        );
    }
}