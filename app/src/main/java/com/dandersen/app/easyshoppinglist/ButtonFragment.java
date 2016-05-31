package com.dandersen.app.easyshoppinglist;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by Dan on 27-05-2016.
 */
public class ButtonFragment extends Fragment {

    private final String LOG_TAG = ButtonFragment.class.getSimpleName();

    // Tag for save instance bundle
    private static final String SELECTED_BUTTON = "selected_button";
    // Current button selection
    private int mSelectedButton = 0;

    private boolean mTwoPaneLayout = false;

    private final int CurrentList     = 0;
    private final int ShoppingList    = 1;
    private final int Category        = 2;
    private final int Product         = 3;
    private final int Shop            = 4;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * ButtonFragmentCallback for when a button has been selected.
         */
        void onCurrentListBtn(View view);
        void onShoppingListBtn(View view);
        void onCategoryBtn(View view);
        void onProductBtn(View view);
        void onShopBtn(View view);
    }

    public ButtonFragment() {
    }

//    public void setTwoPaneLayout(boolean twoPaneLayout) {
//        mTwoPaneLayout = twoPaneLayout;
//        if (mCategoryAdapter != null) {
//            mCategoryAdapter.setTwoPaneLayout(twoPaneLayout);
//        }
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The following line makes this fragment handle menu events
        setHasOptionsMenu(true);
    }

    /**
     * Cache of the children views for a category list item.
     */
    private static class ViewHolder {
        public final LinearLayout currentListGroup;
        public final LinearLayout shoppingListGroup;
        public final LinearLayout categoryGroup;
        public final LinearLayout productGroup;
        public final LinearLayout shopGroup;

        public final ImageView currentListIcon;
        public final ImageView shoppingListIcon;
        public final ImageView categoryIcon;
        public final ImageView productIcon;
        public final ImageView shopIcon;

        public ViewHolder(View view) {
            currentListGroup     = (LinearLayout) view.findViewById(R.id.current_list_group);
            shoppingListGroup    = (LinearLayout) view.findViewById(R.id.shopping_list_group);
            categoryGroup        = (LinearLayout) view.findViewById(R.id.category_group);
            productGroup         = (LinearLayout) view.findViewById(R.id.product_group);
            shopGroup            = (LinearLayout) view.findViewById(R.id.shop_group);

            currentListIcon      = (ImageView) view.findViewById(R.id.current_list_button);
            shoppingListIcon     = (ImageView) view.findViewById(R.id.shopping_list_button);
            categoryIcon         = (ImageView) view.findViewById(R.id.category_button);
            productIcon          = (ImageView) view.findViewById(R.id.product_button);
            shopIcon             = (ImageView) view.findViewById(R.id.shop_button);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setSelectedButton(mSelectedButton);

        ViewHolder viewHolder = (ViewHolder) getView().getTag();

        switch (mSelectedButton) {
            case CurrentList:
                viewHolder.currentListGroup.callOnClick();
                break;
            case ShoppingList:
                viewHolder.shoppingListGroup.callOnClick();
                break;
            case Category:
                viewHolder.categoryGroup.callOnClick();
                break;
            case Product:
                viewHolder.productGroup.callOnClick();
                break;
            case Shop:
                viewHolder.shopGroup.callOnClick();
                break;
            default:
                throw new UnsupportedOperationException("Unknown button position: " + mSelectedButton);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.v(LOG_TAG, "DSA LOG - onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_buttons, container, false);

        // set view holder for easy access to the view elements
        ViewHolder viewHolder = new ViewHolder(rootView);
        rootView.setTag(viewHolder);

        // Create on click listeners for the buttons
        viewHolder.currentListGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedButton(CurrentList);
                ((Callback)getActivity()).onCurrentListBtn(view);
            }
        });
        viewHolder.shoppingListGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedButton(ShoppingList);
                ((Callback)getActivity()).onShoppingListBtn(view);
            }
        });
        viewHolder.categoryGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedButton(Category);
                ((Callback)getActivity()).onCategoryBtn(view);
            }
        });
        viewHolder.productGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedButton(Product);
                ((Callback)getActivity()).onProductBtn(view);
            }
        });
        viewHolder.shopGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedButton(Shop);
                ((Callback)getActivity()).onShopBtn(view);
            }
        });

        // Load button selection from Shared Preferences
        mSelectedButton = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(
                getActivity()).getInt(SELECTED_BUTTON, CurrentList));

        return rootView;
    }

    private void setSelectedButton(int pos) {
        ViewHolder viewHolder = (ViewHolder) getView().getTag();

        restoreButtons();

        switch (pos) {
            case CurrentList:
                viewHolder.currentListIcon.setImageResource(R.drawable.ic_shopping_cart_red_36dp);
                break;
            case ShoppingList:
                viewHolder.shoppingListIcon.setImageResource(R.drawable.ic_list_red_36dp);
                break;
            case Category:
                viewHolder.categoryIcon.setImageResource(R.drawable.ic_layers_red_36dp);
                break;
            case Product:
                viewHolder.productIcon.setImageResource(R.drawable.ic_shop_red_36dp);
                break;
            case Shop:
                viewHolder.shopIcon.setImageResource(R.drawable.ic_store_red_36dp);
                break;
            default:
                throw new UnsupportedOperationException("Unknown button position: " + pos);
        }

        mSelectedButton = pos;

        // Save button selection in Shared Preferences
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.putInt(SELECTED_BUTTON, mSelectedButton);
        editor.commit();
    }

    private void restoreButtons() {
        ViewHolder viewHolder = (ViewHolder) getView().getTag();
        viewHolder.currentListIcon.setImageResource(R.drawable.ic_shopping_cart_black_36dp);
        viewHolder.shoppingListIcon.setImageResource(R.drawable.ic_list_black_36dp);
        viewHolder.categoryIcon.setImageResource(R.drawable.ic_layers_black_36dp);
        viewHolder.productIcon.setImageResource(R.drawable.ic_shop_black_36dp);
        viewHolder.shopIcon.setImageResource(R.drawable.ic_store_black_36dp);
    }

}
