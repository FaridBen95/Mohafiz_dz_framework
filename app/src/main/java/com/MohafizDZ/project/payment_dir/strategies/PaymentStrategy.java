package com.MohafizDZ.project.payment_dir.strategies;

import android.app.Activity;
import android.content.Context;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.payment_dir.IPaymentPresenter;
import com.MohafizDZ.project.payment_dir.models.Models;

public class PaymentStrategy extends ConcretePaymentStrategy{
    private static final String TAG = PaymentStrategy.class.getSimpleName();
    protected DataRow customerRow, distributorRow, tourRow, visitRow;
    private String validatedPaymentId;

    public PaymentStrategy(Context context, IPaymentPresenter.View view, DataRow currentUserRow) {
        super(context, view, currentUserRow);
    }

    public PaymentStrategy(Context context, IPaymentPresenter.ValidateView view, DataRow currentUserRow) {
        super(context, view, currentUserRow);
    }

    @Override
    public void setModels(Models models) {
        super.setModels(models);
        distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        tourRow = models.tourModel.getCurrentTour(distributorRow);
    }
    @Override
    public void setCustomerId(String customerId) {
        if(customerId != null){
            this.customerRow = models.companyCustomerModel.browse(customerId);
            visitRow = models.tourVisitModel.getCurrentVisit(tourRow.getString(Col.SERVER_ID), customerId);
        }
    }

    @Override
    public String getName() {
        return customerRow.getString("name");
    }

    @Override
    public float getBalanceLimit() {
        return customerRow.getFloat("balance_limit");
    }

    @Override
    public float getBalance() {
        return customerRow.getFloat("balance");
    }

    @Override
    public float getOrderAmount() {
        return 0;
    }

    @Override
    public float getTotalToPay() {
        return getBalance();
    }

    @Override
    public void onAddPayment(String paymentAmount) {
        try{
            this.paymentAmount = amountSign() * Float.valueOf(paymentAmount);
        }catch (Exception ignored){
            this.paymentAmount = 0;
        }
        onRefresh();
    }

    protected int amountSign(){
        return isRefund()? -1 : 1;
    }

    @Override
    public float calculateActualBalance() {
        return getTotalToPay() - paymentAmount;
    }

    @Override
    public float getPaymentAmount() {
        return paymentAmount;
    }

    @Override
    public boolean onValidate(double latitude, double longitude) {
        boolean resultOk = validatePayment(latitude, longitude);
        if(resultOk) {
            validateView.openOrderDetails(true, validatedPaymentId);
        }else{
            validateView.showToast(getString(R.string.error_occurred));
        }
        return resultOk;
    }

    private boolean validatePayment(double latitude, double longitude){
        return Model.startTransaction(context, () -> {
            String validateDate = MyUtil.getCurrentDate();
            float remainingAmount = calculateActualBalance();
            DataRow paymentRow = models.paymentModel.createPayment(isRefund(), paymentAmount, remainingAmount, visitRow, distributorRow, validateDate);
            DataRow actionRow = models.tourVisitActionModel.createPaymentAction(isRefund(), visitRow, latitude, longitude, validateDate);
            if(paymentRow == null || actionRow == null){
                return false;
            }
            {
                Values values = new Values();
                values.put("action_details_id", actionRow.getString(Col.SERVER_ID));
                models.paymentModel.update(paymentRow.getString(Col.SERVER_ID), values);
            }
            {
                Values values = new Values();
                values.put("rel_model_name", models.paymentModel.getModelName());
                values.put("res_id", paymentRow.getString(Col.SERVER_ID));
                models.tourVisitActionModel.update(actionRow.getString(Col.SERVER_ID), values);
            }
            models.companyCustomerModel.updateBalance(customerRow.getString(Col.SERVER_ID), remainingAmount);
            this.validatedPaymentId = paymentRow.getString(Col.SERVER_ID);
            return true;
        });
    }

    @Override
    public boolean isRefund() {
        return false;
    }
}
