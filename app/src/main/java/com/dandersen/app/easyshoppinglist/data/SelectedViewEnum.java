package com.dandersen.app.easyshoppinglist.data;

/**
 * Created by Dan on 05-06-2016.
 */
public enum SelectedViewEnum {
    CurrentList,
    ShoppingList,
    Category,
    Product,
    Shop,
    CategoryProduct;

    public static SelectedViewEnum fromInteger(int x) {
        switch (x) {
            case 0:     return CurrentList;
            case 1:     return ShoppingList;
            case 2:     return Category;
            case 3:     return Product;
            case 4:     return Shop;
            case 5:     return CategoryProduct;
            default:    throw new UnsupportedOperationException("fromInteger " + x);
        }
    }
}
