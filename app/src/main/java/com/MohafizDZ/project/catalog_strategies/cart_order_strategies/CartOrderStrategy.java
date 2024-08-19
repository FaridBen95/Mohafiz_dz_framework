package com.MohafizDZ.project.catalog_strategies.cart_order_strategies;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.catalog_dir.cart_order_dir.ICartOrderPresenter;
import com.MohafizDZ.project.catalog_dir.cart_order_dir.strategies_dir.ConcreteCartOrderStrategy;
import com.MohafizDZ.project.catalog_dir.models.CartItem;
import com.MohafizDZ.project.catalog_dir.models.CartItemSingleton;
import com.MohafizDZ.project.catalog_dir.models.ProductRow;
import com.MohafizDZ.project.catalog_strategies.payment_strategies.OrderPaymentStrategy;
import com.MohafizDZ.project.models.CompanyModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CartOrderStrategy extends ConcreteCartOrderStrategy {
    private final CartItemSingleton cartItemSingleton;
    private final String currencyCode;

    public CartOrderStrategy(Context context, ICartOrderPresenter.View view, DataRow currentUserRow) {
        super(context, view, currentUserRow);
        this.cartItemSingleton = CartItemSingleton.getInstance();
        cartItemSingleton.setCanShowQty(true);
        currencyCode = CompanyModel.getCompanyCurrency(context);
    }

    @Override
    public List<ProductRow> setProductRows() {
        Map<String, DataRow> products = models.companyProductModel.getMap(Col.SERVER_ID);
        List<ProductRow> rows = new ArrayList<>();
        for(CartItem cartItem : cartItemSingleton.cartItems.values()){
            ProductRow productRow = new ProductRow();
            productRow.putAll(products.get(cartItem.getProductId()));
            rows.add(productRow);
        }
        return rows;
    }

    @Override
    public boolean canShowValidateButton() {
        boolean visible = cartItemSingleton.hasCartItems();
        view.refreshValidateButtonBadge(visible? cartItemSingleton.cartItems.size() : 0);
        view.setTotalAmount(cartItemSingleton.getTotalAmount() + " " + currencyCode);
        return visible;
    }

    @Override
    public void onViewCreated() {
        view.setCustomerName(customerRow.getString("name"));
    }

    @Override
    public void onValidate() {
        view.openPaymentActivity(OrderPaymentStrategy.class.getName());
    }

    @Override
    public String getTitle() {
        return getString(R.string.sale_label);
    }
}
