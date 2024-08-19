package com.MohafizDZ.project.catalog_dir.models;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;

public class ProductRow extends DataRow{

    public float getQty(){
        CartItem cartItem = getCartItem();
        return cartItem != null? cartItem.getQty() : 0.0f;
    }

    public CartItem getCartItem(){
        String productId = getString(Col.SERVER_ID);
        return CartItemSingleton.getInstance().cartItems.get(productId);
    }

    public void incrementQty(float addedQty){
        final CartItemSingleton cartItemSingleton = CartItemSingleton.getInstance();
        cartItemSingleton.incrementQty(this, addedQty);
    }

    public void setQty(float qty){
        final CartItemSingleton cartItemSingleton = CartItemSingleton.getInstance();
        cartItemSingleton.setQty(this, qty);
    }

    public boolean canShowQty(){
        final CartItemSingleton cartItemSingleton = CartItemSingleton.getInstance();
        return cartItemSingleton.isCanShowQty();
    }

    public float getTotalPrice() {
        CartItem cartItem = getCartItem();
        return cartItem != null? cartItem.getTotalPrice() : 0.0f;
    }
}
