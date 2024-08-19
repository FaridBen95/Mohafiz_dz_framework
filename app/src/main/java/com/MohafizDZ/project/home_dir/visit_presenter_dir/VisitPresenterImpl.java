package com.MohafizDZ.project.home_dir.visit_presenter_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.catalog_dir.models.CartItemSingleton;
import com.MohafizDZ.project.catalog_strategies.BackOrderCatalogStrategy;
import com.MohafizDZ.project.catalog_strategies.OrderCatalogStrategy;
import com.MohafizDZ.project.models.CompanyCustomerModel;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.TourConfigurationModel;
import com.MohafizDZ.project.models.TourModel;
import com.MohafizDZ.project.models.TourVisitActionModel;
import com.MohafizDZ.project.models.TourVisitModel;
import com.MohafizDZ.project.payment_dir.strategies.PaymentStrategy;
import com.MohafizDZ.project.payment_dir.strategies.RefundStrategy;

import java.util.ArrayList;
import java.util.List;

public class VisitPresenterImpl implements IVisitPresenter.Presenter{
    private static final String TAG = VisitPresenterImpl.class.getSimpleName();

    private final IVisitPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private DataRow distributorRow, tourRow, currentCustomerRow, visitRow;
    private boolean visitInProgress;
    private final List<String> configurations;

    public VisitPresenterImpl(IVisitPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
        configurations = new ArrayList<>();
    }

    @Override
    public void onViewCreated() {
        initData();
        onRefresh();
    }

    private void initData(){
        this.distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        this.tourRow = models.tourModel.getCurrentTour(distributorRow);
        if(tourRow != null) {
            configurations.clear();
            configurations.addAll(tourRow.getRelArray(models.tourModel, "configurations"));
        }
    }

    @Override
    public void onRefresh() {
        initData();
        initCurrentCustomer();
        prepareCustomerView();
        prepareVisitView();
        view.toggleGoalButton(hasGoal());
    }

    private boolean hasGoal(){
        return tourRow != null && !tourRow.getString("goal_text").equals("");
    }

    private void prepareProgressView(){
        if(tourRow != null) {
            int visitedCount = models.tourVisitModel.count(" tour_id = ? and state <> ? ",
                    new String[]{tourRow.getString(Col.SERVER_ID), TourVisitModel.STATE_DRAFT});
            //todo uncomment this when adding visit planing
            int plannedVisitsCount = tourRow.getInteger("visits_goal_count");
            plannedVisitsCount = plannedVisitsCount == 0 ? visitedCount : plannedVisitsCount;
//        int plannedVisitsCount = models.tourVisitModel.count(" tour_id = ? and (planned = 1 or state <> ?) ",
//                new String[]{tourRow.getString(Col.SERVER_ID), TourVisitModel.STATE_DRAFT});
            view.setVisitsProgress(visitedCount, plannedVisitsCount);
        }
    }

    private void initCurrentCustomer(){
        this.currentCustomerRow = tourRow != null? models.companyCustomerModel.browse(tourRow.getString("current_customer_id")): null;
    }

    private void prepareCustomerView(){
        if(currentCustomerRow != null){
            view.toggleCustomerDetails(true);
            view.setCustomerName(currentCustomerRow.getString("name"));
            view.setCustomerImage(currentCustomerRow.getString("picture_low"));
        }else{
            view.toggleCustomerDetails(false);
        }
    }

    private void prepareVisitView(){
        if(currentCustomerRow != null) {
            this.visitRow = models.tourVisitModel.getCurrentVisit(tourRow.getString(Col.SERVER_ID), currentCustomerRow.getString(Col.SERVER_ID));
            visitInProgress = false;
            if (visitRow.getString("state").equals(TourVisitModel.STATE_DRAFT)) {
                view.toggleStartVisit(true);
                view.toggleStopVisit(false);
                view.toggleRestartVisit(false);
            } else if (visitRow.getString("state").equals(TourVisitModel.STATE_PROGRESS)) {
                visitInProgress = true;
                view.toggleStartVisit(false);
                view.toggleStopVisit(true);
                view.toggleRestartVisit(false);
            } else {
                view.toggleStartVisit(false);
                view.toggleStopVisit(false);
                view.toggleRestartVisit(true);
            }
            view.toggleCustomersListButton(!visitInProgress);
            prepareActionsContainer(true);
        }else{
            prepareActionsContainer(false);
            view.toggleCustomersListButton(true);
            view.toggleStartVisit(false);
            view.toggleStopVisit(false);
            view.toggleRestartVisit(false);
        }
        prepareProgressView();
    }

