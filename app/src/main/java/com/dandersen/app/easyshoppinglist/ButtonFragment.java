package com.dandersen.app.easyshoppinglist;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dandersen.app.easyshoppinglist.data.SelectedViewEnum;
import com.dandersen.app.easyshoppinglist.prefs.Settings;

/**
 * Created by Dan on 27-05-2016.
 * Button fragment for the main button bar in the App.
 */
public class ButtonFragment extends Fragment {

    private final String LOG_TAG = ButtonFragment.class.getSimpleName();

    private boolean mTwoPaneLayout = false;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * ButtonFragmentCallback for when a button has been selected.
         */
        void onCurrentList();
        void onShoppingList();
        void onCategory();
        void onProduct();
        void onShop();
        void onCategoryProduct();
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

        setSelectedView(Settings.getInstance().getSelectedView());

        if (getView() == null) return;

        ViewHolder viewHolder = (ViewHolder) getView().getTag();

        switch (Settings.getInstance().getSelectedView()) {
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
            case CategoryProduct:
                ((Callback)getActivity()).onCategoryProduct();
                break;
            default:
                throw new UnsupportedOperationException("Unknown button position: " + Settings.getInstance().getSelectedView());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_buttons, container, false);

        // set view holder for easy access to the view elements
        ViewHolder viewHolder = new ViewHolder(rootView);
        rootView.setTag(viewHolder);

        // Create on click listeners for the buttons
        viewHolder.currentListGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedView(SelectedViewEnum.CurrentList);
                ((Callback)getActivity()).onCurrentList();
            }
        });
        viewHolder.shoppingListGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedView(SelectedViewEnum.ShoppingList);
                ((Callback)getActivity()).onShoppingList();
            }
        });
        viewHolder.categoryGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedView(SelectedViewEnum.Category);
                ((Callback)getActivity()).onCategory();
            }
        });
        viewHolder.productGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedView(SelectedViewEnum.Product);
                ((Callback)getActivity()).onProduct();
            }
        });
        viewHolder.shopGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedView(SelectedViewEnum.Shop);
                ((Callback)getActivity()).onShop();
            }
        });

        return rootView;
    }

    private void setSelectedView(SelectedViewEnum pos) {
        if (getView() == null) return;

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
            case CategoryProduct:
                viewHolder.categoryIcon.setImageResource(R.drawable.ic_layers_red_36dp);
                break;
            default:
                throw new UnsupportedOperationException("Unknown view position: " + pos);
        }

        Settings.getInstance().setSelectedView(pos);
    }

    private void restoreButtons() {
        if (getView() == null) return;

        ViewHolder viewHolder = (ViewHolder) getView().getTag();
        viewHolder.currentListIcon.setImageResource(R.drawable.ic_shopping_cart_black_36dp);
        viewHolder.shoppingListIcon.setImageResource(R.drawable.ic_list_black_36dp);
        viewHolder.categoryIcon.setImageResource(R.drawable.ic_layers_black_36dp);
        viewHolder.productIcon.setImageResource(R.drawable.ic_shop_black_36dp);
        viewHolder.shopIcon.setImageResource(R.drawable.ic_store_black_36dp);
    }

}
