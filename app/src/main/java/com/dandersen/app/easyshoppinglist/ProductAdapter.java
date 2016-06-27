package com.dandersen.app.easyshoppinglist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dandersen.app.easyshoppinglist.data.ActiveListUtil;
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
        super(context, c, flags, R.id.list_item_shopping_cart,
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
        public final ImageView shoppingCartView;

        public ViewHolder(View view) {
            productNameView     = (TextView) view.findViewById(R.id.list_item_product_name);
            categoryNameView    = (TextView) view.findViewById(R.id.list_item_category_name);
            shoppingCartView    = (ImageView) view.findViewById(R.id.list_item_shopping_cart);
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

        // Category name
        String categoryName = cursor.getString(ProductFragment.COL_CATEGORY_NAME);
        viewHolder.categoryNameView.setText(categoryName);

        // Shopping cart icon
        Long productId = cursor.getLong(ProductFragment.COL_PRODUCT_ID);
        viewHolder.shoppingCartView.setTag(R.id.product_id_tag, productId);

        // Shopping list product id
        if (cursor.isNull(ProductFragment.COL_SHOPPING_LIST_PRODUCT_ID)) {
            setProductAsOffActiveList(viewHolder.shoppingCartView);
            viewHolder.shoppingCartView.setTag(R.id.shopping_list_product_id_tag, null);
        }
        else {
            setProductAsOnActiveList(viewHolder.shoppingCartView);
            Long shoppingListProductId = cursor.getLong(ProductFragment.COL_SHOPPING_LIST_PRODUCT_ID);
            viewHolder.shoppingCartView.setTag(R.id.shopping_list_product_id_tag, shoppingListProductId);
        }

        setupAddToActiveListClickListener(viewHolder);
    }

    private void setupAddToActiveListClickListener(ViewHolder viewHolder) {
        viewHolder.shoppingCartView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView shoppingCartView = (ImageView) view;
                if ((int)shoppingCartView.getTag(R.id.drawable_id_tag) == R.drawable.ic_shopping_cart_red_24dp) {
                    Long shoppingListProductId = (Long) shoppingCartView.getTag(R.id.shopping_list_product_id_tag);

                    // Delete product from active list
                    ActiveListUtil.removeProduct(mContext, shoppingListProductId);

                    setProductAsOffActiveList(shoppingCartView);
                }
                else {
                    Long productId = (Long) view.getTag(R.id.product_id_tag);
                    Long activeShoppingListId = ActiveListUtil.getActiveShoppingListId(mContext);
                    if (activeShoppingListId == null) return;

                    // Determine shop id
                    Long shopId = ShoppingContract.ShopEntry.DEFAULT_STORE_ID;
                    Pair<Long, Long> shopIdPair = ActiveListUtil.getLastUsedShopId(mContext);
                    if (shopIdPair != null) {
                        shopId = shopIdPair.first;
                    }

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(ShoppingContract.ShoppingListProductEntry.COLUMN_PRODUCT_ID, productId);
                    contentValues.put(ShoppingContract.ShoppingListProductEntry.COLUMN_SHOPPING_LIST_ID, activeShoppingListId);
                    contentValues.put(ShoppingContract.ShoppingListProductEntry.COLUMN_SHOP_ID, shopId);

                    if (shopIdPair == null || shopIdPair.second == null) {
                        // Insert shopping list product entry
                        Uri insertUri = mContext.getContentResolver().insert(
                                ShoppingContract.ShoppingListProductEntry.CONTENT_URI, contentValues);
                        String shoppingListProductId = ShoppingContract.ShoppingListProductEntry.getShoppingListProductIdFromUri(insertUri);
                        view.setTag(R.id.shopping_list_product_id_tag, Long.valueOf(shoppingListProductId));
                    }
                    else {
                        // Update shop entry with the product
                        mContext.getContentResolver().update(
                                ShoppingContract.ShoppingListProductEntry.CONTENT_URI,
                                contentValues,
                                ShoppingContract.ShoppingListProductEntry.COLUMN_SHOP_ID + "= ?",
                                new String[] { Long.toString(shopId)});
                        view.setTag(R.id.shopping_list_product_id_tag, shopIdPair.second);
                    }

                    setProductAsOnActiveList(shoppingCartView);
                }
            }
        });
    }

    private void setProductAsOffActiveList(ImageView shoppingCartView) {
        shoppingCartView.setImageResource(R.drawable.ic_shopping_cart_black_24dp);
        shoppingCartView.setTag(R.id.drawable_id_tag, R.drawable.ic_shopping_cart_black_24dp);
    }

    private void setProductAsOnActiveList(ImageView shoppingCartView) {
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

    @Override
    protected void onDeleteEntries(StringBuilder idSelection, String[] idSelectionArgs) {
        mContext.getContentResolver().delete(
                ShoppingContract.ProductEntry.CONTENT_URI,
                ShoppingContract.ProductEntry._ID + " IN ( " + idSelection.toString() + " )",
                idSelectionArgs
        );
    }
}