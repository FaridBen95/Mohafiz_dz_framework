package com.MohafizDZ.project.catalog_dir.models;

public class CartItem {
    private static final String TAG = CartItem.class.getSimpleName();

    private final String productId;
    private final String productName;
    private final float unitPrice;
    private float totalPrice;
    private float qty;

    public CartItem(String productId, String productName, float qty, float unitPrice) {
        this.productId = productId;
        this.qty = qty;
        this.unitPrice = unitPrice;
        this.productName = productName;
    }

    public CartItem setQty(float qty){
        this.qty = qty;
        this.totalPrice = unitPrice * qty;
        return this;
    }

    public float getQty() {
        return qty;
    }

    public float getUnitPrice() {
        return unitPrice;
    }

    public float getTotalPrice() {
        return totalPrice;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }
}
