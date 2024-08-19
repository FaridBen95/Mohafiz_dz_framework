package com.MohafizDZ.project.visit_action_list_dir.filter_presenter_dir;

import android.content.Context;

import androidx.core.util.Pair;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.CompanyCustomerModel;
import com.MohafizDZ.project.models.RegionModel;
import com.MohafizDZ.project.models.TourModel;
import com.MohafizDZ.project.models.TourVisitActionModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class FiltersPresenterImpl implements IFiltersPresenter.Presenter{
    private static final String TAG = FiltersPresenterImpl.class.getSimpleName();

    private final IFiltersPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private Filters filters;
    private final List<DataRow> customers;
    private final List<DataRow> tours;
    private final List<DataRow> regions;
    private final LinkedHashMap<String, String> proximityMap;
    private final List<Pair<String, String>> actionsMap;
    private String selectedTourId, selectedCustomerId;
    private String selectedAction;

    public FiltersPresenterImpl(IFiltersPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
        customers = new ArrayList<>();
        tours = new ArrayList<>();
        regions = new ArrayList<>();
        proximityMap = new LinkedHashMap<>();
        actionsMap = new ArrayList<>();
    }

    @Override
    public void onViewCreated() {
        filters = new Filters(selectedAction);
        initData();
        prepareActionsFilter();
        prepareAutoCompleteFilters();
        prepareDefaultFilters();
        onRefresh();
    }

    private void prepareActionsFilter(){
        view.clearActionsFilter();
        for(int i = 0; i < actionsMap.size(); i++){
            Pair<String, String> action = actionsMap.get(i);
            boolean checked = filters.selectedAction != null && filters.selectedAction.equals(action.first);
            view.createActionChip(action.second, i, checked);
        }
    }

    private void initData(){
        customers.clear();
        customers.addAll(models.customerModel.getRows());
        regions.clear();
        regions.addAll(models.regionModel.getRows());
        tours.clear();
        tours.addAll(models.tourModel.getRows());
        proximityMap.clear();
        String moreThanLabel = getString(R.string.more_than_label);
        proximityMap.put("0.01", moreThanLabel + "10m");
        proximityMap.put("0.02", moreThanLabel + "20m");
        proximityMap.put("0.05", moreThanLabel + "50m");
        proximityMap.put("0.1", moreThanLabel + "100m");
        actionsMap.clear();
        actionsMap.add(new Pair<>(TourVisitActionModel.ACTION_VISIT_START, models.actionModel.getAction(TourVisitActionModel.ACTION_VISIT_START)));
        actionsMap.add(new Pair<>(TourVisitActionModel.ACTION_VISIT_STOP, models.actionModel.getAction(TourVisitActionModel.ACTION_VISIT_STOP)));
        actionsMap.add(new Pair<>(TourVisitActionModel.ACTION_VISIT_RESTART, models.actionModel.getAction(TourVisitActionModel.ACTION_VISIT_RESTART)));
        actionsMap.add(new Pair<>(TourVisitActionModel.ACTION_SALE, models.actionModel.getAction(TourVisitActionModel.ACTION_SALE)));
        actionsMap.add(new Pair<>(TourVisitActionModel.ACTION_DELETE_ORDER, models.actionModel.getAction(TourVisitActionModel.ACTION_DELETE_ORDER)));
        actionsMap.add(new Pair<>(TourVisitActionModel.ACTION_CANCEL_ORDER, models.actionModel.getAction(TourVisitActionModel.ACTION_CANCEL_ORDER)));
        actionsMap.add(new Pair<>(TourVisitActionModel.ACTION_BACK_ORDER, models.actionModel.getAction(TourVisitActionModel.ACTION_BACK_ORDER)));
        actionsMap.add(new Pair<>(TourVisitActionModel.ACTION_NO_ACTION, models.actionModel.getAction(TourVisitActionModel.ACTION_NO_ACTION)));
        actionsMap.add(new Pair<>(TourVisitActionModel.ACTION_OTHER, models.actionModel.getAction(TourVisitActionModel.ACTION_OTHER)));
        actionsMap.add(new Pair<>(TourVisitActionModel.ACTION_PAYMENT, models.actionModel.getAction(TourVisitActionModel.ACTION_PAYMENT)));
        actionsMap.add(new Pair<>(TourVisitActionModel.ACTION_REFUND, models.actionModel.getAction(TourVisitActionModel.ACTION_REFUND)));
    }

    private void prepareAutoCompleteFilters() {
        final LinkedHashMap<String, String> customersMap = getNamesFromRows(customers, "name");
        filters.customerId = selectedCustomerId;
        filters.customerName = selectedCustomerId != null? customersMap.get(selectedCustomerId) : null;
        view.initCustomersFilter(customersMap, filters.customerName);
        view.initRegionsFilter(getNamesFromRows(regions, "name"));
        final LinkedHashMap<String, String> toursMap = getNamesFromRows(tours, "name");
        filters.tourId = selectedTourId;
        filters.tourName = selectedTourId != null? toursMap.get(selectedTourId) : null;
        view.initToursFilter(toursMap, filters.tourName);
        view.initProximityFilter(proximityMap);
    }

    private LinkedHashMap<String, String> getNamesFromRows(List<DataRow> rows, String key) {
        LinkedHashMap<String, String> list = new LinkedHashMap<>();
        for(DataRow row : rows){
            list.put(row.getString(Col.SERVER_ID), row.getString(key));
        }
        return list;
    }


    private void prepareDefaultFilters(){
        view.setReverseChecked(filters.reverse);
        Long dateStart = filters.dateStart;
        if(dateStart != null) {
            view.setDateStartFilter(getDateStr(dateStart));
        }else{
            view.setDateStartFilter(getString(R.string.date_start_label));
        }
        Long dateEnd = filters.dateEnd;
        if(dateEnd != null){
            view.setDateEndFilter(getDateStr(dateEnd));
        }else{
            view.setDateEndFilter(getString(R.string.date_end_label));
        }
        if(filters.distance != null){
            view.setProximityFilter(proximityMap.get(filters.distance + ""));
        }else{
            view.setProximityFilter("");
        }
        view.setCustomerFilter(getCustomerName(filters.customerId));
        view.setRegionFilter(getRegionName(filters.regionId));
        view.setTourFilter(getTourName(filters.tourId));
        switch (filters.orderBy){
            case date:
                view.orderByDate();
                break;
            case customer:
                view.orderByCustomer();
                break;
            case distance:
                view.orderByDistance();
                break;
        }
    }

    private String getTourName(String tourId) {
        return tourId != null? filters.tourName : "";
    }

    private String getRegionName(String regionId) {
        return regionId != null? filters.regionName : "";
    }

    private String getCustomerName(String customerId) {
        return customerId != null? filters.customerName : "";
    }

    private String getString(int resId){
        return context.getString(resId);
    }

    private String getDateStr(long dateInMillis){
        return MyUtil.milliSecToDate(dateInMillis, MyUtil.DEFAULT_DATE_FORMAT);
    }

    @Override
    public void onRefresh() {
        view.setFilters(filters);
    }

    @Override
    public void orderByDate() {
        filters.orderBy = Filters.OrderBy.date;
        onRefresh();
    }

    @Override
    public void orderByCustomer() {
        filters.orderBy = Filters.OrderBy.customer;
        onRefresh();
    }

    @Override
    public void orderByDistance() {
        filters.orderBy = Filters.OrderBy.distance;
        onRefresh();
    }

    @Override
    public void onCustomerSelected(String id, String name) {
        filters.customerId = id;
        filters.customerName = name;
        onRefresh();
    }

    @Override
    public void onRegionSelected(String id, String name) {
        filters.regionId = id;
        filters.regionName = name;
        onRefresh();
    }

    @Override
    public void onTourSelected(String id, String name) {
        filters.tourId = id;
        filters.tourName = name;
        onRefresh();
    }

    @Override
    public void onResetClicked() {
        filters = new Filters(selectedAction);
        prepareDefaultFilters();
        onRefresh();
    }

    @Override
    public void requestSelectStartDate() {
        view.requestSelectDateRange(new Pair<>(null, filters.dateEnd));
    }

    @Override
    public void requestSelectEndDate() {
        view.requestSelectDateRange(new Pair<>(filters.dateStart, null));
    }

    @Override
    public void onSelectDateRange(Pair<Long, Long> dateRange) {
        filters.dateStart = dateRange.first;
        filters.dateEnd = dateRange.second;
        view.setDateStartFilter(MyUtil.milliSecToDate(filters.dateStart, MyUtil.DEFAULT_DATE_FORMAT));
        view.setDateEndFilter(MyUtil.milliSecToDate(filters.dateEnd, MyUtil.DEFAULT_DATE_FORMAT));
        onRefresh();
    }

    @Override
    public void setTourId(String tourId) {
        selectedTourId = tourId;
    }

    @Override
    public void setCustomerId(String customerId) {
        selectedCustomerId = customerId;
    }

    @Override
    public void setSelectedAction(String action) {
        this.selectedAction = action;
    }

    @Override
    public void filterByAllCategories(boolean checked) {
        filters.showAllActions = checked;
        if(checked){
            unCheckActions();
        }
        onRefresh();
    }

    @Override
    public void onActionClicked(int position) {
        Pair<String, String> actions = actionsMap.get(position);
        boolean showAllCategories = filters.showAllActions;
        filters.addOrRemove(actions.first);
        if(showAllCategories != filters.showAllActions) {
            view.filterByAllActions(filters.showAllActions);
        }else{
            onRefresh();
        }
    }

    private void unCheckActions(){
        filters.clearActions();
        prepareActionsFilter();
        view.filterByAllActions(filters.showAllActions);
    }

    @Override
    public void reverseSortBy(boolean checked) {
        filters.reverse = checked;
        onRefresh();
    }

    private static class Models{
        private final TourModel tourModel;
        private final RegionModel regionModel;
        private final CompanyCustomerModel customerModel;
        private final TourVisitActionModel actionModel;

        private Models(Context context){
            tourModel = new TourModel(context);
            regionModel = new RegionModel(context);
            customerModel = new CompanyCustomerModel(context);
            actionModel = new TourVisitActionModel(context);
        }
    }
}
