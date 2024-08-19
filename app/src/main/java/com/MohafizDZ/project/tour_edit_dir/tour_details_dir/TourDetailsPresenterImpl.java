package com.MohafizDZ.project.tour_edit_dir.tour_details_dir;

import static com.MohafizDZ.project.models.TourModel.STATE_CLOSED;
import static com.MohafizDZ.project.models.TourModel.STATE_CLOSING;
import static com.MohafizDZ.project.models.TourModel.STATE_CONFIRMED;
import static com.MohafizDZ.project.models.TourModel.STATE_DRAFT;
import static com.MohafizDZ.project.models.TourModel.STATE_PRE_CLOSING;
import static com.MohafizDZ.project.models.TourModel.STATE_PROGRESS;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.RegionModel;
import com.MohafizDZ.project.models.TourConfigurationModel;
import com.MohafizDZ.project.models.TourModel;

import java.util.List;

public class TourDetailsPresenterImpl implements ITourDetailsPresenter.Presenter{
    private static final String TAG = TourDetailsPresenterImpl.class.getSimpleName();

    private final ITourDetailsPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private final String tourId;
    private DataRow distributorRow, tourRow;

    public TourDetailsPresenterImpl(ITourDetailsPresenter.View view, Context context, DataRow currentUserRow, String tourId) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.tourId = tourId;
        this.models = new Models(context);
    }

    @Override
    public void onViewCreated() {
        initData();
        if(tourRow == null){
            view.showToast(getString(R.string.error_occurred));
            view.goBack();
            return;
        }
        onRefresh();
    }

    private String getString(int resId){
        return context.getString(resId);
    }

    private void initData(){
        distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        tourRow = tourId == null? models.tourModel.getCurrentTour(distributorRow) : models.tourModel.browse(tourId);
    }

    @Override
    public void onRefresh() {
        view.setTourName(tourRow.getString("name"));
        view.setVehicle(tourRow.getString("vehicle_name"));
        view.setRegion(getRegion());
        toggleViewBasedOnStates();
        prepareConfigurations();
    }

    private void toggleViewBasedOnStates(){
        String state = tourRow.getString("state");
        view.setState(state);
        view.togglePlanDate(false);
        view.toggleStartDate(false);
        view.togglePreClosingDate(false);
        view.toggleEndDate(false);
        view.toggleClosingDate(false);
        switch (state){
            case STATE_CLOSED:
                view.setEndDate(tourRow.getString("end_date"));
                view.toggleEndDate(true);
            case STATE_CLOSING:
                view.setClosingDate(tourRow.getString("closing_date"));
                view.toggleClosingDate(true);
            case STATE_PRE_CLOSING:
                view.setPreClosingDate(tourRow.getString("pre_closing_date"));
                view.togglePreClosingDate(true);
            case STATE_PROGRESS:
                view.setStartDate(tourRow.getString("start_date"));
                view.toggleStartDate(true);
            case STATE_CONFIRMED:
                view.setPlanDate(tourRow.getString("plan_date"));
                view.togglePlanDate(true);
        }
    }

    private void prepareConfigurations(){
        view.clearChips();
        List<DataRow> configurations = tourRow.getRelArrayRows(models.tourModel, "configurations");
        for(DataRow config : configurations){
            view.createChip(config.getString(Col.SERVER_ID), getString(config.getInteger("value")));
        }
    }
    private String getRegion(){
        try {
            return models.regionModel.browse(tourRow.getString("region_id")).getString("name");
        }catch (Exception ignored){}
        return "-";
    }

    private static class Models{
        private final TourModel tourModel;
        private final DistributorModel distributorModel;
        private final RegionModel regionModel;
        private final TourConfigurationModel configurationModel;

        private Models(Context context){
            this.tourModel = new TourModel(context);
            this.distributorModel = new DistributorModel(context);
            this.regionModel = new RegionModel(context);
            this.configurationModel = new TourConfigurationModel(context);
        }
    }
}
