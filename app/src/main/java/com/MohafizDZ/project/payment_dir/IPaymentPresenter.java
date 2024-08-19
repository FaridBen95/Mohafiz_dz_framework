package com.MohafizDZ.project.payment_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;

public interface IPaymentPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void onAddPayment(String paymentAmount);

        void requestAddPayment();
        void requestEditPayment();

        void onValidate();
    }

    interface View extends ValidateView{

        void togglePaymentContainer(boolean visible);

        void toggleAddButton(boolean visible);

        void setName(String text);
        void setBalanceLimit(String text);
        void setBalance(String text);
        void setTotalToPay(String text);
        void setPaymentAmount(String text);
        void setActualBalance(String text);

        void openPaymentDialog(float totalToPay, Float paymentAmount);

        void setOrderAmount(String text);

        void setToolbarTitle(String title);

        void setTotalPaymentLabel(String text);
        void setPaymentLabel(String text);
    }

    interface ValidateView extends BasePresenter.View{
        void requestCurrentLocation(LocationListener locationListener);
        void openOrderDetails(boolean openPayment, String orderId);
        void goBack(int resultCode);
    }

    interface LocationListener{
        void onLocationChanged(double latitude, double longitude);

        void onStart();

        void onFailed();
    }

}
