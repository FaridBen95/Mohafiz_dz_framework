package com.MohafizDZ.project.catalog_dir.models;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartItemSingleton {
    public Map<String, CartItem> cartItems = new HashMap<>();
    private float totalAmount = 0.0f;
    private boolean canShowQty;
    private static CartItemSingleton cartItemSingleton;
    private CartItemSingleton(){

    }

    public static CartItemSingleton getInstance(){
        return getInstance(true);
    }

    public static CartItemSingleton getInstance(boolean canShowQty){
        if(cartItemSingleton == null){
            cartItemSingleton = new CartItemSingleton();
            cartItemSingleton.setCanShowQty(canShowQty);
        }
        return cartItemSingleton;
    }

    public void setItem(String productId, CartItem cartItem) {
        cartItems.put(productId, cartItem);
    }

    public boolean isCanShowQty() {
        return canShowQty;
    }

    public void setCanShowQty(boolean canShowQty) {
        this.canShowQty = canShowQty;
    }

    public void incrementQty(DataRow productRow, float addedQty) {
        String productId = productRow.getString(Col.SERVER_ID);
        float unitPrice = productRow.getFloat("price");
        final CartItem cartItem = cartItemSingleton.cartItems.getOrDefault(productId, new CartItem(productId, productRow.getString("name"), 0, unitPrice));
        totalAmount -= cartItem.getTotalPrice();
        cartItem.setQty(cartItem.getQty() + addedQty);
        totalAmount += cartItem.getTotalPrice();
        if(cartItem.getQty() > 0) {
            if (!cartItems.containsKey(productId)) {
                cartItems.put(productId, cartItem);
            }
        }else{
            if (cartItems.containsKey(productId)) {
                cartItems.remove(productId);
            }
        }
    }

    public void setQty(DataRow productRow, float qty) {
        String productId = productRow.getString(Col.SERVER_ID);
        float unitPrice = productRow.getFloat("price");
        final CartItem cartItem = cartItemSingleton.cartItems.getOrDefault(productId, new CartItem(productId, productRow.getString("name"), 0, unitPrice));
        totalAmount -= cartItem.getTotalPrice();
        cartItem.setQty(qty);
        totalAmount += cartItem.getTotalPrice();
        if(cartItem.getQty() > 0) {
            if (!cartItems.containsKey(productId)) {
                cartItems.put(productId, cartItem);
            }
        }else{
            if (cartItems.containsKey(productId)) {
                cartItems.remove(productId);
            }
        }
    }

    public boolean hasCartItems(){
        return cartItems.size() > 0;
    }

    public void clearItems() {
        totalAmount = 0.0f;
        cartItems.clear();
    }

    public float getTotalAmount() {
        return totalAmount;
    }

    public void addCartItems(Map<String, CartItem> cartItems) {
        this.cartItems.putAll(cartItems);
    }
}
