package com.MohafizDZ.project.expenses_list_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.PaymentModel;
import com.MohafizDZ.project.models.TourModel;

import java.util.ArrayList;
import java.util.List;

public class BaseExpensesPresenterImpl implements IExpensesPresenter.Presenter{
    private static final String TAG = BaseExpensesPresenterImpl.class.getSimpleName();

    private final IExpensesPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private final List<DataRow> rows;
    private DataRow tourRow, distributorRow;
    private String tourId;

    public BaseExpensesPresenterImpl(IExpensesPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
        rows = new ArrayList<>();
    }

    @Override
    public void onViewCreated() {
        initData();
        view.toggleBottomNavigation(canShowBottomNavigation());
        view.toggleTotalContainer(false);
        view.initAdapter(rows);
        view.setToolbarTitle(getString(R.string.expenses_label));
        view.toggleValidateButton(false);
        view.toggleCreateButton(canCreateExpense());
        onRefresh();
    }

    protected boolean canShowBottomNavigation() {
        return true;
    }

    private String getString(int resId){
        return context.getString(resId);
    }

    private void initData(){
        distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        tourRow = tourId != null? models.tourModel.browse(tourId) : models.tourModel.getCurrentTour(distributorRow);
    }
    @Override
    public void onRefresh() {
        loadPayments();
        view.onLoadFinished(rows);
    }

    private void loadPayments(){
        rows.clear();
        if(tourRow != null) {
            String selection = " customer_id = ? and tour_id = ? ";
            String[] args = {"false", tourRow.getString(Col.SERVER_ID)};
            for (DataRow paymentRow : models.paymentModel.getRows(selection, args)) {
                paymentRow.putRel("user", currentUserRow);
                rows.add(paymentRow);
            }
        }
    }

    @Override
    public void requestCreateExpense() {
        if(canCreateExpense()){
            view.openExpenseActivity();
        }
    }

    private boolean canCreateExpense(){
        return tourRow != null && !tourRow.getBoolean("expenses_validated");
    }

    @Override
    public void onItemClick(int position) {
        DataRow row = rows.get(position);
        view.requestOpenDetails(row.getString(Col.SERVER_ID), false);
    }

    @Override
    public void onItemLongClick(int position) {

    }

    @Override
    public void requestValidateExpenses() {

    }

    @Override
    public void validateExpenses() {

    }

    @Override
    public void setTourId(String tourId) {
        this.tourId = tourId;
    }

    @Override
    public void setEditable(boolean isEditable) {

    }

    private static class Models{
        private final TourModel tourModel;
        private final DistributorModel distributorModel;
        private final PaymentModel paymentModel;

        private Models(Context context){
            this.tourModel = new TourModel(context);
            this.distributorModel = new DistributorModel(context);
            this.paymentModel = new PaymentModel(context);
        }
    }
}
