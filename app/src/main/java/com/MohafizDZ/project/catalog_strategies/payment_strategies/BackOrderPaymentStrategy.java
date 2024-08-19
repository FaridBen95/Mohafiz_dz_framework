package com.MohafizDZ.project.catalog_strategies.payment_strategies;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.project.payment_dir.IPaymentPresenter;

public class BackOrderPaymentStrategy extends OrderPaymentStrategy {
    public BackOrderPaymentStrategy(Context context, IPaymentPresenter.View view, DataRow currentUserRow) {
        super(context, view, currentUserRow);
    }

    public BackOrderPaymentStrategy(Context context, IPaymentPresenter.ValidateView validateView, DataRow currentUserRow){
        super(context, validateView, currentUserRow);
    }

    @Override
    public boolean isRefund() {
        return true;
    }
}
