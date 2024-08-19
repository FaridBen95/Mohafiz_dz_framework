package com.MohafizDZ.project.home_dir.pre_closing_presenter_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.TourModel;

public class PreClosingPresenterImpl implements IPreClosingPresenter.Presenter{
    private static final String TAG = PreClosingPresenterImpl.class.getSimpleName();

    private final IPreClosingPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private DataRow tourRow, distributorRow;

    public PreClosingPresenterImpl(IPreClosingPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
    }

    @Override
    public void onViewCreated() {
        initData();
        view.toggleCashBox(tourRow != null && tourRow.getBoolean("use_cash_box"));
        onRefresh();
    }

    private void initData(){
        this.distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        this.tourRow = models.tourModel.getCurrentTour(distributorRow);
    }

    @Override
    public void onRefresh() {
        initData();
        view.checkExpenses(tourRow != null && tourRow.getBoolean("expenses_validated"));
        view.checkCashBox(tourRow != null && tourRow.getBoolean("cash_box_validated"));
        view.checkSales(tourRow != null && tourRow.getBoolean("sales_validated"));
        view.checkInventory(tourRow != null && tourRow.getBoolean("inventory_validated"));
    }

    @Override
    public void setTourId(String tourId) {

    }

    @Override
    public void requestOpenExpenses() {
        String tourId = tourRow != null? tourRow.getString(Col.SERVER_ID) : null;
        view.openExpenses(tourId);
    }

    @Override
    public void requestOpenCashBox() {
        if(allowCashBox()){
            if(canOpenCashBox()){
                String tourId = tourRow != null? tourRow.getString(Col.SERVER_ID) : null;
                view.openCashBox(tourId);
            }else {
                String title = getString(R.string.cash_box_locked_title);
                String msg = getString(R.string.cash_box_locked_msg);
                view.showSimpleDialog(title, msg);
            }
        }else{
            String title = getString(R.string.cant_use_cash_box_title);
            String msg = getString(R.string.cant_use_cash_box_msg);
            view.showSimpleDialog(title, msg);
        }
    }


    @Override
    public void requestOpenSales() {
        String tourId = tourRow != null? tourRow.getString(Col.SERVER_ID) : null;
        view.openSales(tourId);
    }

    @Override
    public void requestOpenInventory() {
        if(canOpenInventory()){
            String tourId = tourRow != null? tourRow.getString(Col.SERVER_ID) : null;
            view.openInventory(tourId);
        }else {
            String title = getString(R.string.inventory_locked_title);
            String msg = getString(R.string.inventory_locked_msg);
            view.showSimpleDialog(title, msg);
        }
    }

    private String getString(int resId){
        return context.getString(resId);
    }

    private boolean allowCashBox(){
        return tourRow != null && tourRow.getBoolean("use_cash_box");
    }

    private boolean canOpenCashBox(){
        return tourRow != null && tourRow.getBoolean("expenses_validated");
    }

    private boolean canOpenInventory(){
        return tourRow != null && tourRow.getBoolean("sales_validated");
    }

    private static class Models{
        private final TourModel tourModel;
        private final DistributorModel distributorModel;

        private Models(Context context){
            this.tourModel = new TourModel(context);
            this.distributorModel = new DistributorModel(context);
        }
    }
}
