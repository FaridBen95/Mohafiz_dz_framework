package com.MohafizDZ.project.tour_edit_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.CompanyUserModel;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.TourModel;

import java.util.List;

public class TourPlanPresenterImpl implements ITourPlanPresenter.Presenter{
    private static final String TAG = TourPlanPresenterImpl.class.getSimpleName();
    private final ITourPlanPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private DataRow tourRow, distributorRow;
    private int step = 0;

    public TourPlanPresenterImpl(ITourPlanPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
    }

    @Override
    public void onViewCreated() {
        initData();
        view.setVehicleName(distributorRow.getString("default_vehicle_name"));
        view.setExpensesLimit(distributorRow.getString("expenses_limit"));
        toggleView();
    }

    private void initData(){
        distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
    }
    @Override
    public void onRefresh() {

    }

    @Override
    public void onDistributorChanged() {
        distributorRow = models.distributorModel.browse(distributorRow.getString(Col.SERVER_ID));
        view.setVehicleName(distributorRow.getString("default_vehicle_name"));
        view.setExpensesLimit(distributorRow.getString("expenses_limit"));
    }

    @Override
    public void requestEditDistributor() {
        DataRow companyUser = models.companyUserModel.getCurrentUser(currentUserRow);
        if(!CompanyUserModel.getRole(companyUser).equals(CompanyUserModel.ADMIN_ROLE)){
            view.requestScanAdminQRCode();
        }else{
            view.openDistributorForm();
        }
    }

    @Override
    public void onValidate(String vehicleName, String regionId, String expenseLimit, String visitsGoal, String goal, List<String> configurations) {
        step++;
        if(step >= 2){
            saveTour(vehicleName, regionId, expenseLimit, visitsGoal, goal, configurations);
        }else {
            toggleView();
        }
    }

    private void saveTour(String vehicleName, String regionId, String expenseLimit, String visitsGoal, String goal, List<String> configurations){
        boolean result = Model.startTransaction(context, () -> {
            Values values = new Values();
            values.put("vehicle_name", vehicleName);
            values.put("region_id", regionId);
            values.put("expenses_limit", expenseLimit);
            values.put("visits_goal_count", visitsGoal);
            values.put("goal_text", goal);
            values.put("state", TourModel.STATE_CONFIRMED);
            tourRow = models.tourModel.getCurrentTour(distributorRow, true);
            String tourId = tourRow.getString(Col.SERVER_ID);
            models.tourModel.insertRelArray(tourId, "configurations", configurations);
            return models.tourModel.update(tourId, values) > 0;
        });
        if(result){
            view.goBack();
        }else{
            view.showToast(getString(R.string.error_occurred));
        }
    }

    private String getString(int resId){
        return context.getString(resId);
    }

    @Override
    public void onBackPressed() {
        if(step == 0){
            view.goBack();
        }else{
            step--;
            toggleView();
        }
    }

    private void toggleView(){
        view.toggleBasicDetailsContainer(false);
        view.toggleConfigurationContainer(false);
        if(step == 0){
            view.toggleBasicDetailsContainer(true);
        }else if(step == 1){
            view.toggleConfigurationContainer(true);
        }
    }

    private static class Models{
        private final DistributorModel distributorModel;
        private final TourModel tourModel;
        private final CompanyUserModel companyUserModel;

        public Models(Context context) {
            this.distributorModel = new DistributorModel(context);
            this.tourModel = new TourModel(context);
            this.companyUserModel = new CompanyUserModel(context);
        }
    }
}
