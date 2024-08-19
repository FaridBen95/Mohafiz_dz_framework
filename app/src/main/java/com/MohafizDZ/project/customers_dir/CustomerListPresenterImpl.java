package com.MohafizDZ.project.customers_dir;

import android.content.Context;

import androidx.core.util.Pair;

import com.MohafizDZ.framework_repository.Utils.GeoUtil;
import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.Utils.Selection;
import com.MohafizDZ.framework_repository.core.BitmapDataRow;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.project.models.CompanyCustomerModel;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.RegionModel;
import com.MohafizDZ.project.models.TourConfigurationModel;
import com.MohafizDZ.project.models.TourModel;
import com.MohafizDZ.project.models.TourVisitModel;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomerListPresenterImpl implements ICustomersListPresenter.Presenter {
    private static final String TAG = CustomerListPresenterImpl.class.getSimpleName();

    private final ICustomersListPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private DataRow distributorRow, currentTourRow;
    private final List<BitmapDataRow> rows;
    private final List<String> configurations;
    private Filters filters;
    private String searchFilter = "";
    private boolean selectCustomerMode;

    public CustomerListPresenterImpl(ICustomersListPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
        rows = new ArrayList<>();
        configurations = new ArrayList<>();
    }

    @Override
    public void onViewCreated() {
        initData();
        view.initAdapter(rows);
    }

    private boolean canEditCustomers(){
        if(isCurrentTour()) {
            return TourConfigurationModel.canEditCustomers(configurations);
        }else{
            return false;
        }
    }

    private boolean isCurrentTour(){
        return filters != null && filters.tourId != null && currentTourRow != null && filters.tourId.equals(currentTourRow.getString(Col.SERVER_ID));
    }

    private void initData(){
        distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        currentTourRow = models.tourModel.getCurrentTour(distributorRow);
        configurations.clear();
        configurations.addAll(currentTourRow.getRelArray(models.tourModel, "configurations"));
    }

    @Override
    public void onRefresh() {
        view.toggleAddCustomer(canEditCustomers());
        loadCustomers();
        view.onLoadFinished(rows);
    }

    @Override
    public void onCustomerChanged(int selectedPosition) {
        DataRow selectedRow = rows.get(selectedPosition);
        String selection = " tour_id = ? ";
        String[] args = {currentTourRow.getString(Col.SERVER_ID)};
        Map<String, DataRow> visits = models.visitModel.getMap(selection, args, null, "customer_id");
        DataRow visitRow = visits.getOrDefault(selectedRow.getString(Col.SERVER_ID), null);
        selectedRow.put("visited", visitRow != null && visitRow.getString("state").equals(TourVisitModel.STATE_VISITED));
        BitmapDataRow bitmapDataRow = new BitmapDataRow();
        bitmapDataRow.putAll(models.companyCustomerModel.browse(selectedRow.getString(Col.SERVER_ID)));
        rows.set(selectedPosition, bitmapDataRow);
        view.onCustomerUpdated(selectedPosition);
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
    public void requestOpenMap() {
        String tourId = filters.tourId;
//        view.requestOpenCustomersMap(tourId);
        view.requestOpenCustomersMap(filters, selectCustomerMode);
    }

    private void loadCustomers(){
        rows.clear();
        Selection selection = prepareSelection();
        String sortBy = prepareSortBy();
        String tourId = filters.tourId;
        String latitude = filters.currentLatitude != null? filters.currentLatitude + "" : "0";
        String longitude = filters.currentLongitude != null? filters.currentLongitude + "" : "0";
        List<DataRow> customers = models.companyCustomerModel.getCustomers(tourId, latitude,
                longitude, selection.getSelection(), selection.getArgs(), sortBy);
        for(DataRow row : customers){
            row.put("visited", row.getString("visit_state").equals(TourVisitModel.STATE_VISITED));
            BitmapDataRow bitmapDataRow = new BitmapDataRow();
            bitmapDataRow.putAll(row);
            rows.add(bitmapDataRow);
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
            String direction = filters.reverse? " desc" : " asc";
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
    public void setSelectCustomerMode(boolean active) {
        this.selectCustomerMode = active;
    }

    @Override
    public void onItemLongClick(int position) {
        DataRow row = rows.get(position);
        String tourId = filters.tourId;
        view.requestOpenDetails(position, row.getString(Col.SERVER_ID), tourId);
    }

    @Override
    public void onItemClick(int position) {
        DataRow row = rows.get(position);
        if (selectCustomerMode && isCurrentTour()) {
            view.onSelectCustomer(row.getString(Col.SERVER_ID));
        }else{
            view.requestOpenDetails(position, row.getString(Col.SERVER_ID), filters.tourId);
        }
    }

    private static class Models{
        private final CompanyCustomerModel companyCustomerModel;
        private final DistributorModel distributorModel;
        private final TourModel tourModel;
        private final TourVisitModel visitModel;
        private final RegionModel regionModel;

        private Models(Context context){
            this.companyCustomerModel = new CompanyCustomerModel(context);
            this.distributorModel = new DistributorModel(context);
            this.tourModel = new TourModel(context);
            this.visitModel = new TourVisitModel(context);
            this.regionModel = new RegionModel(context);
        }
    }
}
