package com.MohafizDZ.project.home_dir.pre_closing_presenter_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;

public interface IPreClosingPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void setTourId(String tourId);

        void requestOpenExpenses();

        void requestOpenCashBox();

        void requestOpenSales();

        void requestOpenInventory();
    }

    interface View extends BasePresenter.View{

        void toggleCashBox(Boolean visible);

        void checkExpenses(Boolean checked);
        void checkCashBox(Boolean checked);
        void checkSales(Boolean checked);
        void checkInventory(Boolean checked);

        void openExpenses(String tourId);

        void openCashBox(String tourId);

        void openSales(String tourId);

        void openInventory(String tourId);
    }
}
