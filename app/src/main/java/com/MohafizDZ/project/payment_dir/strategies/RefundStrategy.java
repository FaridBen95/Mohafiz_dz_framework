package com.MohafizDZ.project.payment_dir.strategies;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.project.payment_dir.IPaymentPresenter;

public class RefundStrategy extends PaymentStrategy{
    private static final String TAG = RefundStrategy.class.getSimpleName();
    public RefundStrategy(Context context, IPaymentPresenter.View view, DataRow currentUserRow) {
        super(context, view, currentUserRow);
    }

    public RefundStrategy(Context context, IPaymentPresenter.ValidateView view, DataRow currentUserRow) {
        super(context, view, currentUserRow);
    }

    @Override
    public boolean isRefund() {
        return true;
    }
}
