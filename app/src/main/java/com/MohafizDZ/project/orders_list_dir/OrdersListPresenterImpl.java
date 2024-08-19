package com.MohafizDZ.project.orders_list_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.Utils.Selection;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.CompanyCustomerModel;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.TourModel;
import com.MohafizDZ.project.models.VisitOrderModel;
import com.MohafizDZ.project.orders_list_dir.filter_presenter_dir.Filters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrdersListPresenterImpl implements IOrderListPresenter.Presenter{
    private static final String TAG = OrdersListPresenterImpl.class.getSimpleName();

    private final IOrderListPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private final List<DataRow> rows;
    private DataRow tourRow, distributorRow;
    private Filters filters;
    private String searchFilter = "";

    public OrdersListPresenterImpl(IOrderListPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
        rows = new ArrayList<>();
    }

    @Override
    public void onViewCreated() {
        initData();
        view.initAdapter(rows);
        view.setToolbarTitle(getString(R.string.orders_label));
    }

    private String getString(int resId){
        return context.getString(resId);
    }

    private void initData(){
        distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        tourRow = models.tourModel.getCurrentTour(distributorRow);
    }

    @Override
    public void setFilters(Filters filters) {
        this.filters = filters;
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
    public void onRefresh() {
        loadOrders();
        view.onLoadFinished(rows);
    }

    private void loadOrders(){
        rows.clear();
        Selection selection = prepareSelection();
        String sortBy = prepareSortBy();
        Map<String, DataRow> customersMap = models.companyCustomerModel.getMap(Col.SERVER_ID);
        for(DataRow orderRow : models.visitOrderModel.getVisitDetails( selection.getSelection(), selection.getArgs(), sortBy)){
            DataRow customerRow = customersMap.getOrDefault(orderRow.getString("customer_id"), null);
            orderRow.putRel("customer", customerRow);
            rows.add(orderRow);
        }
    }

    private Selection prepareSelection() {
        Selection selection = new Selection();
        if(!searchFilter.equals("")){
            selection.addSelection(" (vo.name like ? or customer_name like ?) ");
            selection.addArg("%" + searchFilter + "%");
            selection.addArg("%" + searchFilter + "%");
        }
        if(filters != null) {
            if(filters.isSales){
                selection.addSelection(" total_amount > 0 ");
            }
            if(filters.isBackOrders){
                selection.addSelection(" total_amount < 0 ");
            }
            if (filters.customerId != null) {
                selection.addSelection(" customer_id = ? ", filters.customerId);
            }
            if (filters.regionId != null) {
                selection.addSelection("customer_region_id = ? ", filters.regionId);
            }
            if (filters.tourId != null) {
                selection.addSelection("tour_id = ? ", filters.tourId);
            }
            if (filters.productId != null) {
                selection.addSelection("vopr.rel_col = ? ", filters.productId);
            }
            if (filters.dateStart != null && filters.dateEnd != null) {
                String dateStartStr = MyUtil.milliSecToDate(filters.dateStart, MyUtil.DEFAULT_DATE_FORMAT);
                String dateEndStr = MyUtil.milliSecToDate(filters.dateEnd, MyUtil.DEFAULT_DATE_FORMAT) + " 23:59:59";
                selection.addSelection(" done_date >= ? ", dateStartStr);
                selection.addSelection(" done_date <= ? ", dateEndStr);
            }
        }
        return selection;
    }

    private String prepareSortBy(){
        if(filters != null && filters.orderBy != null) {
            String direction = filters.reverse? " desc" : " asc";
            switch (filters.orderBy){
                case date:
                    return " done_date" + direction;
                case amount:
                    return " total_amount" + direction;
                case customer:
                    return " customer_name" + direction;
                case reference:
                    return " vo.name"+ direction;
            }
        }
        return null;
    }

    @Override
    public void onItemClick(int position) {
        DataRow row = rows.get(position);
        view.requestOpenDetails(row.getString(Col.SERVER_ID));
    }

    private static class Models{
        private final TourModel tourModel;
        private final DistributorModel distributorModel;
        private final VisitOrderModel visitOrderModel;
        private final CompanyCustomerModel companyCustomerModel;

        private Models(Context context){
            this.tourModel = new TourModel(context);
            this.distributorModel = new DistributorModel(context);
            this.visitOrderModel = new VisitOrderModel(context);
            this.companyCustomerModel = new CompanyCustomerModel(context);
        }
    }
}
