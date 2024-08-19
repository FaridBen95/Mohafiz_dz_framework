package com.MohafizDZ.project.home_dir.details_presenter_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;

public interface IDetailsPresenter {

    interface Presenter extends BasePresenter.Presenter{
        void requestSyncUp();
    }

    interface View extends BasePresenter.View{

        void prepareView();

        void requestSyncPlannerModel();

        void toggleSyncSnackBar(boolean visible);

        void setSyncSnackBarTitle(String title);
    }
}
