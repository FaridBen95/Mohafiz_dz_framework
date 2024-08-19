package com.MohafizDZ.project.customers_dir.filters_presenter_dir;

import android.content.Context;

import androidx.core.util.Pair;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.customers_dir.Filters;
import com.MohafizDZ.project.models.CustomerCategoryModel;
import com.MohafizDZ.project.models.RegionModel;
import com.MohafizDZ.project.models.TourConfigurationModel;
import com.MohafizDZ.project.models.TourModel;

import java.util.LinkedHashMap;
import java.util.List;

public class FiltersPresenterImpl implements IFiltersPresenter.Presenter{
    private static final String TAG = FiltersPresenterImpl.class.getSimpleName();

    private final IFiltersPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private Filters filters, receivedFilters;
    private final LinkedHashMap<String, String> proximityMap, visitStateMap;
    private String tourId;
    private DataRow tourRow;
    private boolean showPlannedCustomers, selectCustomerMode;

    public FiltersPresenterImpl(IFiltersPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        proximityMap = new LinkedHashMap<>();
        visitStateMap = new LinkedHashMap<>();
        this.models = new Models(context);
    }

    @Override
    public void setTourId(String tourId){
        this.tourId = tourId;
    }

    @Override
    public void setFilters(Filters filters) {
        receivedFilters = filters;
    }

    @Override
    public void setSelectCustomerMode(boolean selectCustomersMode) {
        this.showPlannedCustomers = !selectCustomersMode;
        this.selectCustomerMode = selectCustomersMode;
    }

    @Override
    public void orderByName() {
        filters.orderBy = Filters.OrderBy.name;
        onRefresh();
    }

    @Override
    public void orderByVisitDate() {
        filters.orderBy = Filters.OrderBy.visitDate;
        onRefresh();
    }

    @Override
    public void orderByProximity() {
        if(filters.currentLatitude != null && filters.currentLongitude != null) {
            filters.orderBy = Filters.OrderBy.proximity;
            onRefresh();
        }else{
            view.requestCurrentLocation(new IFiltersPresenter.LocationListener() {
                @Override
                public void onLocationChanged(double latitude, double longitude) {
                    view.toggleLoading(false);
                    filters.currentLatitude = latitude;
                    filters.currentLongitude = longitude;
                    orderByProximity();
                }

                @Override
                public void onStart() {
                    view.toggleLoading(true);
                }

                @Override
                public void onFailed() {
                    view.toggleLoading(false);
                    view.orderByName();
                }
            });
        }
    }

    @Override
    public void reverseSortBy(boolean checked) {
        filters.reverse = checked;
        onRefresh();
    }

    @Override
    public void showHasBalanceLimit(boolean checked) {
        filters.hasBalanceLimit = checked;
        onRefresh();
    }

    @Override
    public void showPlannedCustomers(boolean checked) {
        filters.plannedCustomers = checked;
        onRefresh();
    }

    @Override
    public void onProximitySelected(String key, String value) {
        if(key != null) {
            if (filters.currentLatitude != null && filters.currentLongitude != null) {
                filters.proximity = Float.valueOf(key);
                onRefresh();
            } else if(filters.proximity != Float.valueOf(key)){
                view.requestCurrentLocation(new IFiltersPresenter.LocationListener() {
                    @Override
                    public void onLocationChanged(double latitude, double longitude) {
                        view.toggleLoading(false);
                        filters.currentLatitude = latitude;
                        filters.currentLongitude = longitude;
                        onProximitySelected(key, value);
                    }

                    @Override
                    public void onStart() {
                        view.toggleLoading(true);
                    }

                    @Override
                    public void onFailed() {
                        view.toggleLoading(false);
                        view.setProximityFilter("");
                    }
                });
            }
        }else{
            filters.proximity = null;
        }
    }

    @Override
    public void onVisitStateSelected(String key, String value) {
        filters.visitState = key != null? Filters.VisitState.valueOf(key) : null;
        onRefresh();
    }

    @Override
    public void onCategorySelected(String key, String value) {
        filters.categoryId = key;
        filters.categoryName = value;
        onRefresh();
    }

    @Override
    public void onRegionSelected(String key, String value) {
        filters.regionId = key;
        filters.regionName = value;
        onRefresh();
    }

    @Override
    public void onTourSelected(String key, String value) {
        filters.tourId = key;
        filters.tourName = value;
        onRefresh();
    }

    @Override
    public void onResetClicked() {
        initFilters();
        prepareDefaultFilters();
        onRefresh();
    }

    private void initFilters(){
        if(receivedFilters != null){
            filters = receivedFilters;
        }else {
            filters = new Filters(tourId);
        }
        tourRow = filters.tourId != null? models.tourModel.browse(filters.tourId) : null;
        filters.plannedCustomers = showPlannedCustomers;
        if(forceCurrentRegion(tourRow)) {
            filters.regionId = getCurrentRegion(tourRow);
        }
    }

    @Override
    public void requestSelectStartDate() {
        view.requestSelectDateRange(new Pair<>(null, filters.visitDateEnd));
    }

    @Override
    public void requestSelectEndDate() {
        view.requestSelectDateRange(new Pair<>(filters.visitDateStart, null));
    }

    @Override
    public void onSelectDateRange(Pair<Long, Long> dateRange) {
        filters.visitDateStart = dateRange.first;
        filters.visitDateEnd = dateRange.second;
        view.setDateStartFilter(MyUtil.milliSecToDate(filters.visitDateStart, MyUtil.DEFAULT_DATE_FORMAT));
        view.setDateEndFilter(MyUtil.milliSecToDate(filters.visitDateEnd, MyUtil.DEFAULT_DATE_FORMAT));
        onRefresh();
    }

