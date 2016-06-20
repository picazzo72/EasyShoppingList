package com.dandersen.app.easyshoppinglist.ui;

/**
 * Created by Dan on 20-06-2016.
 * Interface for the Item Touch Helper.
 * The adapter doing the operations must implement these methods.
 */
public interface ItemTouchHelperAdapter {

    /**
     * Called every time an item has changed position.
     * Is mainly used to update the ui so the user can see, where the item is dropped.
     * @param fromPosition Position where the item was.
     * @param toPosition Position to where the item should be moved.
     */
    void onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);

    /**
     * Do the actual move here, where the user has released the item.
     * @param fromPosition Position where the item was.
     * @param toPosition Position to where the item should be moved.
     */
    void onDrop(int fromPosition, int toPosition);
}