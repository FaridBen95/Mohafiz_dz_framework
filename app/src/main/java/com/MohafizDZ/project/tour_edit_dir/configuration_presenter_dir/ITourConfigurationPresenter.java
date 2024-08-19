package com.MohafizDZ.project.tour_edit_dir.configuration_presenter_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;

public interface ITourConfigurationPresenter {

    interface Presenter extends BasePresenter.Presenter{

    }

    interface View extends BasePresenter.View{

        void createChip(String key, String text);
    }
}
