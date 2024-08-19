package com.MohafizDZ.project.orders_list_dir.filter_presenter_dir;

import android.content.Context;

import androidx.core.util.Pair;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.CompanyCustomerModel;
import com.MohafizDZ.project.models.CompanyProductModel;
import com.MohafizDZ.project.models.RegionModel;
import com.MohafizDZ.project.models.TourModel;

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
    private final List<DataRow> products;
    private String selectedTourId, selectedCustomerId;
    private boolean isBackOrders;
    private boolean isSales;

    public FiltersPresenterImpl(IFiltersPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
        customers = new ArrayList<>();
        tours = new ArrayList<>();
        regions = new ArrayList<>();
        products = new ArrayList<>();
    }

    @Override
    public void onViewCreated() {
        filters = new Filters(isSales, isBackOrders);
        initData();
        prepareAutoCompleteFilters();
        prepareDefaultFilters();
        onRefresh();
    }

    private void initData(){
        customers.clear();
        customers.addAll(models.customerModel.getRows());
        regions.clear();
        regions.addAll(models.regionModel.getRows());
        products.clear();
        products.addAll(models.productModel.getRows());
        tours.clear();
        tours.addAll(models.tourModel.getRows());
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
        view.initProductsFilter(getNamesFromRows(products, "name"));
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
        view.setCustomerFilter(getCustomerName(filters.customerId));
        view.setRegionFilter(getRegionName(filters.regionId));
        view.setTourFilter(getTourName(filters.tourId));
        view.setProductFilter(getProductName(filters.productId));
        switch (filters.orderBy){
            case reference:
                view.orderByRef();
                break;
            case date:
                view.orderByDate();
                break;
            case customer:
                view.orderByCustomer();
                break;
            case amount:
                view.orderByAmount();
                break;
        }
    }

    private String getProductName(String productId) {
        return productId != null? filters.productName : "";
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
    public void orderByRef() {
        filters.orderBy = Filters.OrderBy.reference;
        onRefresh();
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
    public void orderByAmount() {
        filters.orderBy = Filters.OrderBy.amount;
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
    public void onProductSelected(String id, String name) {
        filters.productId = id;
        filters.productName = name;
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
        filters = new Filters(isSales, isBackOrders);
        filters.isSales = isSales;
        filters.isBackOrders = isBackOrders;
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
    public void setSalesFilter(boolean isSales) {
        this.isSales = isSales;
    }

    @Override
    public void setBackOrdersFilter(boolean isBackOrders) {
        this.isBackOrders = isBackOrders;
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
        private final CompanyProductModel productModel;

        private Models(Context context){
            tourModel = new TourModel(context);
            regionModel = new RegionModel(context);
            customerModel = new CompanyCustomerModel(context);
            productModel = new CompanyProductModel(context);
        }
    }
}
