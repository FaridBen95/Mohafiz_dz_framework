package com.MohafizDZ.project.customers_dir.customers_map_dir;

import android.content.Context;

import androidx.core.util.Pair;

import com.MohafizDZ.framework_repository.Utils.GeoUtil;
import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.Utils.Selection;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.project.customers_dir.Filters;
import com.MohafizDZ.project.models.CompanyCustomerModel;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.RegionModel;
import com.MohafizDZ.project.models.TourConfigurationModel;
import com.MohafizDZ.project.models.TourModel;
import com.MohafizDZ.project.models.TourVisitModel;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class CustomersMapPresenterImpl implements ICustomersMapPresenter.Presenter{
    private static final String TAG = CustomersMapPresenterImpl.class.getSimpleName();

    private final ICustomersMapPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private final List<DataRow> rows;
    private Filters filters;
    private String searchFilter = "";
    private boolean selectCustomerMode;
    private boolean editable;
    private DataRow currentTourRow;

    public CustomersMapPresenterImpl(ICustomersMapPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        models = new Models(context);
        rows = new ArrayList<>();
    }

    @Override
    public void onViewCreated() {
        initData();
        view.getCurrentLocation();
    }

    private void initData(){
        DataRow distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        currentTourRow = models.tourModel.getCurrentTour(distributorRow);
    }

    @Override
    public void onRefresh() {
        loadCustomers();
        prepareMap();
    }

    private void prepareMap(){
        view.clearMarkers();
        for(int i=0; i <rows.size(); i++){
            DataRow row = rows.get(i);
            double latitude = Double.valueOf(row.getString("latitude"));
            double longitude = Double.valueOf((row.getString("longitude")));
            view.addMarker(row.getString("name"), latitude, longitude, isEditable(), i);
        }
    }

    private void loadCustomers(){
        rows.clear();
        Selection selection = prepareSelection();
        String sortBy = prepareSortBy();
        String tourId = filters.tourId;
        String latitude = filters.currentLatitude != null? filters.currentLatitude + "" : "0";
        String longitude = filters.currentLongitude != null? filters.currentLongitude + "" : "0";
        List<DataRow> customers = models.customerModel.getCustomers(tourId, latitude,
                longitude, selection.getSelection(), selection.getArgs(), sortBy);
        for(DataRow row : customers){
            row.put("visited", row.getString("visit_state").equals(TourVisitModel.STATE_VISITED));
            rows.add(row);
        }
    }

    private Selection prepareSelection(){
        Selection selection = new Selection();
        if(!searchFilter.equals("")){
            selection.addSelection(" (Lower(cc.name) like ? or Lower(address) like ? or customer_code like ?) ");
            selection.addArg("%" + searchFilter + "%");
            selection.addArg("%" + searchFilter + "%");
            selection.addArg("%" + searchFilter + "%");
        }
        if(filters != null){
            if(filters.customerId != null){
                selection.addSelection(" cc.id = ? ", filters.customerId);
            }
            if(filters.hasBalanceLimit){
                selection.addSelection(" balance_limit > 0 ");
            }
            if(filters.balanceStart != null && filters.balanceStart > 0){
                selection.addSelection(" balance > " + filters.balanceStart);
            }
            if(filters.balanceEnd != null && filters.balanceEnd > 0){
                selection.addSelection(" balance <= " + filters.balanceEnd);
            }
            if(filters.visitState != null){
                selection.addSelection("visit_state = ? ", filters.visitState == Filters.VisitState.visited? TourVisitModel.STATE_VISITED : TourVisitModel.STATE_DRAFT);
            }
            if(filters.categoryId != null){
                selection.addSelection(" category_id = ? ", filters.categoryId);
            }
            if(filters.regionId != null){
                selection.addSelection(" region_id = ? ", filters.regionId);
            }
            if(filters.proximity != null && filters.currentLatitude != null && filters.currentLongitude != null){
                Pair<LatLng, LatLng> proximityLocations = GeoUtil.getProximityLocations(filters.proximity, filters.currentLatitude, filters.currentLongitude);
                selection.addSelection("cc.latitude between ? and ? and cc.longitude between ? and ?");
                selection.addArg(proximityLocations.first.latitude + "");
                selection.addArg(proximityLocations.second.latitude + "");
                selection.addArg(proximityLocations.first.longitude + "");
                selection.addArg(proximityLocations.second.longitude + "");
            }
            if(filters.plannedCustomers && filters.tourId != null){
                selection.addSelection(" tv.tour_id = ? ", filters.tourId);
            }
            if(filters.visitDateStart != null && filters.visitDateEnd != null){
                String dateStartStr = MyUtil.milliSecToDate(filters.visitDateStart, MyUtil.DEFAULT_DATE_FORMAT);
                String dateEndStr = MyUtil.milliSecToDate(filters.visitDateEnd, MyUtil.DEFAULT_DATE_FORMAT) + " 23:59:59";
                selection.addSelection(" visited_date between ? and ? ", dateStartStr);
                selection.addArg(dateEndStr);
            }
        }
        return selection;
    }

    private String prepareSortBy(){
        if(filters != null){
            String direction = !filters.reverse? " desc" : " asc";
            switch (filters.orderBy){
                case visitDate:
                    return " visited_date" + direction;
                case name:
                    return " Lower(cc.name)" + direction;
                case proximity:
                    return " squared_distance" + direction;
            }
        }
        return null;
    }

    @Override
    public void onSearch(String searchFilter) {
        searchFilter = searchFilter == null? "" : searchFilter;
        if(!this.searchFilter.equals(searchFilter)){
            this.searchFilter = searchFilter;
            onRefresh();
        }
    }

    @Override
    public void setFilters(Filters filters) {
        this.filters = filters;
    }

    @Override
    public void setSelectCustomerMode(boolean selectCustomerMode) {
        this.selectCustomerMode = selectCustomerMode;
    }

    @Override
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    private boolean isEditable(){
        return canEditCustomer() && editable;
    }

    private boolean canEditCustomer(){
        return TourConfigurationModel.canEditCustomers(currentTourRow.getRelArray(models.tourModel, "configurations"));
    }

    @Override
    public void onCustomerClick(Integer position) {
        DataRow customerRow = rows.get(position);
        String customerId = customerRow.getString(Col.SERVER_ID);
        if(selectCustomerMode){
            view.onSelectCustomer(customerId);
            view.goBack();
        }else{
            view.openCustomerDetails(customerId, filters.tourId);
        }
    }

    @Override
    public void onMarkerDragged(Integer position, double latitude, double longitude) {
        DataRow row = rows.get(position);
        Values values = new Values();
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        values.put("geo_hash", MyUtil.getGeoHash(latitude, longitude));
        models.customerModel.updateCustomer(row.getString(Col.SERVER_ID), values);
        view.onCustomerUpdated();
    }

    private static class Models{
        private final TourModel tourModel;
        private final RegionModel regionModel;
        private final DistributorModel distributorModel;
        private final CompanyCustomerModel customerModel;

        private Models(Context context){
            tourModel = new TourModel(context);
            regionModel = new RegionModel(context);
            distributorModel = new DistributorModel(context);
            customerModel = new CompanyCustomerModel(context);
        }
    }
}
