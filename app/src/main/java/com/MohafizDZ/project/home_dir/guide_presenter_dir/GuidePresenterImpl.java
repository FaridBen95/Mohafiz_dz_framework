package com.MohafizDZ.project.home_dir.guide_presenter_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.CompanyModel;
import com.MohafizDZ.project.models.CompanyUserModel;
import com.MohafizDZ.project.models.DistributorConfigurationModel;
import com.MohafizDZ.project.models.TourModel;
import com.MohafizDZ.project.models.TourVisitModel;

import java.util.List;

public class GuidePresenterImpl implements IGuidePresenter.Presenter{
    private static final String TAG = GuidePresenterImpl.class.getSimpleName();

    private final Context context;
    private final IGuidePresenter.View view;
    private final Models models;
    private DataRow tourRow;
    private DataRow companyRow, distributorRow, plannerRow;
    private final DataRow currentUserRow;
    private IGuideDetailsStrategy currentGuideDetail;
    private boolean guideDetailsVisible = true;
    private boolean guideDetailsVisibilityLocked;

    public GuidePresenterImpl(Context context, IGuidePresenter.View view, DataRow currentUserRow) {
        this.context = context;
        this.view = view;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
    }

    @Override
    public void onViewCreated() {
        view.toggleGuideContainer(false);
        onRefresh();
    }

    private boolean initData(){
        prepareCompanyRow();
        preparePlannerRow();
        if(plannerRow == null){
            view.showSimpleDialog(getString(R.string.contact_admin_title), getString(R.string.planners_not_configured_msg));
        }
        prepareDistributorRow();
        if(distributorRow == null){
            view.showSimpleDialog(getString(R.string.contact_admin_title), getString(R.string.account_not_configured_msg));
            setGuideDetails(new PendingAccountIGuideDetails(this, context, models));
            return false;
        }
        view.toggleGuideContainer(true);
        prepareCurrentTour();
        return true;
    }

    private String getString(int resId){
        return context.getString(resId);
    }

    private void prepareCompanyRow(){
        companyRow = CompanyModel.getCurrentCompany(context);
    }

    private void preparePlannerRow() {
        plannerRow = distributorRow != null?
                models.plannerModel.browse(" id = ? ", new String[]{distributorRow.getString("planner_id")}):
                null;
        if(plannerRow == null){
            List<DataRow> planners = models.plannerModel.getRows();
            plannerRow = planners.size() > 0? planners.get(0) : null;
        }
    }

    private void setGuideDetails(IGuideDetailsStrategy guideDetailsStrategy){
        try {
            if(!currentGuideDetail.getTag().equals(guideDetailsStrategy.getTag())){
                guideDetailsVisible = false;
                requestToggleGuideDetails();
            }
        }catch (Exception ignored){}
        currentGuideDetail = guideDetailsStrategy;
        view.toggleGuideContainer(guideDetailsStrategy != null);
        if(guideDetailsStrategy != null) {
            guideDetailsStrategy.onViewCreated(guideDetailsVisible);
            view.toggleGuideButton(guideDetailsStrategy.setButtonVisibility(), guideDetailsStrategy.setButtonTitle());
            view.toggleGuideStepTitle(guideDetailsStrategy.setStepVisibility(), guideDetailsStrategy.setStepTitle());
            view.toggleGuideTitle(guideDetailsStrategy.setDescription());
            view.setImage(guideDetailsStrategy.setImageDrawable());
        }
    }

    private void prepareDistributorRow(){
        distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
    }

    private void prepareCurrentTour(){
        tourRow = models.tourModel.getCurrentTour(distributorRow);
    }

    @Override
    public void onRefresh() {
        String lastTourId = tourRow != null? tourRow.getString(Col.SERVER_ID) : "";
        String lastTourState = tourRow != null? tourRow.getString("state") : "";
        if(initData()){
            String currentTourId = tourRow != null? tourRow.getString(Col.SERVER_ID) : "";
            String currentTourState = tourRow != null? tourRow.getString("state") : "";
            boolean hasUpdated = !lastTourId.equals(currentTourId) || !lastTourState.equals(currentTourState);
            if(tourRow != null){
                view.toggleTourProgressContainer(false);
                view.togglePreClosingContainer(false);
                String state = tourRow.getString("state");
                if(state.equals(TourModel.STATE_DRAFT)){
                    setGuideDetails(new TourPlaningGuideDetails(this, context, models));
                }else if(state.equals(TourModel.STATE_CONFIRMED)){
                    setGuideDetails(new TourReadyGuideDetails(this, context, models));
                }else if(state.equals(TourModel.STATE_PROGRESS)){
                    if(hasVisitInProgress()) {
                        view.toggleTourProgressContainer(true);
                        setGuideDetails(new ProgressVisitGuideDetails(this, context, models));
                    }else{
                        view.toggleTourProgressContainer(true);
                        setGuideDetails(new TourProgressGuideDetails(this, context, models));
                    }
                }else if(state.equals(TourModel.STATE_PRE_CLOSING)){
                    view.togglePreClosingContainer(true);
                    setGuideDetails(new PreClosingGuideDetails(this, context, models));
                }else if(state.equals(TourModel.STATE_CLOSING)){
                    setGuideDetails(new ClosingGuideDetails(this, context, models));
                }
            }else{
                setGuideDetails(new TourPlaningGuideDetails(this, context, models));
//                setGuideDetails(null);
//                view.showSimpleDialog(null, getString(R.string.error_occurred));
            }
            if(hasUpdated){
                view.requestRefreshPresenters();
            }
        }
    }

