package com.dandersen.app.easyshoppinglist;

/**
 * Created by Dan on 27-05-2016.
 */

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link CategoryAdapter} exposes a list of product categories
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class CategoryAdapter extends CursorAdapter {

    private static boolean mTwoPaneLayout = true;

    public CategoryAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
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
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = R.layout.list_item_category;

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

        // Category name
        String name = cursor.getString(CategoryFragment.COL_CATEGORY_NAME);
        viewHolder.nameView.setText(name);
    }
}