package com.MohafizDZ.project.payment_dir.strategies;

import android.app.Activity;
import android.content.Context;

import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.catalog_dir.models.CartItemSingleton;
import com.MohafizDZ.project.models.CompanyModel;
import com.MohafizDZ.project.payment_dir.IPaymentPresenter;
import com.MohafizDZ.project.payment_dir.models.Models;

public abstract class ConcretePaymentStrategy implements IPaymentStrategy{
    protected final Context context;
    protected final IPaymentPresenter.View view;
    protected final DataRow currentUserRow;
    private final String currencyCode;
    protected final IPaymentPresenter.ValidateView validateView;
    protected Models models;
    protected float paymentAmount = 0.0f;
    public abstract boolean onValidate(double latitude, double longitude);

    public ConcretePaymentStrategy(Context context, IPaymentPresenter.View view, DataRow currentUserRow) {
        this.context = context;
        this.view = view;
        validateView = view;
        this.currentUserRow = currentUserRow;
        currencyCode = CompanyModel.getCompanyCurrency(context);
    }

    public ConcretePaymentStrategy(Context context, IPaymentPresenter.ValidateView view, DataRow currentUserRow) {
        this.context = context;
        this.view = null;
        this.validateView = view;
        this.currentUserRow = currentUserRow;
        currencyCode = CompanyModel.getCompanyCurrency(context);
    }

    public void setModels(Models models){
        this.models = models;
    }

    @Override
    public void onViewCreated() {
        if(view != null) {
            view.setName(getName());
            view.setBalanceLimit(getPrice(getBalanceLimit()));
            view.setBalance(getPrice(getBalance()));
            view.setOrderAmount(getPrice(getOrderAmount()));
            view.setTotalToPay(getPrice(getTotalToPay()));
        }
    }

    @Override
    public void onRefresh() {
        if(view != null) {
            view.toggleAddButton(paymentAmount == 0);
            view.togglePaymentContainer(paymentAmount != 0);
            view.setPaymentAmount(getPrice(paymentAmount));
            view.setActualBalance(getPrice(calculateActualBalance()));
        }
    }

    protected String getString(int resId){
        return context.getString(resId);
    }

    protected String getPrice(float price){
        return price + " " + currencyCode;
    }

    @Override
    public boolean onPreValidatePayment() {
        final float balanceLimit = getBalanceLimit();
        final float actualBalance = calculateActualBalance();
        if(actualBalance > balanceLimit){
            view.showSimpleDialog(getString(R.string.balance_exceeded_title), getString(R.string.balance_exceeded_msg));
            return false;
        }
        return true;
    }

    @Override
    public void validate(double latitude, double longitude) {
        if(onValidate(latitude, longitude)){
            CartItemSingleton.getInstance().clearItems();
            validateView.goBack(Activity.RESULT_OK);
        }
    }

    @Override
    public float getSwipeAmount() {
        float totalToPay = getTotalToPay();
        boolean isRefundAllowed = totalToPay < 0;
        boolean isPaymentAllowed = totalToPay >= 0;
        return (isRefund()? isRefundAllowed : isPaymentAllowed)? Math.abs(totalToPay) : 0.0f;
    }

    public static class ValidateHelper{
        private final ConcretePaymentStrategy concretePaymentStrategy;

        private ValidateHelper(ConcretePaymentStrategy concretePaymentStrategy) {
            this.concretePaymentStrategy = concretePaymentStrategy;
        }

        public static ValidateHelper getInstance(ConcretePaymentStrategy concretePaymentStrategy, Models models, String customerId){
            ValidateHelper validateHelper = new ValidateHelper(concretePaymentStrategy);
            validateHelper.concretePaymentStrategy.setModels(models);
            validateHelper.concretePaymentStrategy.setCustomerId(customerId);
            return validateHelper;
        }

        public ValidateHelper setPayment(float payment){
            concretePaymentStrategy.onAddPayment(payment + "");
            return this;
        }

        public void validate(double latitude, double longitude){
            concretePaymentStrategy.onValidate(latitude, longitude);
        }
    }
}
