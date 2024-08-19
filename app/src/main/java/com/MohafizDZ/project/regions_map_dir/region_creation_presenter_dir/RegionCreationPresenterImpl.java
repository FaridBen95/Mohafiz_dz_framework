package com.MohafizDZ.project.regions_map_dir.region_creation_presenter_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.RegionModel;

public class RegionCreationPresenterImpl implements IRegionCreationPresenter.Presenter{
    private static final String TAG = RegionCreationPresenterImpl.class.getSimpleName();
    private static float DEFAULT_RADIUS = 20;
    private static float DEFAULT_RADIUS_FACTOR = 10;

    private final IRegionCreationPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private Region region;

    public RegionCreationPresenterImpl(IRegionCreationPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
    }

    @Override
    public void onMapClicked(double latitude, double longitude) {
        if(unsavedRegion()) {
            view.showRegionNameDialog(name -> {
                if (name.equals("")) {
                    view.showToast(getString(R.string.name_required));
                } else {
                    region = new Region(latitude, longitude, DEFAULT_RADIUS * DEFAULT_RADIUS_FACTOR, name);
                    view.addMarker(name, latitude, longitude, true);
                    view.showRegion(latitude, longitude, region.radius, true);
                    view.toggleSlider(true);
                    view.toggleCurrentCircle(true);
                    view.toggleSave(true);
                }
            });
        }
    }

    private boolean unsavedRegion(){
        return region == null || region.validated;
    }

    @Override
    public void onMarkerDragStart() {
        view.toggleSave(true);
        view.toggleSlider(false);
        view.toggleCurrentCircle(false);
    }

    @Override
    public void onMarkerDragEng(double latitude, double longitude) {
        view.toggleSlider(true);
        view.toggleCurrentCircle(true);
        view.toggleSave(true);
        region.latitude = latitude;
        region.longitude = longitude;
    }

    @Override
    public void onSliderValueChange(float value) {
        region.radius = value * DEFAULT_RADIUS;
        view.setCurrentCircleRadius(region.radius);
    }

    @Override
    public void onCreateOptionsMenu() {
        view.toggleSave(false);
    }

    @Override
    public void onBackPressed() {
        if(!unsavedRegion()){
            view.showIgnoreChangesDialog();
        }else{
            view.goBack();
        }
    }

    @Override
    public void save() {
        boolean created = createRegion();
        if(created) {
            view.showToast(getString(R.string.region_saved_msg));
            view.goBack();
        }else{
            view.showSimpleDialog(getString(R.string.error_occurred), getString(R.string.contact_admin_title));
        }
    }

    private boolean createRegion(){
        if(region != null) {
            Values values = new Values();
            values.put("name", region.name);
            values.put("creator_id", currentUserRow.getString(Col.SERVER_ID));
            values.put("state_id", currentUserRow.getString("state_id"));
            values.put("country_id", currentUserRow.getString("country_id"));
            values.put("latitude", region.latitude);
            values.put("longitude", region.longitude);
            values.put("geo_hash", region.getGeoHash());
            values.put("radius", region.radius);
            region.validated = true;
            int _id = models.regionModel.insert(values);
            return _id > 0;
        }
        return false;
    }

    private String getString(int resId){
        return context.getString(resId);
    }

    private static class Models{
        private final RegionModel regionModel;

        private Models(Context context){
            this.regionModel = new RegionModel(context);
        }
    }

    private static class Region{
        private double latitude;
        private double longitude;
        private float radius;
        private String name;
        private boolean validated;

        public Region(double latitude, double longitude, float radius, String name) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.radius = radius;
            this.name = name;
        }

        public String getGeoHash() {
            return MyUtil.getGeoHash(latitude, longitude);
        }
    }
}
