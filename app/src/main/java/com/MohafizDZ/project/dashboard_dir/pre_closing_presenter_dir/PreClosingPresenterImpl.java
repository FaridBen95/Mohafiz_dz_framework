package com.MohafizDZ.project.dashboard_dir.pre_closing_presenter_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.home_dir.pre_closing_presenter_dir.IPreClosingPresenter;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.TourModel;

public class PreClosingPresenterImpl implements IPreClosingPresenter.Presenter{
    private static final String TAG = PreClosingPresenterImpl.class.getSimpleName();

    private final IPreClosingPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private DataRow tourRow, distributorRow;
    private String tourId;

    public PreClosingPresenterImpl(IPreClosingPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
    }

    @Override
    public void onViewCreated() {
        initData();
        view.toggleCashBox(tourRow.getBoolean("use_cash_box"));
        onRefresh();
    }
    @Override
    public void setTourId(String tourId) {
        this.tourId = tourId;
        initData();
        view.toggleCashBox(useCashBox());
        onRefresh();
    }

    private boolean useCashBox(){
        return tourRow != null && tourRow.getBoolean("use_cash_box");
    }

    private void initData(){
        this.distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        this.tourRow = models.tourModel.browse(tourId);
    }

    @Override
    public void onRefresh() {
        initData();
        view.checkExpenses(expensesValidated());
        view.checkCashBox(cashBoxValidated());
        view.checkSales(salesValidated());
        view.checkInventory(inventoryValidated());
    }

    private boolean expensesValidated(){
        return tourRow != null && tourRow.getBoolean("expenses_validated");
    }

    private boolean cashBoxValidated(){
        return tourRow != null && tourRow.getBoolean("cash_box_validated");
    }

    private boolean salesValidated(){
        return tourRow != null && tourRow.getBoolean("sales_validated");
    }

    private boolean inventoryValidated(){
        return tourRow != null && tourRow.getBoolean("inventory_validated");
    }

    @Override
    public void requestOpenExpenses() {
        if(tourId != null) {
            view.openExpenses(tourId);
        }
    }

    @Override
    public void requestOpenCashBox() {
        if(allowCashBox()){
            if(canOpenCashBox()){
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
        if(tourId != null) {
            view.openSales(tourId);
        }
    }

    @Override
    public void requestOpenInventory() {
        if(canOpenInventory()){
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
