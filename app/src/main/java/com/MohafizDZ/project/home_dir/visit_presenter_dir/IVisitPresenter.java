package com.MohafizDZ.project.home_dir.visit_presenter_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;

public interface IVisitPresenter {
    interface Presenter extends BasePresenter.Presenter {

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
        void requestOpenNoActionsList();

        void requestOpenActionsList();

        void requestOpenCustomersList();

        void requestShowGoal();

        void requestOpenOrdersList(boolean isBackOrder);

        void requestOpenPaymentsLis(boolean isRefund);
    }

    interface View extends BasePresenter.View{

        void toggleCustomerDetails(boolean visible);

        void setCustomerName(String text);
        void setCustomerImage(String text);

        void openCustomerDetails(String customerId);

        void toggleStartVisit(boolean visible);
        void toggleStopVisit(boolean visible);
        void toggleRestartVisit(boolean visible);

        void toggleCustomersListButton(boolean visible);

        void requestCurrentLocation(LocationListener locationListener);

        void toggleActionsContainer(boolean visible);

        void openOrderCatalog(String customerId, String strategyClassName);

        void checkSale(boolean checked);
        void checkBackOrder(boolean checked);
        void checkNoAction(boolean checked);
        void checkOtherAction(boolean checked);
        void checkPaymentAction(boolean checked);
        void checkRefundAction(boolean checked);

        void openNoActionForm(String action, String customerId);

        void openNoActionDetails(String actionId);

        void openPaymentActivity(String strategyClassName, String customerId);

        void toggleSaleChip(boolean visible);
        void toggleBackOrderChip(boolean visible);
        void toggleNoActionChip(boolean visible);
        void toggleOtherChip(boolean visible);
        void togglePaymentChip(boolean visible);
        void toggleRefundChip(boolean visible);

        void openActionsList(String tourId, String customerId, String actionName);

        void showGoalSnackBar(String goalText);

        void setVisitsProgress(int visitedCount, int plannedVisitsCount);

        void openCustomersList(String tourId);

        void openOrdersList(String tourId, String customerId, boolean isBackOrder);

        void openPaymentsList(String tourId, String customerId, boolean isRefund);

        void toggleGoalButton(boolean visible);
    }

    interface LocationListener{
        void onLocationChanged(double latitude, double longitude);

        void onStart();

        void onFailed();
    }
}
