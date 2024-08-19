package com.MohafizDZ.project.tour_edit_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;

import java.util.List;

public interface ITourPlanPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void onValidate(String vehicleName, String regionId, String expenseLimit, String visitsGoal, String goal, List<String> configurations);

        void onBackPressed();

        void onDistributorChanged();

        void requestEditDistributor();
    }

    interface View extends BasePresenter.View{

        void toggleBasicDetailsContainer(boolean visible);
        void toggleConfigurationContainer(boolean visible);
        void goBack();

        void setVehicleName(String defaultVehicleName);

        void setExpensesLimit(String txt);

        void requestScanAdminQRCode();

        void openDistributorForm();
    }
}
