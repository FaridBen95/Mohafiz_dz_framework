package com.MohafizDZ.project.catalog_strategies;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.catalog_dir.catalog_presenters_dir.ICatalogPresenter;
import com.MohafizDZ.project.catalog_dir.models.ProductRow;
import com.MohafizDZ.project.catalog_dir.quantity_dialog_dir.IQtyDialogPresenter;
import com.MohafizDZ.project.catalog_dir.quantity_dialog_dir.QtyDialog;
import com.MohafizDZ.project.catalog_strategies.cart_order_strategies.BackOrderCartOrderStrategy;

public class BackOrderCatalogStrategy extends OrderCatalogStrategy {
    public BackOrderCatalogStrategy(Context context, ICatalogPresenter.View view, DataRow currentUserRow) {
        super(context, view, currentUserRow);
    }

    @Override
    public void onValidate() {
        view.openCartOrder(BackOrderCartOrderStrategy.class.getName());
    }

    @Override
    public String getTitle(){
        return getString(R.string.back_order_label);
    }

    @Override
    public boolean canShowAvailability() {
        return false;
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
        }, productRow, lastQty, IQtyDialogPresenter.PresenterType.backOrderQty);
        dialog.setUnitPrice(productRow.getFloat("price"));
        view.showQtyDialog(dialog);
    }

}
