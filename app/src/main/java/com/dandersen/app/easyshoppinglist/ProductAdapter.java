package com.dandersen.app.easyshoppinglist;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dandersen.app.easyshoppinglist.data.ShoppingContract;
import com.dandersen.app.easyshoppinglist.ui.ActionModeCursorAdapter;

/**
 * Created by Dan on 28-05-2016.
 * {@link ProductAdapter} exposes a list of products
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ProductAdapter extends ActionModeCursorAdapter {

    private static boolean mTwoPaneLayout = true;

    public ProductAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags, R.id.list_item_product_shopping_cart,
                R.drawable.ic_shopping_cart_black_24dp);
    }

    public void setTwoPaneLayout(boolean twoPaneLayout) {
        mTwoPaneLayout = twoPaneLayout;
    }

    /**
     * Cache of the children views for a category list item.
     */
    private static class ViewHolder {
        public final TextView productNameView;
        public final TextView categoryNameView;

        public ViewHolder(View view) {
            productNameView     = (TextView) view.findViewById(R.id.list_item_product_name);
            categoryNameView    = (TextView) view.findViewById(R.id.list_item_category_name);
        }
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = R.layout.list_item_product;

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

        // Product name
        String productName = cursor.getString(ProductFragment.COL_PRODUCT_NAME);
        viewHolder.productNameView.setText(productName);

        String categoryName = cursor.getString(ProductFragment.COL_CATEGORY_NAME);
        viewHolder.categoryNameView.setText(categoryName);
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

    @Override
    protected void onDeleteEntries(StringBuilder idSelection, String[] idSelectionArgs) {
        mContext.getContentResolver().delete(
                ShoppingContract.ProductEntry.CONTENT_URI,
                ShoppingContract.ProductEntry._ID + " IN ( " + idSelection.toString() + " )",
                idSelectionArgs
        );
    }
}