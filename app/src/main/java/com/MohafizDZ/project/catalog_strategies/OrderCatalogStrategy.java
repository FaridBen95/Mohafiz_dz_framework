package com.MohafizDZ.project.catalog_strategies;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.catalog_dir.catalog_presenters_dir.ICatalogPresenter;
import com.MohafizDZ.project.catalog_dir.models.CartItemSingleton;
import com.MohafizDZ.project.catalog_dir.models.ProductRow;
import com.MohafizDZ.project.catalog_dir.quantity_dialog_dir.IQtyDialogPresenter;
import com.MohafizDZ.project.catalog_dir.quantity_dialog_dir.QtyDialog;
import com.MohafizDZ.project.catalog_dir.strategies_dir.ConcreteCatalogStrategy;
import com.MohafizDZ.project.catalog_strategies.cart_order_strategies.CartOrderStrategy;
import com.MohafizDZ.project.models.CompanyModel;

import java.util.List;

public class OrderCatalogStrategy extends ConcreteCatalogStrategy {
    protected final CartItemSingleton cartItemSingleton;
    protected final String currencyCode;

    @Override
    public boolean canShowAvailability() {
        return useAvailability();
    }

    public OrderCatalogStrategy(Context context, ICatalogPresenter.View view, DataRow currentUserRow) {
        super(context, view, currentUserRow);
        this.cartItemSingleton = CartItemSingleton.getInstance();
        cartItemSingleton.setCanShowQty(true);
        currencyCode = CompanyModel.getCompanyCurrency(context);
    }

    @Override
    public boolean canEdit() {
        return false;
    }

    @Override
    public boolean canShowValidateButton() {
        boolean visible = cartItemSingleton.hasCartItems();
        view.refreshValidateButtonBadge(visible? cartItemSingleton.cartItems.size() : 0);
        view.setTotalAmount(cartItemSingleton.getTotalAmount() + " " + currencyCode);
        return visible;
    }

    @Override
    public List<DataRow> getProductRows(String selection, String[] args, String sortBy) {
        return models.companyProductModel.getProducts(tourRow.getString(Col.SERVER_ID), selection, args, sortBy);
    }

    @Override
    public void onItemClick(int position, ProductRow productRow) {
        Float availability = null;
        if(canShowAvailability()){
            availability = productRow.getFloat("stock_qty");
        }
        if(availability != null && productRow.getQty() + 1 > availability){
            view.showToast(getString(R.string.quantity_exceed_msg));
        }else {
            productRow.incrementQty(1);
            view.refreshItem(position);
            refreshView();
        }
    }

    private void refreshView(){
        view.toggleValidateContainer(canShowValidateButton());
        view.toggleEmptyMenuItem(canEmpty());
    }

    @Override
    public void onItemLongClick(int position, ProductRow productRow) {
        float lastQty = productRow.getQty();
        IQtyDialogPresenter.Dialog dialog = new QtyDialog(new IQtyDialogPresenter.DialogListener() {
            @Override
            public void onPositiveClicked(ProductRow productRow, float qty) {
                productRow.setQty(qty);
                presenter.refreshLine(position);
            }

            @Override
            public void onNeutralClicked(ProductRow productRow, float qty) {
                productRow.setQty(0);
                presenter.refreshLine(position);
            }

            @Override
            public void onNegativeClicked(ProductRow productRow, float qty) {
                productRow.setQty(lastQty);
            }
        }, productRow, lastQty, IQtyDialogPresenter.PresenterType.saleQty);
        dialog.setUnitPrice(productRow.getFloat("price"));
        if(canShowAvailability()) {
            dialog.setAvailability(productRow.getFloat("stock_qty"));
        }
        view.showQtyDialog(dialog);
    }

    @Override
    public boolean canEmpty() {
        return cartItemSingleton.hasCartItems();
    }

    @Override
    public void onEmptyClicked() {
        super.onEmptyClicked();
        cartItemSingleton.clearItems();
        refreshView();
        view.onListUpdated();
    }

    @Override
    public void onValidate() {
        super.onValidate();
        view.openCartOrder(CartOrderStrategy.class.getName());
    }

    @Override
    public void onViewCreated() {
        super.onViewCreated();
        if(customerRow != null) {
            view.setCustomerName(customerRow.getString("name"));
        }
    }

    @Override
    public String getTitle(){
        return getString(R.string.sale_label);
    }
}
