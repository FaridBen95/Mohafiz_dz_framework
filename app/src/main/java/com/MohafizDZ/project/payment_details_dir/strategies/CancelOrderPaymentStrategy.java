package com.MohafizDZ.project.payment_details_dir.strategies;

import static com.MohafizDZ.project.models.VisitOrderModel.ORDER_STATE_CANCEL;

import android.app.Activity;
import android.content.Context;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.catalog_dir.models.CartItemSingleton;
import com.MohafizDZ.project.catalog_strategies.payment_strategies.BackOrderPaymentStrategy;
import com.MohafizDZ.project.catalog_strategies.payment_strategies.OrderPaymentStrategy;
import com.MohafizDZ.project.payment_dir.IPaymentPresenter;

import java.util.List;
import java.util.Map;

public class CancelOrderPaymentStrategy extends OrderPaymentStrategy implements IPaymentPresenter.ValidateView{
    private final DataRow orderRow;
    private final List<DataRow> orderLines;
    private ValidateHelper backOrderalidateHelper;
    private double latitude, longitude;

    public CancelOrderPaymentStrategy(Context context, IPaymentPresenter.ValidateView view, DataRow currentUserRow, DataRow orderRow, List<DataRow> orderLines) {
        super(context, view, currentUserRow);
        this.orderRow = orderRow;
        this.orderLines = orderLines;
    }

    @Override
    public float getBalance() {
        return getTotalToPay() - getOrderAmount();
    }

    @Override
    public float getOrderAmount() {
        return orderRow.getFloat("total_amount");
    }

    @Override
    public float getTotalToPay() {
        return calculateActualBalance() + getPaymentAmount();
    }

    @Override
    public float calculateActualBalance() {
        return customerRow.getFloat("balance");
    }

    @Override
    public float getPaymentAmount() {
        return orderRow.getFloat("payment_amount");
    }

    @Override
    public boolean onValidate(double latitude, double longitude) {
        boolean resultOk = cancelOrder(latitude, longitude);
        if(resultOk) {
            validateView.openOrderDetails(false, validatedOrderId);
        }else{
            validateView.showToast(getString(R.string.error_occurred));
        }
        return resultOk;
    }

    private boolean cancelOrder(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
        return Model.startTransaction(context, () -> {
            prepareCartItems();
            String validateDate = MyUtil.getCurrentDate();
            createBackOrder();
            DataRow actionRow = models.tourVisitActionModel.createOrderDeletionAction(false, visitRow, latitude, longitude, validateDate);
            DataRow validatedOrder = models.visitOrderModel.browse(validatedOrderId);
            {
                models.tourVisitActionModel.delete(validatedOrder.getString("action_details_id"), true);
            }
            {
                Values values = new Values();
                values.put("state", ORDER_STATE_CANCEL);
                models.visitOrderModel.update(validatedOrderId, values);
            }
            {
                Values values = new Values();
                values.put("state", ORDER_STATE_CANCEL);
                models.visitOrderModel.update(orderRow.getString(Col.SERVER_ID), values);
            }
            return true;
        });
    }

    private void createBackOrder(){
        backOrderalidateHelper = ValidateHelper.getInstance(new BackOrderPaymentStrategy(context, this, currentUserRow),
                models, customerRow.getString(Col.SERVER_ID));
        backOrderalidateHelper.setPayment(orderRow.getFloat("payment_amount")).validate(latitude, longitude);
    }

    private void prepareCartItems(){
        CartItemSingleton.getInstance().clearItems();
        Map<String, DataRow> productsMap = models.companyProductModel.getMap(Col.SERVER_ID);
        for(DataRow row : orderLines){
            String productId = row.getString("product_id");
            cartItemSingleton.setQty(productsMap.get(productId), row.getFloat("qty"));
        }
    }


    @Override
    public boolean isRefund() {
        return false;
    }

    @Override
    public void showToast(String msg) {
        validateView.showToast(msg);
    }

    @Override
    public void showSimpleDialog(String title, String msg) {
        validateView.showSimpleDialog(title, msg);
    }

    @Override
    public boolean inNetwork() {
        return validateView.inNetwork();
    }

    @Override
    public void toggleLoading(boolean isRefreshing) {
        validateView.toggleLoading(isRefreshing);
    }

    @Override
    public void setToolbarTitle(String title) {

    }

    @Override
    public void requestCurrentLocation(IPaymentPresenter.LocationListener locationListener) {
//        backOrderalidateHelper.validate(latitude, longitude);
    }

    @Override
    public void openOrderDetails(boolean openPayment, String orderId) {
        validatedOrderId = orderId;
    }

    @Override
    public void goBack(int resultCode) {
//        validateView.goBack(resultCode);
    }
}
