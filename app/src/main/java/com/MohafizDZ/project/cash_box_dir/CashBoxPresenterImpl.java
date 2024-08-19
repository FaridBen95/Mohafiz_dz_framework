package com.MohafizDZ.project.cash_box_dir;

import android.content.Context;
import android.text.TextUtils;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.CashBoxLinesModel;
import com.MohafizDZ.project.models.CashBoxModel;
import com.MohafizDZ.project.models.CashDenominationModel;
import com.MohafizDZ.project.models.CompanyModel;
import com.MohafizDZ.project.models.DistributorConfigurationModel;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.PaymentModel;
import com.MohafizDZ.project.models.TourConfigurationModel;
import com.MohafizDZ.project.models.TourModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class CashBoxPresenterImpl implements ICashBoxPresenter.Presenter{
    private final String TAG = CashBoxPresenterImpl.class.getSimpleName();

    private final ICashBoxPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private final List<DataRow> rows;
    private final List<String> configurations;
    private final String currency;
    private DataRow tourRow, distributorRow, cashBoxRow;
    private boolean detailsVisible = true;
    private String tourId;
    private boolean isEditable;

    public CashBoxPresenterImpl(ICashBoxPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
        this.currency = CompanyModel.getCompanyCurrency(context);
        this.rows = new ArrayList<>();
        this.configurations = new ArrayList<>();
    }

    private String getString(int resId){
        return context.getString(resId);
    }

    @Override
    public void onViewCreated() {
        initData();
        view.setName(cashBoxRow.getString("name"));
        view.setToolbarTitle(getString(R.string.cash_box_label));
        view.initAdapter(rows, !unvalidated());
        onRefresh();
    }

    private void prepareDetailsView() {
        String selection = " tour_id = ? and state <> ? ";
        String[] args = {tourRow.getString(Col.SERVER_ID), PaymentModel.STATE_CANCEL};
        Calculator calculator = new Calculator().calculate(models.paymentModel.getRows(selection, args));
        view.setTotalPayments(getPrice(calculator.payments));
        view.setTotalRefunds(getPrice(calculator.refunds));
        view.setTotalExpenses(getPrice(calculator.expenses));
        view.setTotal(getPrice(calculator.total));
    }

    private boolean canShowDetails(){
        return TourConfigurationModel.showCashBoxDetails(configurations) ||
                cashBoxRow.getString("state").equals(PaymentModel.STATE_DONE);
    }

    private void initData(){
        this.distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        this.tourRow = tourId != null? models.tourModel.browse(tourId) : models.tourModel.getCurrentTour(distributorRow);
        this.cashBoxRow = models.cashBoxModel.getCurrentCashBox(tourRow);
        configurations.clear();
        configurations.addAll(tourRow.getRelArray(models.tourModel, "configurations"));
        configurations.addAll(distributorRow.getRelArray(models.distributorModel, "configurations"));
    }

    @Override
    public void onRefresh() {
        boolean canShowDetails = canShowDetails();
        if(canShowDetails){
            prepareDetailsView();
        }
        view.toggleCashBoxDetails(canShowDetails);
        requestToggleDetails();
        if(!unvalidated()){
            String title = getString(R.string.cash_box_label) + " (" + getString(R.string.validated_label) + ")";
            view.setToolbarTitle(title);
            view.showValidateDate(cashBoxRow.getString("validate_date"));
        }
        loadLines();
        view.onLoadFinished(rows, !unvalidated());
        view.toggleAddButton(unvalidated() && isEditable);
        view.toggleValidateButton(unvalidated() && isEditable);
        view.setCashBoxTotal(getPrice(getTotal()));
    }

    private String getPrice(float price){
        return Math.abs(price) + " " + currency;
    }

    private float getTotal(){
        float total = 0.0f;
        for(DataRow row : rows){
            total += totalPerDenomination(row);
        }
        return total;
    }

    private float totalPerDenomination(DataRow row){
        return row.getFloat("denomination_value") * row.getFloat("count");
    }

    private boolean unvalidated(){
        return !cashBoxRow.getString("state").equals(CashBoxModel.STATE_DONE);
    }

    private void loadLines(){
        String selection = " cash_box_id = ? ";
        String[] args = {cashBoxRow.getString(Col.SERVER_ID)};
        rows.clear();
        for(DataRow row : models.cashBoxLinesModel.getRows(selection, args)){
            row.put("total", totalPerDenomination(row));
            rows.add(row);
        }
    }

    @Override
    public void onItemClick(int position) {
        if(unvalidated() && isEditable) {
            final LinkedHashMap<String, String> denominations = getDenominations();
            DataRow row = rows.get(position);
            int denominationValue = row.getInteger("denomination_value");
            int count = row.getInteger("count");
            view.showLineCreationDialog(denominations, denominationValue, count, true);
        }
    }

    @Override
    public void onAddClicked() {
        final LinkedHashMap<String, String> denominations = getDenominations();
        view.showLineCreationDialog(denominations, null, null, false);
    }

    private LinkedHashMap<String, String> getDenominations(){
        List<DataRow> categories = models.denominationLocalModel.getRows();
        return getNamesFromRows(categories);
    }

    private LinkedHashMap<String, String> getNamesFromRows(List<DataRow> rows) {
        LinkedHashMap<String, String> list = new LinkedHashMap<>();
        for(DataRow row : rows){
            list.put(row.getString(Col.SERVER_ID), row.getString("name"));
        }
        return list;
    }

    @Override
    public void onValidate() {
        Values values = new Values();
        values.put("state", CashBoxModel.STATE_DONE);
        values.put("validate_date", MyUtil.getCurrentDate());
        models.cashBoxModel.update(cashBoxRow.getString(Col.SERVER_ID), values);
        Values tourValues = new Values();
        tourValues.put("cash_box_validated", 1);
        models.tourModel.update(tourRow.getString(Col.SERVER_ID), tourValues);
        view.goBack();
    }

    @Override
    public void requestToggleDetails() {
        if(detailsVisible){
            view.updateToggleButtonIcon(gun0912.tedimagepicker.R.drawable.ic_arrow_drop_down_black_24dp);
        }else{
            view.updateToggleButtonIcon(gun0912.tedimagepicker.R.drawable.ic_arrow_drop_up_black_24dp);
        }
        detailsVisible = !detailsVisible;
        view.toggleCashBoxDetailsContainer(detailsVisible);
    }

    @Override
    public void createOrUpdateLine(boolean updating, String denomination, String count) {
        if(TextUtils.isEmpty(denomination) || Integer.valueOf(denomination) == 0){
            view.showToast(getString(R.string.denomation_required));
            return;
        }
        if(TextUtils.isEmpty(count) || Integer.valueOf(count) == 0){
            view.showToast(getString(R.string.count_required));
            return;
        }
        createDenomination(denomination);
        models.cashBoxLinesModel.createLine(updating, denomination, Integer.valueOf(count), cashBoxRow, currentUserRow);
        onRefresh();
    }

    @Override
    public void deleteLine(Integer denominationValue) {
        String selection = " cash_box_id = ? and denomination = ? ";
        String[] args = {cashBoxRow.getString(Col.SERVER_ID), denominationValue + ""};
        models.cashBoxLinesModel.delete(selection, args, true);
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

    private boolean createDenomination(String denomination){
        if(!hasDenomination(denomination)){
            if(canCreateDenomination()) {
                Values values = new Values();
                values.put("name", denomination);
                values.put("creator_id", currentUserRow.getString(Col.SERVER_ID));
                models.denominationLocalModel.insert(values);
                return true;
            }else{
                view.showToast(getString(R.string.choose_valid_denomination_msg));
                return false;
            }
        }
        return true;
    }

    private boolean canCreateDenomination() {
        return DistributorConfigurationModel.canCreateDenomination(configurations);
    }

    private boolean hasDenomination(String denomination){
        String selection = " name = ? ";
        String[] args = {denomination };
        return models.denominationLocalModel.browse(selection, args) != null;
    }

    private static class Models{
        private final TourModel tourModel;
        private final DistributorModel distributorModel;
        private final PaymentModel paymentModel;
        private final CashBoxModel cashBoxModel;
        private final CashBoxLinesModel cashBoxLinesModel;
        private final CashDenominationModel denominationLocalModel;

        private Models(Context context){
            this.tourModel = new TourModel(context);
            this.distributorModel = new DistributorModel(context);
            this.paymentModel = new PaymentModel(context);
            this.cashBoxModel = new CashBoxModel(context);
            this.cashBoxLinesModel = new CashBoxLinesModel(context);
            this.denominationLocalModel = new CashDenominationModel(context);
        }
    }

    private static class Calculator{
        private float payments;
        private float refunds;
        private float expenses;
        private float total;

        private Calculator(){

        }

        private Calculator calculate(List<DataRow> paymentLines){
            expenses = 0;
            payments = 0;
            refunds = 0;
            for(DataRow row : paymentLines){
                String state = row.getString("state");
                float amount = row.getFloat("amount");
                if(state.equals(PaymentModel.STATE_EXPENSES_DONE)){
                    expenses -= Math.abs(amount);
                    total -= Math.abs(amount);
                }else if(state.equals(PaymentModel.STATE_DONE)){
                    if(amount > 0){
                        payments += amount;
                    }else{
                        refunds += amount;
                    }
                    total += amount;
                }
            }
            return this;
        }
    }
}
