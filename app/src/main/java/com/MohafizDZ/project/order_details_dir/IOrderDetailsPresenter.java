package com.MohafizDZ.project.order_details_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.project.payment_dir.IPaymentPresenter;

import java.util.List;

public interface IOrderDetailsPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void onCreateOptionsMenu();

        void cancelOrder(IPaymentPresenter.ValidateView validateView);

        void requestCancelOrder();
    }

    interface View extends BasePresenter.View{

        void goBack();

        void setOrderName(String txt);
        void setCustomerName(String txt);
        void setDistanceToCustomer(String txt);
        void setTourName(String txt);
        void setSellerName(String txt);
        void setDate(String txt);
        void setOrderAmount(String txt);
        void setPaymentAmount(String txt);
        void setRemainingAmount(String txt);

        void initAdapter(List<DataRow> lines);

        void onLoadFinished(List<DataRow> lines);

        void toggleDeleteMenuItem(boolean visible);

        void showCancelDialog(String title, String msg, String positiveTitle);
    }
}