    private void prepareActionsContainer(boolean allowed){
        view.checkSale(hasAction(TourVisitActionModel.ACTION_SALE));
        view.checkBackOrder(hasAction(TourVisitActionModel.ACTION_BACK_ORDER));
        view.checkNoAction(hasAction(TourVisitActionModel.ACTION_NO_ACTION));
        view.checkOtherAction(hasAction(TourVisitActionModel.ACTION_OTHER));
        view.checkPaymentAction(hasAction(TourVisitActionModel.ACTION_PAYMENT));
        view.checkRefundAction(hasAction(TourVisitActionModel.ACTION_REFUND));
        if(allowed){
            if(!visitInProgress) {
                boolean hasAction = false;
//                toggleChipViews(false);
                if (hasAction(TourVisitActionModel.ACTION_SALE)) {
                    view.toggleSaleChip(true);
                    hasAction = true;
                }
                if (hasAction(TourVisitActionModel.ACTION_BACK_ORDER)) {
                    view.toggleBackOrderChip(true);
                    hasAction = true;
                }
                if (hasAction(TourVisitActionModel.ACTION_NO_ACTION)) {
                    view.toggleNoActionChip(true);
                    hasAction = true;
                }
                if (hasAction(TourVisitActionModel.ACTION_OTHER)) {
                    view.toggleOtherChip(true);
                    hasAction = true;
                }
                if (hasAction(TourVisitActionModel.ACTION_PAYMENT)) {
                    view.togglePaymentChip(true);
                    hasAction = true;
                }
                if (hasAction(TourVisitActionModel.ACTION_REFUND)) {
                    view.toggleRefundChip(true);
                }
                view.toggleActionsContainer(true);
            }else{
//                toggleChipViews(true);
                view.toggleActionsContainer(true);
            }
        }else{
            view.toggleActionsContainer(false);
        }
    }

    private void toggleChipViews(boolean visible){
        view.toggleSaleChip(visible);
        view.toggleRefundChip(visible);
        view.togglePaymentChip(visible);
        view.toggleOtherChip(visible);
        view.toggleNoActionChip(visible);
        view.toggleBackOrderChip(visible);
    }

    private boolean hasAction(String action){
        String visitId = visitRow != null? visitRow.getString(Col.SERVER_ID) : null;
        return visitId != null && models.tourVisitActionModel.hasAction(visitId, action);
    }

    @Override
    public void onSelectCustomer(String customerId) {
        if(tourRow != null){
            Values values = new Values();
            values.put("current_customer_id", customerId);
            //todo when dealing with delay sync i should make sure to not update _updated based on local columns
            models.tourModel.update(tourRow.getString(Col.SERVER_ID), values);
            onRefresh();
        }
    }

    @Override
    public void requestCustomerDetails() {
        if(currentCustomerRow != null) {
            view.openCustomerDetails(currentCustomerRow.getString(Col.SERVER_ID));
        }else{
            view.showToast(getString(R.string.error_occurred));
        }
    }

    @Override
    public void onStartVisitClicked() {
        view.requestCurrentLocation(new IVisitPresenter.LocationListener() {
            @Override
            public void onLocationChanged(double latitude, double longitude) {
                view.toggleLoading(false);
                if(models.tourVisitModel.startVisit(visitRow, currentCustomerRow, latitude, longitude)){
                    prepareVisitView();
                }else{
                    view.showToast(getString(R.string.error_occurred));
                }
            }

            @Override
            public void onStart() {
                view.toggleLoading(true);
            }

            @Override
            public void onFailed() {
                view.toggleLoading(false);
            }
        });
    }

    @Override
    public void onStopVisitClicked() {
        if(forceVisitAction() && !visitHasAction()){
            view.showSimpleDialog(getString(R.string.no_action_label), getString(R.string.require_visit_actino_msg));
            return;
        }
        CartItemSingleton.getInstance().clearItems();
        view.requestCurrentLocation(new IVisitPresenter.LocationListener() {
            @Override
            public void onLocationChanged(double latitude, double longitude) {
                view.toggleLoading(false);
                if(models.tourVisitModel.stopVisit(visitRow, currentCustomerRow, latitude, longitude)){
                    prepareVisitView();
                }else{
                    view.showToast(getString(R.string.error_occurred));
                }
            }

            @Override
            public void onStart() {
                view.toggleLoading(true);
            }

            @Override
            public void onFailed() {
                view.toggleLoading(false);
            }
        });
    }

    private boolean visitHasAction() {
        return visitRow != null && models.tourVisitActionModel.hasVisitAction(visitRow.getString(Col.SERVER_ID));
    }

    private boolean forceVisitAction() {
        return TourConfigurationModel.forceVisitAction(configurations);
    }

    @Override
    public void onRestartClicked() {
        view.requestCurrentLocation(new IVisitPresenter.LocationListener() {
            @Override
            public void onLocationChanged(double latitude, double longitude) {
                view.toggleLoading(false);
                if(models.tourVisitModel.restartVisit(visitRow, currentCustomerRow, latitude, longitude)){
                    prepareVisitView();
                }else{
                    view.showToast(getString(R.string.error_occurred));
                }
            }

            @Override
            public void onStart() {
                view.toggleLoading(true);
            }

            @Override
            public void onFailed() {
                view.toggleLoading(false);
            }
        });
    }