    @Override
    public void onBalanceStartChanged(String text) {
        try {
            filters.balanceStart = Float.valueOf(text);
        }catch (Exception ignored){}
        onRefresh();
    }

    @Override
    public void onBalanceEndChanged(String text) {
        try {
            filters.balanceEnd = Float.valueOf(text);
        }catch (Exception ignored){}
        onRefresh();
    }

    @Override
    public void onViewCreated() {
        initFilters();
        initData();
        prepareAutoCompleteFilters();
        prepareDefaultFilters();
        view.setFiltersControls();
        onRefresh();
    }

    private void initData(){
        proximityMap.clear();
        proximityMap.put("0.2", "200m");
        proximityMap.put("0.5", "500m");
        proximityMap.put("1", "1km");
        proximityMap.put("2", "2km");
        proximityMap.put("4", "4km");
        proximityMap.put("10", "10km");
        proximityMap.put("20", "20km");
        visitStateMap.clear();
        visitStateMap.put(Filters.VisitState.visited.name(), getString(R.string.visited_label));
        visitStateMap.put(Filters.VisitState.not_visited.name(), getString(R.string.not_visited_label));
    }

    private void prepareAutoCompleteFilters(){
        final LinkedHashMap<String, String> toursMap = getNamesFromRows(models.tourModel.getRows(), "name");
        filters.tourName = filters.tourId != null? toursMap.get(filters.tourId)
                : null;
        view.initToursFilter(toursMap, !forceCurrentTour(), filters.tourName);
        final LinkedHashMap<String, String> regionsMap = getNamesFromRows(models.regionModel.getRows(), "name");
        filters.regionName = filters.regionId != null ? regionsMap.get(filters.regionId)
                : null;
        view.initRegionsFilter(regionsMap, !forceCurrentRegion(tourRow), filters.regionName);
        view.initCategoriesFilter(getNamesFromRows(models.categoryModel.getRows(), "name"));
        view.initProximityFilter(proximityMap);
        view.initVisitStateFilter(visitStateMap);
    }

    private boolean forceCurrentTour() {
        return selectCustomerMode;
    }

    private String getCurrentRegion(DataRow tourRow){
        return selectCustomerMode? tourRow.getString("region_id") : null;
    }

    private boolean forceCurrentRegion(DataRow tourRow){
        return selectCustomerMode && tourRow != null && TourConfigurationModel.forceCurrentRegion(tourRow.getRelArray(models.tourModel, "configurations"));
    }


    private void prepareDefaultFilters(){
        view.setReverseChecked(filters.reverse);
        switch (filters.orderBy){
            case name:
                view.orderByName();
                break;
            case proximity:
                view.orderByProximity();
                break;
            case visitDate:
                view.orderByVisitDate();
                break;
        }
        if(filters.visitDateEnd != null){
            view.setDateEndFilter(getDateStr(filters.visitDateEnd));
        }else {
            view.setDateEndFilter(getString(R.string.date_end_label));
        }
        if(filters.visitDateStart != null) {
            view.setDateStartFilter(getDateStr(filters.visitDateStart));
        }else{
            view.setDateStartFilter(getString(R.string.date_start_label));
        }
        view.setHasBalanceChecked(filters.hasBalanceLimit);
        view.setPlannedCustomersChecked(filters.plannedCustomers);
        if(filters.balanceStart != null){
            view.setBalanceStart(filters.balanceStart + "");
        }else{
            view.setBalanceStart("");
        }
        if(filters.balanceEnd != null){
            view.setBalanceEnd(filters.balanceEnd + "");
        }else{
            view.setBalanceEnd("");
        }
        if(filters.proximity != null){
            view.setProximityFilter(proximityMap.get(filters.proximity + ""));
        }else{
            view.setProximityFilter("");
        }
        if(filters.visitState != null){
            view.setVisitStateFilter(visitStateMap.get(filters.visitState.name()));
        }else{
            view.setVisitStateFilter("");
        }
        if(filters.categoryId != null){
            view.setCategoryFilter(filters.categoryName);
        }else{
            view.setCategoryFilter("");
        }
        if(filters.regionId != null){
            filters.regionName = models.regionModel.browse(filters.regionId).getString("name");
            view.setRegionFilter(filters.regionName);
        }else{
            view.setRegionFilter("");
        }
        if(filters.tourId != null){
            filters.tourName = models.tourModel.browse(filters.tourId).getString("name");
            view.setTourFilter(filters.tourName);
        }else{
            view.setTourFilter("");
        }
    }

    private String getDateStr(long dateInMillis){
        return MyUtil.milliSecToDate(dateInMillis, MyUtil.DEFAULT_DATE_FORMAT);
    }

    private LinkedHashMap<String, String> getNamesFromRows(List<DataRow> rows, String key) {
        LinkedHashMap<String, String> list = new LinkedHashMap<>();
        for(DataRow row : rows){
            list.put(row.getString(Col.SERVER_ID), row.getString(key));
        }
        return list;
    }

    private String getString(int resId){
        return context.getString(resId);
    }

    @Override
    public void onRefresh() {
        view.setFilters(filters);
        view.enableVisitStateFilter(filters.tourId != null);
        view.enableVisitDatesFilter(filters.tourId != null);
        view.enableVisitDateOrder(filters.tourId != null);
    }

    private static class Models{
        private final TourModel tourModel;
        private final RegionModel regionModel;
        private final CustomerCategoryModel categoryModel;

        private Models(Context context){
            this.tourModel = new TourModel(context);
            this.regionModel = new RegionModel(context);
            this.categoryModel = new CustomerCategoryModel(context);
        }
    }
}
