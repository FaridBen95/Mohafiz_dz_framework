package com.MohafizDZ.project.tour_edit_dir.tour_details_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;

public interface ITourDetailsPresenter {

    interface Presenter extends BasePresenter.Presenter{

    }

    interface View extends BasePresenter.View{

        void goBack();
        void setTourName(String text);

        void setState(String text);
        void setEndDate(String text);
        void setPlanDate(String text);
        void setStartDate(String text);
        void setPreClosingDate(String text);
        void setClosingDate(String text);
        void setVehicle(String text);
        void setRegion(String text);
        void togglePlanDate(boolean visible);
        void toggleStartDate(boolean visible);
        void togglePreClosingDate(boolean visible);
        void toggleEndDate(boolean visible);
        void toggleClosingDate(boolean visible);

        void createChip(String id, String name);

        void clearChips();

    }
}
