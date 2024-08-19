package com.MohafizDZ.project.dashboard_dir.visit_presenter_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.TourModel;
import com.MohafizDZ.project.models.TourVisitActionModel;
import com.MohafizDZ.project.models.TourVisitModel;

public class VisitPresenterImpl implements IVisitPresenter.Presenter {
    private static final String TAG = VisitPresenterImpl.class.getSimpleName();

    private final IVisitPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private String tourId;
    private final Models models;
    private DataRow tourRow;
    private Long dateStart, dateEnd;

    public VisitPresenterImpl(IVisitPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
    }

    @Override
    public void setTourId(String tourId) {
        this.tourId = tourId;
        initData();
        onRefresh();
    }

    @Override
    public void onViewCreated() {
        initData();
        onRefresh();
    }

    private void initData(){
        this.tourRow = models.tourModel.browse(tourId);
    }

    @Override
    public void onRefresh() {
        view.toggleCustomerDetails(false);
        prepareActionsContainer();
        view.toggleCustomersListButton(true);
        view.toggleStartVisit(false);
        view.toggleStopVisit(false);
        view.toggleRestartVisit(false);
        prepareProgressView();
    }

    private void prepareActionsContainer() {
        view.toggleSaleChip(true);
        view.toggleRefundChip(true);
        view.togglePaymentChip(true);
        view.toggleOtherChip(true);
        view.toggleNoActionChip(true);
        view.toggleBackOrderChip(true);
        view.checkSale(actionsCount(TourVisitActionModel.ACTION_SALE));
        view.checkBackOrder(actionsCount(TourVisitActionModel.ACTION_BACK_ORDER));
        view.checkNoAction(actionsCount(TourVisitActionModel.ACTION_NO_ACTION));
        view.checkOtherAction(actionsCount(TourVisitActionModel.ACTION_OTHER));
        view.checkPaymentAction(actionsCount(TourVisitActionModel.ACTION_PAYMENT));
        view.checkRefundAction(actionsCount(TourVisitActionModel.ACTION_REFUND));
    }

    private int actionsCount(String action){
        String selection = " tour_id = ? and action = ? ";
        String[] args = {tourId, action};
        return tourId != null? models.actionModel.select(selection, args).size() : 0;
    }

    private void prepareProgressView(){
        if(tourRow != null) {
            int visitedCount = models.visitModel.count(" tour_id = ? and state <> ? ",
                    new String[]{tourRow.getString(Col.SERVER_ID), TourVisitModel.STATE_DRAFT});
            //todo uncomment this when adding visit planing
            int plannedVisitsCount = tourRow.getInteger("visits_goal_count");
            plannedVisitsCount = plannedVisitsCount == 0 ? visitedCount : plannedVisitsCount;
//        int plannedVisitsCount = models.tourVisitModel.count(" tour_id = ? and (planned = 1 or state <> ?) ",
//                new String[]{tourRow.getString(Col.SERVER_ID), TourVisitModel.STATE_DRAFT});
            view.setVisitsProgress(visitedCount, plannedVisitsCount);
        }else{
            view.setVisitsProgress(0, 0);
        }
    }

    @Override
    public void onSelectCustomer(String customerId) {

    }

    @Override
    public void requestCustomerDetails() {

    }

    @Override
    public void onStartVisitClicked() {

    }

    @Override
    public void onStopVisitClicked() {

    }

    @Override
    public void onRestartClicked() {

    }

    @Override
    public void requestOpenSale() {
    }

    @Override
    public void requestOpenBackOrder() {

    }

    @Override
    public void requestOpenNoAction() {

    }

    @Override
    public void requestOpenOtherAction() {

    }

    @Override
    public void requestOpenPaymentAction(boolean isRefund) {

    }

    @Override
    public void requestOpenActionsList() {
        String tourId = tourRow != null? tourRow.getString(Col.SERVER_ID) : null;
        view.requestOpenActions(tourId, null);
    }

    @Override
    public void requestShowGoal() {
        if(tourRow != null) {
            view.showGoalSnackBar(tourRow.getString("goal_text"));
        }
    }

    @Override
    public void requestOpenSales() {
        String tourId = tourRow != null? tourRow.getString(Col.SERVER_ID) : null;
        view.requestOpenSales(tourId);
    }

    @Override
    public void requestOpenBackOrders() {
        String tourId = tourRow != null? tourRow.getString(Col.SERVER_ID) : null;
        view.requestOpenBackOrders(tourId);
    }

    @Override
    public void requestOpenCustomersList() {
        String tourId = tourRow != null? tourRow.getString(Col.SERVER_ID ) : null;
        view.requestOpenCustomersList(tourId);
    }

    @Override
    public void requestOpenPayments(boolean isRefund) {
        String tourId = tourRow != null? tourRow.getString(Col.SERVER_ID ) : null;
        view.requestOpenPayments(tourId, isRefund);
    }

    @Override
    public void requestOpenNoActions() {
        String tourId = tourRow != null? tourRow.getString(Col.SERVER_ID) : null;
        view.requestOpenActions(tourId, TourVisitActionModel.ACTION_NO_ACTION);
    }

    @Override
    public void requestOpenOtherActionsList() {
        String tourId = tourRow != null? tourRow.getString(Col.SERVER_ID) : null;
        view.requestOpenActions(tourId, TourVisitActionModel.ACTION_OTHER);
    }

    private static class Models{
        private final TourModel tourModel;
        private final DistributorModel distributorModel;
        private final TourVisitModel visitModel;
        private final TourVisitActionModel actionModel;

        private Models(Context context){
            tourModel = new TourModel(context);
            this.distributorModel = new DistributorModel(context);
            this.visitModel = new TourVisitModel(context);
            this.actionModel = new TourVisitActionModel(context);
        }
    }
}
