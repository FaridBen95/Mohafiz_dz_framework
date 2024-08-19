package com.MohafizDZ.project.payment_dir.strategies;

public interface IPaymentStrategy {
    //inorder to do an action i need to extend from concretePayment strategy and then
    //set models, set customerId, finaly call on validate
    void setCustomerId(String customerId);

    void onViewCreated();
    void onRefresh();

    String getName();
    float getBalanceLimit();
    float getBalance();
    float getOrderAmount();
    float getTotalToPay();

    void onAddPayment(String paymentAmount);
    float calculateActualBalance();

    float getPaymentAmount();

    void validate(double latitude, double longitude);

    boolean isRefund();

    boolean onPreValidatePayment();

    float getSwipeAmount();
}
