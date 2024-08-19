package com.MohafizDZ.project.catalog_strategies.cart_order_strategies;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.catalog_dir.cart_order_dir.ICartOrderPresenter;
import com.MohafizDZ.project.catalog_strategies.payment_strategies.BackOrderPaymentStrategy;

public class BackOrderCartOrderStrategy extends CartOrderStrategy {

    public BackOrderCartOrderStrategy(Context context, ICartOrderPresenter.View view, DataRow currentUserRow) {
        super(context, view, currentUserRow);
    }

    @Override
    public void onValidate() {
        view.openPaymentActivity(BackOrderPaymentStrategy.class.getName());
    }

    @Override
    public String getTitle() {
        return getString(R.string.back_order_label);
    }
}
