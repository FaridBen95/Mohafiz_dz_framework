package com.MohafizDZ.project.dashboard_dir;


import androidx.core.util.Pair;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;
import com.MohafizDZ.framework_repository.core.DataRow;

import java.util.LinkedHashMap;

public interface IDashboardPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void onSelectTour(String key);

        void requestOpenTourPlan();

        void requestOpenTourOpening();

        void requestSelectStartDate();

        void requestSelectEndDate();

        void onSelectDateRange(Pair<Long, Long> dateRange);

        void requestToggleProgressContainer();
        void requestTogglePreClosingContainer();
    }

    interface View extends BasePresenter.View{

        void setTourList(LinkedHashMap<String, String> tourList, DataRow currentTourRow);

        void refreshVisitContainer(boolean enabled, String tourId);
        void refreshPreClosingContainer(boolean enabled, String tourId);
        void toggleTourPlaning(boolean visible);
        void toggleTourOpening(boolean visible);
        void toggleTourProgress(boolean visible);
        void toggleTourPreClosing(boolean visible);

        void setTourName(String text);
        void setVehicle(String text);
        void setRegion(String text);

        void openTourPlanForm(String tourId);

        void openInitialStock(String tourId);

        void requestSelectDateRange(Pair<Long, Long> dateRange);

        void setDateStart(String date);
        void setDateEnd(String date);

        void updateProgressToggleButtonIcon(int drawable);
        void updatePreClosingToggleButtonIcon(int drawable);

        void toggleProgressContainer(boolean visible);
        void togglePreClosingContainer(boolean visible);
    }
}