    private boolean hasVisitInProgress() {
        if(tourRow.getString("current_customer_id").equals("") ||
                tourRow.getString("current_customer_id").equals("false")){
            return false;
        }
        DataRow visitRow = models.tourVisitModel.getCurrentVisit(tourRow.getString(Col.SERVER_ID), tourRow.getString("current_customer_id"));
        return visitRow.getString("state").equals(TourVisitModel.STATE_PROGRESS);
    }

    @Override
    public void lockGuideDetailsVisibility(boolean locked) {
        guideDetailsVisibilityLocked = locked;
    }

    @Override
    public void requestToggleGuideDetails() {
        if(canToggleGuideDetails()) {
            if (guideDetailsVisible) {
                view.updateToggleButtonIcon(gun0912.tedimagepicker.R.drawable.ic_arrow_drop_down_black_24dp);
            } else {
                view.updateToggleButtonIcon(gun0912.tedimagepicker.R.drawable.ic_arrow_drop_up_black_24dp);
            }
            guideDetailsVisible = !guideDetailsVisible;
            view.toggleGuideDetailsContainer(guideDetailsVisible);
        }
    }

    @Override
    public boolean canToggleGuideDetails() {
        return !guideDetailsVisibilityLocked;
    }

    @Override
    public void onActionButtonClicked() {
        if(currentGuideDetail != null){
            currentGuideDetail.onClickOnAction();
        }
    }

    @Override
    public boolean hasPhoneCallPermission() {
        return view.checkPhoneCallPermission();
    }

    @Override
    public void requestPhoneCallPermission() {
        view.requestPhonePermission();
    }

    @Override
    public String getSupportPhoneNumber() {
        return plannerRow != null? plannerRow.getString("phone_number"):
                companyRow.getString("support_phone_num");
    }

    @Override
    public void requestOpenTourForm() {
        view.openTourForm();
    }

    @Override
    public void startTour() {
        models.tourModel.startTour(tourRow);
    }

    @Override
    public void endTour() {
        models.tourModel.endTour(tourRow);
        onRefresh();
    }

    @Override
    public void reopenTour() {
        models.tourModel.reopenTour(tourRow);
        onRefresh();
    }

    @Override
    public void closeTour() {
        models.tourModel.closeTour(tourRow);
        onRefresh();
    }

    @Override
    public void validateClosingTour() {
        models.tourModel.validateClosingTour(tourRow);
        onRefresh();
    }

    @Override
    public boolean canStartTour() {
        return tourRow.getBoolean("initial_stock_validated");
    }

    @Override
    public void requestOpenInitialStock() {
        view.requestOpenInitialStock();
    }

    @Override
    public boolean blockTourPlaning() {
        return !CompanyUserModel.isAdmin(context, currentUserRow) &&
                DistributorConfigurationModel.blockTourPlaning(distributorRow.getRelArray(models.distributorModel, "configurations"));
    }

    @Override
    public void requestScanForPlan() {
        view.requestPlanScan();
    }

    @Override
    public void requestStopVisit() {
        view.requestCloseVisit();
    }

    @Override
    public boolean canEndTour() {
        return (!tourRow.getBoolean("use_cash_box") || tourRow.getBoolean("cash_box_validated"))
                && tourRow.getBoolean("sales_validated")
                && tourRow.getBoolean("inventory_validated");
    }

    @Override
    public boolean canReopenTour() {
        return tourRow != null && tourRow.getString("state").equals(TourModel.STATE_PRE_CLOSING) &&
                !tourRow.getBoolean("cash_box_validated") && !tourRow.getBoolean("expenses_validated")
                && !tourRow.getBoolean("sales_validated") && !tourRow.getBoolean("inventory_validated");
    }
}
