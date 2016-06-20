package com.dandersen.app.easyshoppinglist.ui;

import android.content.Context;
import android.database.Cursor;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.dandersen.app.easyshoppinglist.R;

import java.util.ArrayList;

/**
 * Created by Dan on 19-06-2016.
 * Cursor adapter for list view action mode.
 */
public abstract class ActionModeCursorAdapter extends CursorAdapter {

    protected Context mContext;
    protected SparseBooleanArray mSelectedItemsIds;
    protected boolean mActionMode = false;
    private int mCheckBoxIconId;
    private int mCheckBoxDrawableId;

    public ActionModeCursorAdapter(Context context,
                                   Cursor c,
                                   int flags,
                                   int checkBoxIconId,
                                   int checkBoxDrawableId) {
        super(context, c, flags);

        mContext = context;

        mSelectedItemsIds = new SparseBooleanArray();

        mCheckBoxIconId = checkBoxIconId;
        mCheckBoxDrawableId = checkBoxDrawableId;
    }

    /**
     * Takes care of drawing the check box in action mode and another icon in
     * non-action mode.
     * @param view List item
     * @param context Activity
     * @param cursor Database cursor
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView checkBoxIcon = (ImageView) view.findViewById(mCheckBoxIconId);
        if (checkBoxIcon == null) return;

        if (mActionMode) {
            int position = cursor.getPosition();
            if (mSelectedItemsIds.get(position)) {
                // Item is checked
                checkBoxIcon.setImageResource(R.drawable.ic_check_box_black_24dp);
            } else {
                // Item is not checked
                checkBoxIcon.setImageResource(R.drawable.ic_check_box_outline_blank_black_24dp);
            }
        } else {
            checkBoxIcon.setImageResource(mCheckBoxDrawableId);
        }
    }

    /**
     * Check or uncheck a list view item
     * @param position Item position (row)
     * @param checked Checked or unchecked
     */
    protected void checkSelection(int position, boolean checked) {
        if (checked)
            mSelectedItemsIds.put(position, true);
        else
            mSelectedItemsIds.delete(position);
        notifyDataSetChanged();
    }

    /**
     * The selected list items.
     * @return Array of selected ids.
     */
    protected SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

    /**
     * Set adapter in action mode to facilitate that the adapter draws
     * the list items with a check box for selecting items
     * @param actionMode Action mode (true) or not (false)
     */
    protected void setActionMode(boolean actionMode) {
        mActionMode = actionMode;
        mSelectedItemsIds.clear();
    }

    public void deleteEntries(ArrayList<Integer> itemsToDelete, int idColumn) {
        final String itemSep = " , ";
        final String bind = "?";

        if (!itemsToDelete.isEmpty()) {
            ArrayList<String> bindIdList = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            for (Integer index : itemsToDelete) {
                Cursor cursor = (Cursor) getItem(index);
                int shopId = cursor.getInt(idColumn);
                if (shopId > 0) {
                    if (sb.length() > 0) {
                        sb.append(itemSep);
                    }
                    sb.append(bind);
                    bindIdList.add(Integer.toString(shopId));
                }
            }
            String[] bindIdStringArray = new String[bindIdList.size()];
            bindIdStringArray = bindIdList.toArray(bindIdStringArray);

            onDeleteEntries(sb, bindIdStringArray);

            mSelectedItemsIds.clear();
            notifyDataSetChanged();
        }
    }

    protected abstract void onDeleteEntries(StringBuilder idSelection, String[] idSelectionArgs);

}
