package com.MohafizDZ.project.expenses_list_dir;

import android.content.Context;
import android.util.Pair;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.CompanyModel;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.PaymentModel;
import com.MohafizDZ.project.models.TourModel;

import java.util.ArrayList;
import java.util.List;

public class ExpensesValidationPresenterImpl implements IExpensesPresenter.Presenter{
    private static final String TAG = ExpensesValidationPresenterImpl.class.getSimpleName();

    private final IExpensesPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private final List<DataRow> rows;
    private DataRow tourRow, distributorRow;
    private final String currencyCode;
    private String tourId;
    private boolean isEditable;

    public ExpensesValidationPresenterImpl(IExpensesPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
        rows = new ArrayList<>();
        this.currencyCode = CompanyModel.getCompanyCurrency(context);
    }

    @Override
    public void onViewCreated() {
        initData();
        view.initAdapter(rows);
        view.setToolbarTitle(getString(R.string.expenses_label));
        view.toggleBottomNavigation(false);
        view.toggleTotalContainer(true);
        onRefresh();
    }

    private String getString(int resId){
        return context.getString(resId);
    }

    private void initData(){
        distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        tourRow = models.tourModel.browse(tourId);
    }
    @Override
    public void onRefresh() {
        loadPayments();
        Pair<Float, Float> totalExpenses = getTotalExpenses();
        view.setTotalExpenses(getPrice(totalExpenses.first));
        view.setValidatedExpenses(getPrice(totalExpenses.second));
        view.setExpensesLimit(getExpensesLimit() != 0, getPrice(getExpensesLimit()));
        view.onLoadFinished(rows);
        view.toggleCreateButton(unvalidated() && isEditable);
        view.toggleValidateButton(unvalidated() && isEditable);
    }

    private boolean unvalidated() {
        return tourRow != null && !tourRow.getBoolean("expenses_validated");
    }

    private String getPrice(float price){
        return price + " " + currencyCode;
    }

    private Pair<Float, Float> getTotalExpenses() {
        String tourId = tourRow != null? tourRow.getString(Col.SERVER_ID) : null;
        return models.paymentModel.getTotalExpenses(tourId);
    }

    private void loadPayments(){
        rows.clear();
        String selection = " customer_id = ? and tour_id = ? ";
        String[] args = {"false", tourRow.getString(Col.SERVER_ID)};
        for(DataRow paymentRow : models.paymentModel.getRows(selection, args)){
            paymentRow.putRel("user", currentUserRow);
            rows.add(paymentRow);
        }
    }

    @Override
    public void requestCreateExpense() {
        if(canCreateExpense()){
            view.openExpenseActivity();
        }else {
            //todo
        }
    }

    private boolean canCreateExpense(){
        //todo add can create expense condition
        return true;
    }

    @Override
    public void onItemClick(int position) {
        DataRow row = rows.get(position);
        view.requestOpenDetails(row.getString(Col.SERVER_ID), unvalidated());
    }

    @Override
    public void onItemLongClick(int position) {
        DataRow row = rows.get(position);
        if(isEditable && unvalidated() && !row.getString("state").equals(PaymentModel.STATE_EXPENSES_DONE)) {
            final Pair<Float, Float> totalExpenses = getTotalExpenses();
            float total = totalExpenses.second + row.getFloat("amount");
            float limit = getExpensesLimit();
            if (total > limit) {
                view.showSimpleDialog(getString(R.string.limit_exceeded_title), getString(R.string.expenses_limit_exceeded_msg));
            } else {
                view.requestValidateItem((IExpensesPresenter.ValidateDialogListener) () -> {
                    Values values = new Values();
                    values.put("state", PaymentModel.STATE_EXPENSES_DONE);
                    models.paymentModel.update(row.getString(Col.SERVER_ID), values);
                    onRefresh();
                });
            }
        }
    }

    private float getExpensesLimit(){
        return tourRow != null? tourRow.getFloat("expenses_limit") : 0.0f;
    }

    @Override
    public void requestValidateExpenses() {
        Pair<Float, Float> totalAmounts = getTotalExpenses();
        String msg = getString(R.string.validate_expenses_dialog_msg);
        String valuesMsg = getPrice(totalAmounts.second) + " " + getString(R.string.of_label) + " " + getPrice(totalAmounts.first);
        msg = msg + "\n" + valuesMsg;
        view.showConfirmationDialog(getString(R.string.validate_expenses_titla), msg);
    }

    @Override
    public void validateExpenses() {
        Values values = new Values();
        values.put("expenses_validated", 1);
        models.tourModel.update(tourRow.getString(Col.SERVER_ID), values);
        initData();
        onRefresh();
    }

    @Override
    public void setTourId(String tourId) {
        this.tourId = tourId;
    }

    @Override
    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
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
