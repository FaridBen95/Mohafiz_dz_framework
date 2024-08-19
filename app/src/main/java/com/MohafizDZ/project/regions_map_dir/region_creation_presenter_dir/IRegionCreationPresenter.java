package com.MohafizDZ.project.regions_map_dir.region_creation_presenter_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;
import com.MohafizDZ.project.regions_map_dir.IRegionsMapPresenter;

public interface IRegionCreationPresenter {

    interface Presenter{

        void onMapClicked(double latitude, double longitude);

        void onMarkerDragStart();

        void onMarkerDragEng(double latitude, double longitude);

        void onSliderValueChange(float value);

        void onBackPressed();

        void save();

        void onCreateOptionsMenu();
    }

    interface View extends BasePresenter.View{

        void showRegionNameDialog(IRegionsMapPresenter.RegionDialogListener regionDialogListener);

        void addMarker(String name, double latitude, double longitude, boolean draggable);

        void showRegion(double latitude, double longitude, float radius, boolean isNewRegion);

        void toggleSlider(boolean visible);
        void toggleCurrentCircle(boolean visible);

        void setCurrentCircleRadius(float radius);

        void toggleSave(boolean visible);

        void showIgnoreChangesDialog();

        void goBack();
    }
}
