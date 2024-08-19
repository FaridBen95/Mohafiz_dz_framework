package com.MohafizDZ.project.customer_details_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;

public interface ICustomerVisitPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void onChipClicked(int position);

        void setTourId(String tourId);
    }

    interface View extends BasePresenter.View{

        void createActionChip(String action, int position);

        void openActionView(String actionId);

        void clearChipGroup();

        void setVisitDuration(String txt);

        void setVisitNetAmount(String txt);
        void setPaymentsAmount(String txt);
    }
}
