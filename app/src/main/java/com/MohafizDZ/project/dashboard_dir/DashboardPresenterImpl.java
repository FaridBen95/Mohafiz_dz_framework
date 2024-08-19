package com.MohafizDZ.project.dashboard_dir;

import android.content.Context;

import androidx.core.util.Pair;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.dashboard_dir.models.Models;
import com.MohafizDZ.project.models.TourModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Nullable;

public class DashboardPresenterImpl implements IDashboardPresenter.Presenter{
    private static final String TAG = DashboardPresenterImpl.class.getSimpleName();

    private final IDashboardPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private final List<DataRow> tourList;
    private DataRow distributorRow;
    private @Nullable DataRow currentTourRow, selectedTourRow;
    private Long dateStart, dateEnd;
    private boolean progressContainerVisible, preClosingContainerVisible;

    public DashboardPresenterImpl(IDashboardPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        models = new Models(context);
        tourList = new ArrayList<>();
    }

    private String getString(int resId){
        return context.getString(resId);
    }

    @Override
    public void onViewCreated() {
        initData();
        view.setToolbarTitle(getString(R.string.dashboard_label));
        prepareTourList();
        onRefresh();
    }

    private void initData(){
        distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        currentTourRow = models.tourModel.getCurrentTour(distributorRow);
    }

    private void prepareTourList(){
        Selection selection = new Selection();
        selection.addSelection(" distributor_id = ? ", distributorRow.getString(Col.SERVER_ID));
        if(dateStart != null && dateEnd != null){
            String dateStartStr = MyUtil.milliSecToDate(dateStart, MyUtil.DEFAULT_DATE_FORMAT);
            String dateEndStr = MyUtil.milliSecToDate(dateEnd, MyUtil.DEFAULT_DATE_FORMAT) + " 23:59:59";
            selection.addSelection(" plan_date >= ? ", dateStartStr);
            selection.addSelection(" plan_date <= ? ", dateEndStr);
        }
        tourList.clear();
        tourList.addAll(models.tourModel.select(selection.selection, selection.args));
        selectedTourRow = null;
        if(currentTourRow != null) {
            for (DataRow row : tourList) {
                if (row.getString(Col.SERVER_ID).equals(currentTourRow.getString(Col.SERVER_ID))) {
                    selectedTourRow = currentTourRow;
                    break;
                }
            }
        }
        view.setTourList(getNamesFromRows(tourList), selectedTourRow);
    }

    private LinkedHashMap<String, String> getNamesFromRows(List<DataRow> rows) {
        LinkedHashMap<String, String> list = new LinkedHashMap<>();
        for(DataRow row : rows){
            list.put(row.getString(Col.SERVER_ID), row.getString("name"));
        }
        return list;
    }

    @Override
    public void onRefresh() {
        if(selectedTourRow != null){
            view.setToolbarTitle(selectedTourRow.getString("name"));
            view.setTourName(selectedTourRow.getString("name"));
            String vehicleName = selectedTourRow.getString("vehicle_name");
            vehicleName = vehicleName.equals("false")? "-" : vehicleName;
            view.setVehicle(vehicleName);
            view.setRegion(getRegion(selectedTourRow));
        }else{
            view.setToolbarTitle(getString(R.string.dashboard_label));
        }
        String tourId = selectedTourRow != null? selectedTourRow.getString(Col.SERVER_ID) : "";
        if(preClosingContainerVisible){
            requestTogglePreClosingContainer();
        }
        if(progressContainerVisible){
            requestToggleProgressContainer();
        }
        view.refreshVisitContainer(selectedTourRow != null, tourId);
        view.refreshPreClosingContainer(selectedTourRow != null, tourId);
        toggleBasedOnState();
    }

    private void toggleBasedOnState(){
        String state = selectedTourRow != null? selectedTourRow.getString("state") : "";
        view.toggleTourPlaning(false);
        view.toggleTourOpening(false);
        view.toggleTourProgress(false);
        view.toggleTourPreClosing(false);
        if(selectedTourRow == null){
            view.toggleTourProgress(true);
        }else {
            switch (state) {
                case TourModel.STATE_CLOSED:
                case TourModel.STATE_CLOSING:
                case TourModel.STATE_PRE_CLOSING:
                    view.toggleTourPreClosing(true);
                    view.toggleTourProgress(true);
                case TourModel.STATE_PROGRESS:
                    view.toggleTourOpening(true);
                case TourModel.STATE_CONFIRMED:
                    view.toggleTourPlaning(true);
            }
        }
    }

    private String getRegion(DataRow tourRow){
        try {
            DataRow row = models.regionModel.browse(tourRow.getString("region_id"));
            return row.getString("name");
        }catch (Exception ignored){}
        return "-";
    }

    @Override
    public void requestOpenTourPlan() {
        if(selectedTourRow != null) {
            view.openTourPlanForm(selectedTourRow.getString(Col.SERVER_ID));
        }
    }

    @Override
    public void requestOpenTourOpening() {
        if(selectedTourRow != null){
            view.openInitialStock(selectedTourRow.getString(Col.SERVER_ID));
        }
    }

    @Override
    public void requestSelectStartDate() {
        view.requestSelectDateRange(new Pair<>(null, dateEnd));
    }

    @Override
    public void requestSelectEndDate() {
        view.requestSelectDateRange(new Pair<>(dateStart, null));
    }

    @Override
    public void onSelectDateRange(Pair<Long, Long> dateRange) {
        dateStart = dateRange.first;
        dateEnd = dateRange.second;
        view.setDateStart(MyUtil.milliSecToDate(dateStart, MyUtil.DEFAULT_DATE_FORMAT));
        view.setDateEnd(MyUtil.milliSecToDate(dateEnd, MyUtil.DEFAULT_DATE_FORMAT));
        prepareTourList();
    }

    @Override
    public void requestToggleProgressContainer() {
        if (progressContainerVisible) {
            view.updateProgressToggleButtonIcon(gun0912.tedimagepicker.R.drawable.ic_arrow_drop_down_black_24dp);
        } else {
            view.updateProgressToggleButtonIcon(gun0912.tedimagepicker.R.drawable.ic_arrow_drop_up_black_24dp);
        }
        progressContainerVisible = !progressContainerVisible;
        view.toggleProgressContainer(progressContainerVisible);
    }

    @Override
    public void requestTogglePreClosingContainer() {
        if(preClosingContainerVisible){
            view.updatePreClosingToggleButtonIcon(gun0912.tedimagepicker.R.drawable.ic_arrow_drop_down_black_24dp);
        } else {
            view.updatePreClosingToggleButtonIcon(gun0912.tedimagepicker.R.drawable.ic_arrow_drop_up_black_24dp);
        }
        preClosingContainerVisible = !preClosingContainerVisible;
        view.togglePreClosingContainer(preClosingContainerVisible);
    }

    @Override
    public void onSelectTour(String key) {
        selectedTourRow = models.tourModel.browse(key);
        onRefresh();
    }

    private static class Selection{
        String selection = "";
        String[] args = {};

        private void addSelection(String selection, String arg){
            if(arg != null){
                args = MyUtil.addArgs(args, arg);
            }
            this.selection = this.selection.length() > 0? this.selection + " and " + selection : selection;
        }
    }
}
