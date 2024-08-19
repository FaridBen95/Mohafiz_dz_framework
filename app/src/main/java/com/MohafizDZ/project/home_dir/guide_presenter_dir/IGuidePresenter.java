package com.MohafizDZ.project.home_dir.guide_presenter_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;

public interface IGuidePresenter {

    interface Presenter extends BasePresenter.Presenter{
        void lockGuideDetailsVisibility(boolean toggleVisible);
        void requestToggleGuideDetails();

        boolean canToggleGuideDetails();

        void onActionButtonClicked();

        boolean hasPhoneCallPermission();

        void requestPhoneCallPermission();

        String getSupportPhoneNumber();

        void requestOpenTourForm();

        void startTour();

        void endTour();

        void requestStopVisit();

        boolean canEndTour();

        boolean canReopenTour();

        void reopenTour();

        void closeTour();

        void validateClosingTour();

        boolean canStartTour();

        void requestOpenInitialStock();

        boolean blockTourPlaning();

        void requestScanForPlan();
    }

    interface View extends BasePresenter.View{


        void toggleGuideContainer(boolean visible);

        void toggleGuideButton(boolean visible, String text);
        void toggleGuideStepTitle(boolean visible, String text);

        void toggleGuideTitle(String text);

        void setImage(int imageDrawable);

        boolean checkPhoneCallPermission();

        void requestPhonePermission();

        void openTourForm();

        void toggleTourProgressContainer(boolean visible);
        void togglePreClosingContainer(boolean visible);

        void toggleGuideDetailsContainer(boolean visible);

        void updateToggleButtonIcon(int drawableResId);

        void requestCloseVisit();

        void requestOpenInitialStock();

        void requestPlanScan();

        void requestRefreshPresenters();
    }
}
