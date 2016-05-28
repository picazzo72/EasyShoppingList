package com.dandersen.app.easyshoppinglist;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.dandersen.app.easyshoppinglist.data.ShoppingContract;

/**
 * Created by Dan on 27-05-2016.
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
        public void onCurrentListBtn(View view);
        public void onShoppingListBtn(View view);
        public void onCategoryBtn(View view);
        public void onProductBtn(View view);
        public void onShopBtn(View view);
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

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.forecastfragment, menu);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        if (id == R.id.action_refresh) {
//            updateWeather();
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    /**
     * Cache of the children views for a category list item.
     */
    private static class ViewHolder {
        public final LinearLayout currentListGroup;
        public final LinearLayout shoppingListGroup;
        public final LinearLayout categoryGroup;
        public final LinearLayout productGroup;
        public final LinearLayout shopGroup;

        public ViewHolder(View view) {
            currentListGroup     = (LinearLayout) view.findViewById(R.id.current_list_group);
            shoppingListGroup    = (LinearLayout) view.findViewById(R.id.shopping_list_group);
            categoryGroup        = (LinearLayout) view.findViewById(R.id.category_group);
            productGroup         = (LinearLayout) view.findViewById(R.id.product_group);
            shopGroup            = (LinearLayout) view.findViewById(R.id.shop_group);
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
                ((Callback)getActivity()).onCurrentListBtn(view);
            }
        });
        viewHolder.shoppingListGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Callback)getActivity()).onShoppingListBtn(view);
            }
        });
        viewHolder.categoryGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Callback)getActivity()).onCategoryBtn(view);
            }
        });
        viewHolder.productGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Callback)getActivity()).onProductBtn(view);
            }
        });
        viewHolder.shopGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Callback)getActivity()).onShopBtn(view);
            }
        });

        return rootView;
    }

}
