package com.MohafizDZ.project.customer_details_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.CompanyCustomerModel;
import com.MohafizDZ.project.models.CompanyModel;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.PaymentModel;
import com.MohafizDZ.project.models.TourModel;
import com.MohafizDZ.project.models.TourVisitActionModel;
import com.MohafizDZ.project.models.TourVisitModel;
import com.MohafizDZ.project.models.VisitOrderModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerVisitPresenterImpl implements ICustomerVisitPresenter.Presenter{
    private static final String TAG = CustomerVisitPresenterImpl.class.getSimpleName();

    private final ICustomerVisitPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final String customerId;
    private final Models models;
    private final String currency;
    private final List<DataRow> actions;
    private DataRow distributorRow, tourRow, visitRow;
    private String tourId;

    public CustomerVisitPresenterImpl(ICustomerVisitPresenter.View view, Context context, DataRow currentUserRow, String customerId) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.customerId = customerId;
        this.models = new Models(context);
        this.currency = CompanyModel.getCompanyCurrency(context);
        actions = new ArrayList<>();
    }

    @Override
    public void onViewCreated() {
        initData();
        onRefresh();
    }

    private void initData(){
        distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        tourRow = tourId != null? models.tourModel.browse(tourId) : models.tourModel.getCurrentTour(distributorRow);
        visitRow = models.visitModel.getCurrentVisit(tourRow.getString(Col.SERVER_ID), customerId);
    }

    @Override
    public void onRefresh() {
        refreshActions();
        view.setVisitDuration(getVisitDuration());
        view.setVisitNetAmount(getNetAmount());
        view.setPaymentsAmount(getPaymentsAmount());
    }

    private String getVisitDuration(){
        if(visitRow != null){
            String selection = " visit_id = ? and action in (?, ?, ?) ";
            String[] args = {visitRow.getString(Col.SERVER_ID), TourVisitActionModel.ACTION_VISIT_RESTART, TourVisitActionModel.ACTION_VISIT_START, TourVisitActionModel.ACTION_VISIT_STOP};
            List<DataRow> actions = models.actionModel.getRows(selection, args);
            long totalDuration = 0;
            for(DataRow action : actions){
                String actionName = action.getString("action");
                String actionDate = action.getString("action_date");
                int sign = actionName.equals(TourVisitActionModel.ACTION_VISIT_STOP)? 1 : -1;
                totalDuration += sign * MyUtil.getDateFromStringDate(actionDate).getTime();
            }
            totalDuration = totalDuration / 1000 / 60;
            return totalDuration + " " + getString(R.string.min_label);
        }else{
            return "-";
        }
    }

    private String getNetAmount() {
        float netAmount = 0.0f;
        if(visitRow != null) {
            String selection = " visit_id = ? and state <> ? and total_amount > 0 ";
            String[] args = {visitRow.getString(Col.SERVER_ID), VisitOrderModel.ORDER_STATE_CANCEL};
            for (DataRow row : models.orderModel.getRows(selection, args)) {
                netAmount += row.getFloat("total_amount");
            }
        }
        return getPrice(netAmount);
    }

    private String getPaymentsAmount(){
        float total = 0.0f;
        if(visitRow != null){
            String selection = " visit_id = ? ";
            String[] args = {visitRow.getString(Col.SERVER_ID)};
            for(DataRow row : models.paymentModel.getRows(selection, args)){
                total += row.getFloat("amount");
            }
        }
        return getPrice(total);
    }

    private String getPrice(float price){
        return price + " " + currency;
    }

    private String getString(int resId){
        return context.getString(resId);
    }

    private void refreshActions(){
        clearAction();
        if(visitRow != null) {
            String selection = " visit_id = ? ";
            String[] args = {visitRow.getString(Col.SERVER_ID)};
            actions.addAll(models.actionModel.getRows(selection, args));
        }
        Map<String, String> translationMap = new HashMap<>();
        for(int i = 0; i < actions.size(); i++){
            DataRow action = actions.get(i);
            String actionName = action.getString("action");
            if(!translationMap.containsKey(actionName)){
                translationMap.put(actionName, models.actionModel.getAction(actionName));
            }
            actionName = translationMap.get(actionName);
            view.createActionChip(actionName, i);
        }
    }

    private void clearAction() {
        actions.clear();
        view.clearChipGroup();
    }

    @Override
    public void setTourId(String tourId) {
        this.tourId = tourId;
    }

    @Override
    public void onChipClicked(int position) {
        DataRow actionRow = actions.get(position);
        view.openActionView(actionRow.getString(Col.SERVER_ID));
    }

    private static class Models{
        private final CompanyCustomerModel customerModel;
        private final DistributorModel distributorModel;
        private final TourModel tourModel;
        private final TourVisitActionModel actionModel;
        private final TourVisitModel visitModel;
        private final VisitOrderModel orderModel;
        private final PaymentModel paymentModel;
        public Models(Context context) {
            this.customerModel = new CompanyCustomerModel(context);
            this.distributorModel = new DistributorModel(context);
            this.tourModel = new TourModel(context);
            this.actionModel = new TourVisitActionModel(context);
            this.visitModel = new TourVisitModel(context);
            this.orderModel = new VisitOrderModel(context);
            this.paymentModel = new PaymentModel(context);
        }

    }
}
