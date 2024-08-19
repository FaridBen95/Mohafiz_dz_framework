package com.MohafizDZ.project.tour_edit_dir.basic_details_presenter_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;

import java.util.LinkedHashMap;

public interface ITourBasicDetailsPresenter {

    interface Presenter extends BasePresenter.Presenter {

        void onSelectRegion(String key);

    }

    interface View extends BasePresenter.View{

        void setTourName(String text);
        void setDistributorName(String text);
        void setVehicleName(String text);

        void initRegionsFilter(LinkedHashMap<String, String> regions);

        void openRegionMap();
    }
}
