package com.MohafizDZ.project.catalog_dir.quantity_dialog_dir;

import android.content.Context;

import com.MohafizDZ.project.catalog_dir.models.ProductRow;

public class SaleQtyDialogPresenterImpl extends SelectQtyDialogPresenterImpl{
    private static final String TAG = SaleQtyDialogPresenterImpl.class.getSimpleName();
    private final boolean isBackOrder;
    public SaleQtyDialogPresenterImpl(IQtyDialogPresenter.View view, Context context, ProductRow productRow, float qty, boolean isBackOrder) {
        super(view, context, productRow, qty);
        this.isBackOrder = isBackOrder;
    }

    @Override
    public void onViewCreated() {
        super.onViewCreated();
        view.toggleAvailability(!isBackOrder);
    }
}
