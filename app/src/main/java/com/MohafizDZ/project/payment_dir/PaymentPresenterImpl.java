package com.MohafizDZ.project.payment_dir;

import android.content.Context;

import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.payment_dir.models.Models;
import com.MohafizDZ.project.payment_dir.strategies.ConcretePaymentStrategy;
import com.MohafizDZ.project.payment_dir.strategies.IPaymentStrategy;

public class PaymentPresenterImpl implements IPaymentPresenter.Presenter{
    private static final String TAG = PaymentPresenterImpl.class.getSimpleName();

    private final IPaymentPresenter.View view;
    private final Context context;
    private final IPaymentStrategy paymentStrategy;
    private final Models models;

    public PaymentPresenterImpl(IPaymentPresenter.View view, Context context, ConcretePaymentStrategy paymentStrategy) {
        this.view = view;
        this.context = context;
        this.models = new Models(context);
        paymentStrategy.setModels(models);
        this.paymentStrategy = paymentStrategy;
    }

    @Override
    public void onViewCreated() {
        paymentStrategy.onViewCreated();
        boolean isRefund = paymentStrategy.isRefund();
        if(isRefund){
            view.setToolbarTitle(getString(R.string.refund_label));
            view.setTotalPaymentLabel(getString(R.string.total_to_refund_));
            view.setPaymentLabel(getString(R.string.payment_amount_label_));
        }else{
            view.setPaymentLabel(getString(R.string.payment_amount_label_));
            view.setTotalPaymentLabel(getString(R.string.total_to_pay_));
            view.setToolbarTitle(getString(R.string.payment_label));
        }
        view.togglePaymentContainer(false);
        view.toggleAddButton(true);
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void requestAddPayment() {
        view.openPaymentDialog(paymentStrategy.getSwipeAmount(), null);
    }

    @Override
    public void requestEditPayment() {
        view.openPaymentDialog(paymentStrategy.getSwipeAmount(), Math.abs(paymentStrategy.getPaymentAmount()));
    }

    @Override
    public void onValidate() {
        if(!paymentStrategy.onPreValidatePayment()){
            return;
        }
        view.requestCurrentLocation(new IPaymentPresenter.LocationListener() {
            @Override
            public void onLocationChanged(double latitude, double longitude) {
                view.toggleLoading(false);
                paymentStrategy.validate(latitude, longitude);
            }

            @Override
            public void onStart() {
                view.toggleLoading(true);
            }

            @Override
            public void onFailed() {
                view.toggleLoading(false);
            }
        });
    }

    private String getString(int resId){
        return context.getString(resId);
    }
    @Override
    public void onAddPayment(String paymentAmount) {
        paymentStrategy.onAddPayment(paymentAmount);
    }
}
