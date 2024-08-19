package com.MohafizDZ.project.tour_edit_dir.basic_details_presenter_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.DistributorConfigurationModel;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.RegionModel;
import com.MohafizDZ.project.models.TourModel;

import java.util.LinkedHashMap;
import java.util.List;

public class TourBasicDetailsPresenterImpl implements ITourBasicDetailsPresenter.Presenter {
    private static final String TAG = TourBasicDetailsPresenterImpl.class.getSimpleName();

    private final ITourBasicDetailsPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private DataRow currentTourRow, distributorRow;

    public TourBasicDetailsPresenterImpl(ITourBasicDetailsPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
    }

    @Override
    public void onViewCreated() {
        initData();
        String tourName;
        if(currentTourRow != null) {
            tourName = currentTourRow.getString("name");
        }else{
            tourName = models.tourModel.checkAndGenerateName(distributorRow, "", null);
        }
        view.setTourName(tourName);
        view.setDistributorName(currentUserRow.getString("name"));
        view.setVehicleName(getVehicleName());
        onRefresh();
    }

    private String getVehicleName(){
        final String defaultVehicleName = distributorRow.getString("default_vehicle_name");
        final String vehicleName = currentUserRow.getString("vehicle_name");
        if(defaultVehicleName.equals("false") || defaultVehicleName.equals("")){
            if(vehicleName.equals("false")){
                return "";
            }
            return vehicleName;
        }
        return defaultVehicleName;
    }

    private LinkedHashMap<String, String> getRegions(){
        List<DataRow> regions = models.regionModel.getRows();
        regions.add(createRegionRow());
        return getNamesFromRows(regions);
    }

    private DataRow createRegionRow(){
        DataRow row = new DataRow();
        row.put("name", getString(R.string.create_region_label));
        row.put("id", "-2");
        row.put("_id", -2);
        return row;
    }

    private String getString(int resId){
        return context.getString(resId);
    }

    private LinkedHashMap<String, String> getNamesFromRows(List<DataRow> rows) {
        LinkedHashMap<String, String> list = new LinkedHashMap<>();
        for(DataRow row : rows){
            list.put(row.getString(Col.SERVER_ID), row.getString("name"));
        }
        return list;
    }


    private void initData(){
        distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        currentTourRow = models.tourModel.getCurrentTour(distributorRow);
    }

    @Override
    public void onRefresh() {
        view.initRegionsFilter(getRegions());
    }

    @Override
    public void onSelectRegion(String key) {
        if(key.equals("-2")){
            if(canEditRegions()) {
                view.openRegionMap();
            }else{
                view.showToast(getString(R.string.cant_edit_regions_msg));
            }
        }
    }

    private boolean canEditRegions() {
        List<String> configurations = distributorRow.getRelArray(models.distributorModel, "configurations");
        return DistributorConfigurationModel.canEditRegions(configurations);
    }

    private static class Models{
        private final TourModel tourModel;
        private final DistributorModel distributorModel;
        private final RegionModel regionModel;
        private Models(Context context){
            this.tourModel = new TourModel(context);
            this.distributorModel = new DistributorModel(context);
            this.regionModel = new RegionModel(context);
        }
    }
}
