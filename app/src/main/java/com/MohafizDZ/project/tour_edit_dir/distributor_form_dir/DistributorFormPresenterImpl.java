package com.MohafizDZ.project.tour_edit_dir.distributor_form_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.DistributorConfigurationModel;
import com.MohafizDZ.project.models.DistributorModel;

import java.util.ArrayList;
import java.util.List;

public class DistributorFormPresenterImpl implements IDistributorPresenter.Presenter {
    private static final String TAG = DistributorFormPresenterImpl.class.getSimpleName();

    private final IDistributorPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private final List<String> distributorConfig;
    private DataRow distributorRow;

    public DistributorFormPresenterImpl(IDistributorPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
        distributorConfig = new ArrayList<>();
    }

    private String getString(int resId){
        return context.getString(resId);
    }

    @Override
    public void onViewCreated() {
        view.setToolbarTitle(getString(R.string.distributor_label));
        initData();
        view.setDefaultVehicleName(getVehicleName());
        view.setSellerName(currentUserRow.getString("name"));
        view.setJoinDate(distributorRow.getString("join_date"));
        view.setExpensesLimit(distributorRow.getString("expenses_limit"));
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

    private void initData(){
        distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        distributorConfig.clear();
        distributorConfig.addAll(distributorRow.getRelArray(models.distributorModel, "configurations"));
    }

    @Override
    public void onRefresh() {
        refreshConfigurations();
    }

    private void refreshConfigurations(){
        view.clearChips();
        for(DataRow row : models.configurationModel.getRows()){
            view.createChip(row.getString("key"), getString(row.getInteger("value")));
        }
        for(String key : distributorConfig){
            view.selectChip(key);
        }
    }

    @Override
    public void onValidate(String vehicleName, List<String> configurations, String expensesLimit) {
        boolean result = Model.startTransaction(context, () -> {
            Values values = new Values();
            values.put("default_vehicle_name", vehicleName);
            values.put("expenses_limit", expensesLimit);
            String distributorId = distributorRow.getString(Col.SERVER_ID);
            models.distributorModel.insertRelArray(distributorId, "configurations", configurations);
            return models.distributorModel.update(distributorId, values) > 0;
        });
        if(result){
            view.goBack();
        }else{
            view.showToast(getString(R.string.error_occurred));
        }
    }

    private static class Models{
        private final DistributorModel distributorModel;
        private final DistributorConfigurationModel configurationModel;

        private Models(Context context){
            this.distributorModel = new DistributorModel(context);
            this.configurationModel = new DistributorConfigurationModel(context);
        }
    }
}
