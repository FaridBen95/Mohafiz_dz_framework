package com.MohafizDZ.project.order_details_dir.strategies;

import android.app.Activity;
import android.content.Context;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.catalog_strategies.payment_strategies.OrderPaymentStrategy;
import com.MohafizDZ.project.payment_dir.IPaymentPresenter;

public class DeleteOrderPaymentStrategy extends OrderPaymentStrategy {
    private final DataRow orderRow;

    public DeleteOrderPaymentStrategy(Context context, IPaymentPresenter.ValidateView view, DataRow currentUserRow, DataRow orderRow) {
        super(context, view, currentUserRow);
        this.orderRow = orderRow;
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
        boolean resultOk = validateDeletion(latitude, longitude);
        if(resultOk) {
            validateView.showToast(getString(R.string.order_deleted_msg));
        }else{
            validateView.showToast(getString(R.string.error_occurred));
        }
        return resultOk;
    }

    private boolean validateDeletion(double latitude, double longitude){
        return Model.startTransaction(context, () -> {
            String validateDate = MyUtil.getCurrentDate();
            float remainingAmount = getBalance();
            DataRow paymentRow = models.paymentModel.browse(orderRow.getString("payment_line"));
            DataRow lastActionRow = models.tourVisitActionModel.browse(orderRow.getString("action_details_id"));
            DataRow actionRow = models.tourVisitActionModel.createOrderDeletionAction(true, visitRow, latitude, longitude, validateDate);
            if(paymentRow == null || actionRow == null || lastActionRow == null){
                return false;
            }
            {
                int deleteCount = models.paymentModel.delete(paymentRow.getString(Col.SERVER_ID), true);
                if(deleteCount <= 0){
                    return false;
                }
            }
            {
                int deleteCount = models.visitOrderModel.delete(orderRow.getString(Col.SERVER_ID), true);
                if(deleteCount <= 0){
                    return false;
                }
            }
            {
                int deleteCount = models.tourVisitActionModel.delete(actionRow.getString(Col.SERVER_ID), true);
                if(deleteCount <= 0){
                    return false;
                }
            }
            {
                models.visitOrderModel.emptyRelArray(orderRow, "products");
                int linesCount = models.visitOrderLineModel.deleteOrderLines(orderRow.getString(Col.SERVER_ID), true);
                if(linesCount <= 0){
                    return false;
                }
            }
            models.companyCustomerModel.updateBalance(customerRow.getString(Col.SERVER_ID), remainingAmount);
            this.validatedOrderId = orderRow.getString(Col.SERVER_ID);
            return true;
        });
    }


    @Override
    public boolean isRefund() {
        return false;
    }
}
