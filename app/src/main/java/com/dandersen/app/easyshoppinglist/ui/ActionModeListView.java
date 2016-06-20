package com.dandersen.app.easyshoppinglist.ui;

import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dandersen.app.easyshoppinglist.R;

import java.util.ArrayList;

/**
 * Created by Dan on 19-06-2016.
 * Action mode list view setup.
 * Long click is used to activate contextual actions.
 * The procedure comes from:
 * https://techienotes.info/2015/09/13/how-to-select-multiple-items-in-android-listview/
 */
public class ActionModeListView {

    public interface CallBack {
        void onActionModeFinished(ArrayList<Integer> itemsToDelete);
    }

    /**
     * Long click is used to activate contextual actions
     * The procedure comes from:
     * https://techienotes.info/2015/09/13/how-to-select-multiple-items-in-android-listview/
     * @param listView The list view in question
     */
    public static void setupItemLongClickClistener(final ActionModeListView.CallBack callback,
                                                   final AppCompatActivity activity,
                                                   final ListView listView,
                                                   final ActionModeCursorAdapter adapter) {
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int index, long rowId) {
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

                // We set the item to be checked to activate the action mode immediately
                listView.setItemChecked(index, true);

                return true;
            }

        });

        // Capture ListView item click
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode,
                                                  int position, long id, boolean checked) {
                // Prints the count of selected Items in title
                mode.setTitle(listView.getCheckedItemCount() + " selected");

                // Toggle the state of item after every click on it
                adapter.checkSelection(position, checked);
            }

            /**
             * Called to report a user click on an action button.
             * @return true if this callback handled the event,
             *         false if the standard MenuItem invocation should continue.
             */
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.action_delete){
                    SparseBooleanArray selected = adapter.getSelectedIds();
                    short size = (short)selected.size();
                    ArrayList<Integer> itemsToDelete = new ArrayList<>();
                    for (byte I = 0; I<size; I++){
                        if (selected.valueAt(I)) {
                            int curItemNumber = selected.keyAt(I);
                            itemsToDelete.add(curItemNumber);
                        }
                    }
                    callback.onActionModeFinished(itemsToDelete);

                    // Close CAB (Contextual Action Bar)
                    mode.finish();
                    return true;
                }
                else if (item.getItemId() == R.id.action_select_all){
                    for ( int i=0; i < listView.getAdapter().getCount(); i++) {
                        listView.setItemChecked(i, true);
                    }
                }
                return false;
            }

            /**
             * Called when action mode is first created.
             * The menu supplied will be used to generate action buttons for the action mode.
             * @param mode ActionMode being created
             * @param menu Menu used to populate action buttons
             * @return true if the action mode should be created,
             *          false if entering this mode should be aborted.
             */
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Hide action bar
                if (activity != null && activity.getSupportActionBar() != null) {
                    activity.getSupportActionBar().hide();
                }

                // Show contextual action menu
                mode.getMenuInflater().inflate(R.menu.menu_list_context, menu);

                // Set action mode on adapter to change drawing mode
                adapter.setActionMode(true);

                return true;
            }

            /**
             * Called when an action mode is about to be exited and destroyed.
             */
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Show action bar
                if (activity != null && activity.getSupportActionBar() != null) {
                    activity.getSupportActionBar().show();
                }

                // Notify adapter that action mode is done
                adapter.setActionMode(false);
            }

            /**
             * Called to refresh an action mode's action menu whenever it is invalidated.
             * @return true if the menu or action mode was updated, false otherwise.
             */
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }
        });

    }

}