    @Override
    public void requestOpenSale() {
        //todo check wither sale is already made then don't allow for another one
        if(!visitInProgress){
            view.showToast(getString(R.string.start_visit_msg));
            return;
        }
        view.openOrderCatalog(currentCustomerRow.getString(Col.SERVER_ID), OrderCatalogStrategy.class.getName());
    }

    @Override
    public void requestOpenBackOrder() {
        if(!visitInProgress){
            view.showToast(getString(R.string.start_visit_msg));
            return;
        }
        view.openOrderCatalog(currentCustomerRow.getString(Col.SERVER_ID), BackOrderCatalogStrategy.class.getName());
    }

    @Override
    public void requestOpenNoAction() {
        if(hasAction(TourVisitActionModel.ACTION_NO_ACTION)) {
            view.openNoActionDetails(getNoActionId());
        }else{
            view.openNoActionForm(TourVisitActionModel.ACTION_NO_ACTION, currentCustomerRow.getString(Col.SERVER_ID));
        }
    }

    @Override
    public void requestOpenOtherAction() {
        if(!visitInProgress){
            view.showToast(getString(R.string.start_visit_msg));
            return;
        }
        view.openNoActionForm(TourVisitActionModel.ACTION_OTHER, currentCustomerRow.getString(Col.SERVER_ID));
    }

    @Override
    public void requestOpenPaymentAction(boolean isRefund) {
        if(!visitInProgress){
            view.showToast(getString(R.string.start_visit_msg));
            return;
        }
        if(!isRefund){
            view.openPaymentActivity(PaymentStrategy.class.getName(), currentCustomerRow.getString(Col.SERVER_ID));
        }else{
            view.openPaymentActivity(RefundStrategy.class.getName(), currentCustomerRow.getString(Col.SERVER_ID));
        }
    }

    @Override
    public void requestOpenOtherActionsList() {
        String tourId = tourRow != null? tourRow.getString(Col.SERVER_ID) : null;
        String customerId = currentCustomerRow != null? currentCustomerRow.getString(Col.SERVER_ID) : null;
        String actionName = TourVisitActionModel.ACTION_OTHER;
        view.openActionsList(tourId, customerId, actionName);
    }

    @Override
    public void requestOpenNoActionsList() {
        String tourId = tourRow != null? tourRow.getString(Col.SERVER_ID) : null;
        String customerId = currentCustomerRow != null? currentCustomerRow.getString(Col.SERVER_ID) : null;
        String actionName = TourVisitActionModel.ACTION_NO_ACTION;
        view.openActionsList(tourId, customerId, actionName);
    }

    @Override
    public void
    requestOpenActionsList() {
        String tourId = tourRow != null? tourRow.getString(Col.SERVER_ID) : null;
        String customerId = currentCustomerRow != null? currentCustomerRow.getString(Col.SERVER_ID) : null;
        view.openActionsList(tourId, customerId, null);
    }

    @Override
    public void requestOpenCustomersList() {
        String tourId = tourRow != null? tourRow.getString(Col.SERVER_ID) : null;
        view.openCustomersList(tourId);
    }

    @Override
    public void requestShowGoal() {
        view.showGoalSnackBar(tourRow.getString("goal_text"));
    }

    @Override
    public void requestOpenOrdersList(boolean isBackOrder) {
        String customerId = currentCustomerRow != null? currentCustomerRow.getString(Col.SERVER_ID) : null;
        String tourId = tourRow != null? tourRow.getString(Col.SERVER_ID) : null;
        view.openOrdersList(tourId, customerId, isBackOrder);
    }

    @Override
    public void requestOpenPaymentsLis(boolean isRefund) {
        String customerId = currentCustomerRow != null? currentCustomerRow.getString(Col.SERVER_ID) : null;
        String tourId = tourRow != null? tourRow.getString(Col.SERVER_ID) : null;
        view.openPaymentsList(tourId, customerId, isRefund);
    }

    private String getNoActionId(){
        String selection = " visit_id = ? and action = ? ";
        String[] args = {visitRow.getString(Col.SERVER_ID), TourVisitActionModel.ACTION_NO_ACTION};
        return models.tourVisitActionModel.browse(selection, args).getString(Col.SERVER_ID);
    }

    private String getString(int resId){
        return context.getString(resId);
    }

    private static class Models{
        private final TourModel tourModel;
        private final DistributorModel distributorModel;
        private final CompanyCustomerModel companyCustomerModel;
        private final TourVisitModel tourVisitModel;
        private final TourVisitActionModel tourVisitActionModel;

        private Models(Context context){
            this.tourModel = new TourModel(context);
            this.distributorModel = new DistributorModel(context);
            this.companyCustomerModel = new CompanyCustomerModel(context);
            this.tourVisitModel = new TourVisitModel(context);
            tourVisitActionModel = new TourVisitActionModel(context);
        }
    }
}
