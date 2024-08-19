package com.MohafizDZ.project.dashboard_dir.visit_presenter_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;

public interface IVisitPresenter {
    interface Presenter extends BasePresenter.Presenter {

        void setTourId(String tourId);

        void onSelectCustomer(String customerId);

        void requestCustomerDetails();

        void onStartVisitClicked();
        void onStopVisitClicked();

        void onRestartClicked();

        void requestOpenSale();

        void requestOpenBackOrder();

        void requestOpenNoAction();

        void requestOpenOtherAction();

        void requestOpenPaymentAction(boolean isRefund);

        void requestOpenOtherActionsList();

        void requestOpenActionsList();

        void requestShowGoal();

        void requestOpenSales();
        void requestOpenBackOrders();

        void requestOpenCustomersList();

        void requestOpenPayments(boolean isRefund);

        void requestOpenNoActions();
    }

    interface View extends BasePresenter.View{

        void toggleCustomerDetails(boolean visible);

        void toggleStartVisit(boolean visible);
        void toggleStopVisit(boolean visible);
        void toggleRestartVisit(boolean visible);

        void toggleCustomersListButton(boolean visible);

        void checkSale(int count);
        void checkBackOrder(int count);
        void checkNoAction(int count);
        void checkOtherAction(int count);
        void checkPaymentAction(int count);
        void checkRefundAction(int count);

        void toggleSaleChip(boolean visible);
        void toggleBackOrderChip(boolean visible);
        void toggleNoActionChip(boolean visible);
        void toggleOtherChip(boolean visible);
        void togglePaymentChip(boolean visible);
        void toggleRefundChip(boolean visible);

        void showGoalSnackBar(String goalText);

        void setVisitsProgress(int visitedCount, int plannedVisitsCount);

        void requestOpenSales(String tourId);

        void requestOpenBackOrders(String tourId);

        void requestOpenCustomersList(String tourId);

        void requestOpenPayments(String tourId, boolean isRefund);

        void requestOpenActions(String tourId, String actionName);
    }

    interface LocationListener{
        void onLocationChanged(double latitude, double longitude);

        void onStart();

        void onFailed();
    }
}
