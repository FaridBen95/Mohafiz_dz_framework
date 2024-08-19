package com.MohafizDZ.project.regions_map_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.project.models.RegionModel;

import java.util.List;

public class RegionsMapPresenterImpl implements IRegionsMapPresenter.Presenter{
    private static final String TAG = RegionsMapPresenterImpl.class.getSimpleName();

    private final IRegionsMapPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;

    public RegionsMapPresenterImpl(IRegionsMapPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
    }

    @Override
    public void onViewCreated() {

    }

    private void prepareRegions(){
        List<DataRow> regions = models.regionModel.getRows();
        for(DataRow row : regions){
            double latitude= Double.valueOf(row.getString("latitude"));
            double longitude = Double.valueOf(row.getString("longitude"));
            float radius = row.getFloat("radius");
            view.showRegion(latitude, longitude, radius, false);
            view.addMarker(row.getString("name"), latitude, longitude, false);
        }
    }

    @Override
    public void onRefresh() {
        prepareRegions();
        view.getCurrentLocation();
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
}
