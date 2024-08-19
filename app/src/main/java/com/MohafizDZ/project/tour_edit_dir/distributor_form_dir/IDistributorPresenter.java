package com.MohafizDZ.project.tour_edit_dir.distributor_form_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;

import java.util.List;

public interface IDistributorPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void onValidate(String vehicleName, List<String> configurations, String expensesLimit);
    }

    interface View extends BasePresenter.View{

        void setDefaultVehicleName(String txt);
        void setSellerName(String txt);
        void setJoinDate(String txt);

        void createChip(String key, String value);

        void clearChips();

        void selectChip(String key);

        void goBack();

        void setExpensesLimit(String expensesLimit);
    }
}
