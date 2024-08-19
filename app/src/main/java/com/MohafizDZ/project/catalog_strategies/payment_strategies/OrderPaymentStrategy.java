package com.MohafizDZ.project.catalog_strategies.payment_strategies;

import android.app.Activity;
import android.content.Context;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.catalog_dir.models.CartItem;
import com.MohafizDZ.project.catalog_dir.models.CartItemSingleton;
import com.MohafizDZ.project.payment_dir.IPaymentPresenter;
import com.MohafizDZ.project.payment_dir.models.Models;
import com.MohafizDZ.project.payment_dir.strategies.ConcretePaymentStrategy;

import java.util.ArrayList;
import java.util.List;

public class OrderPaymentStrategy extends ConcretePaymentStrategy {
    private static final String TAG = OrderPaymentStrategy.class.getSimpleName();
    protected CartItemSingleton cartItemSingleton;
    protected DataRow customerRow, distributorRow, tourRow, visitRow;
    protected float orderAmount;
    protected String validatedOrderId = null;

    public OrderPaymentStrategy(Context context, IPaymentPresenter.View view, DataRow currentUserRow) {
        super(context, view, currentUserRow);
        cartItemSingleton = CartItemSingleton.getInstance();
        orderAmount = amountSign() * cartItemSingleton.getTotalAmount();
    }

    public OrderPaymentStrategy(Context context, IPaymentPresenter.ValidateView validateView, DataRow currentUserRow){
        super(context, validateView, currentUserRow);
        cartItemSingleton = CartItemSingleton.getInstance();
        orderAmount = amountSign() * cartItemSingleton.getTotalAmount();
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
        return orderAmount;
    }

    @Override
    public float getTotalToPay() {
        return orderAmount + getBalance();
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
        boolean resultOk = validateSale(latitude, longitude);
        if(resultOk) {
            validateView.openOrderDetails(false, validatedOrderId);
        }else{
            validateView.showToast(getString(R.string.error_occurred));
        }
        return resultOk;
    }

    @Override
    public boolean isRefund() {
        return false;
    }

    protected int amountSign(){
        return isRefund()? -1 : 1;
    }

    private boolean validateSale(double latitude, double longitude){
        return Model.startTransaction(context, () -> {
            String validateDate = MyUtil.getCurrentDate();
            float remainingAmount = calculateActualBalance();
            String prefix = isRefund()? "back_order" : "sale";
            DataRow orderRow = models.visitOrderModel.createOrder(prefix, cartItemSingleton, latitude, longitude, visitRow, distributorRow, validateDate, orderAmount, paymentAmount, remainingAmount);
            String orderName = orderRow != null? orderRow.getString("name") : "-";
            DataRow paymentRow = models.paymentModel.createOrderPayment(orderName, paymentAmount, remainingAmount, visitRow, distributorRow, validateDate);
            DataRow actionRow = models.tourVisitActionModel.createOrderAction(isRefund(), visitRow, latitude, longitude, validateDate);
            if(paymentRow == null || orderRow == null || actionRow == null){
                return false;
            }
            {
                Values values = new Values();
                values.put("visit_order_id", orderRow.getString(Col.SERVER_ID));
                values.put("action_details_id", actionRow.getString(Col.SERVER_ID));
                models.paymentModel.update(paymentRow.getString(Col.SERVER_ID), values);
            }
            {
                Values values = new Values();
                values.put("payment_line", paymentRow.getString(Col.SERVER_ID));
                values.put("action_details_id", actionRow.getString(Col.SERVER_ID));
                models.visitOrderModel.update(orderRow.getString(Col.SERVER_ID), values);
            }
            {
                Values values = new Values();
                values.put("rel_model_name", models.visitOrderModel.getModelName());
                values.put("res_id", orderRow.getString(Col.SERVER_ID));
                models.tourVisitActionModel.update(actionRow.getString(Col.SERVER_ID), values);
            }
            {
                List<String> products = new ArrayList<>(cartItemSingleton.cartItems.keySet());
                models.visitOrderModel.insertRelArray(orderRow, "products", products);
                for(CartItem cartItem: cartItemSingleton.cartItems.values()){
                    float qty = amountSign() * cartItem.getQty();
                    float totalPrice = amountSign() * cartItem.getTotalPrice();
                    int rowId = models.visitOrderLineModel.createOrderLine(orderRow, cartItem.getProductId(), cartItem.getProductName(),
                            qty, cartItem.getUnitPrice(), totalPrice);
                    if(rowId <= 0){
                        return false;
                    }
                }
            }
            models.companyCustomerModel.updateBalance(customerRow.getString(Col.SERVER_ID), remainingAmount);
            this.validatedOrderId = orderRow.getString(Col.SERVER_ID);
            return true;
        });
    }
}
