package com.MohafizDZ.project.regions_map_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;

public interface IRegionsMapPresenter {

    interface Presenter extends BasePresenter.Presenter{

    }

    interface View extends BasePresenter.View{

        void getCurrentLocation();

        void showRegion(double latitude, double longitude, float radius, boolean isNewRegion);

        void addMarker(String name, double latitude, double longitude, boolean draggable);
    }

    interface RegionDialogListener{
        void onPositiveClicked(String name);
    }
}
