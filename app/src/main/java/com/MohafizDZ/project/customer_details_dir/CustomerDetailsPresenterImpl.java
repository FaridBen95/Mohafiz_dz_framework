package com.MohafizDZ.project.customer_details_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.project.customers_dir.Filters;
import com.MohafizDZ.project.models.CompanyCustomerModel;
import com.MohafizDZ.project.models.CompanyModel;
import com.MohafizDZ.project.models.CustomerCategoryModel;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.RegionModel;
import com.MohafizDZ.project.models.TourConfigurationModel;
import com.MohafizDZ.project.models.TourModel;
import com.MohafizDZ.project.models.TourVisitModel;

import java.util.List;

public class CustomerDetailsPresenterImpl implements ICustomerDetailsPresenter.Presenter{
    private static final String TAG = CustomerDetailsPresenterImpl.class.getSimpleName();

    private final ICustomerDetailsPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final String customerId;
    private final Models models;
    private final String currency;
    private DataRow customerRow, visitRow;
    private final DataRow tourRow;

    public CustomerDetailsPresenterImpl(ICustomerDetailsPresenter.View view, Context context, DataRow currentUserRow, String customerId) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.customerId = customerId;
        this.models = new Models(context);
        currency = CompanyModel.getCompanyCurrency(context);
        DataRow distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        tourRow = models.tourModel.getCurrentTour(distributorRow);
    }

    @Override
    public void onViewCreated() {
        initData();
        onRefresh();
    }

    private void initData(){
        customerRow = models.companyCustomerModel.browse(customerId);
        visitRow = tourRow != null? models.visitModel.getCurrentVisit(tourRow.getString(Col.SERVER_ID), customerId) : null;
    }

    @Override
    public void onRefresh() {
        view.setName(customerRow.getString("name"));
        view.setCode(customerRow.getString("customer_code"));
        view.setRegion(getRegion());
        view.setGpsPosition(customerRow.getString("geo_hash"));
        view.setPhoneNum(customerRow.getString("phone_num"));
        view.setAddress(customerRow.getString("address"));
        view.setNote(customerRow.getString("note"));
        view.setCategory(getCategory());
        view.setBalance(getPrice(customerRow.getFloat("balance")));
        view.setBalanceLimit(getPrice(customerRow.getFloat("balance_limit")));
        view.setImage(customerRow.getString("picture_low"), customerRow.getString("name"));
        view.toggleVisitContainer(visitRow != null);
    }

    private String getRegion(){
        DataRow regionRow = models.regionModel.browse(customerRow.getString("region_id"));
        return regionRow != null? regionRow.getString("name") : "-";
    }

    private String getCategory(){
        DataRow categoryRow = models.customerCategoryModel.browse(customerRow.getString("region_id"));
        return categoryRow != null? categoryRow.getString("name") : "-";
    }

    private String getString(int resId){
        return context.getString(resId);
    }

    private String getPrice(float price){
        return price + " " + currency;
    }

    @Override
    public void onCreateOptionsMenu() {
        view.toggleEditItem(canEditCustomers());
    }

    @Override
    public void requestOpenMap() {
        String tourId = tourRow != null? tourRow.getString(Col.SERVER_ID) : null;
        Filters filters = new Filters(tourId);
        filters.customerId = customerRow.getString(Col.SERVER_ID);
        view.openMap(filters);
    }

    private boolean canEditCustomers(){
        List<String> configurations = tourRow.getRelArray(models.tourModel, "configurations");
        return TourConfigurationModel.canEditCustomers(configurations);
    }

    private static class Models {
        private final TourModel tourModel;
        private final TourVisitModel visitModel;
        private final DistributorModel distributorModel;
        private final CompanyCustomerModel companyCustomerModel;
        private final RegionModel regionModel;
        private final CustomerCategoryModel customerCategoryModel;

        public Models(Context context) {
            this.tourModel = new TourModel(context);
            this.distributorModel = new DistributorModel(context);
            this.companyCustomerModel = new CompanyCustomerModel(context);
            this.regionModel = new RegionModel(context);
            this.customerCategoryModel = new CustomerCategoryModel(context);
            this.visitModel = new TourVisitModel(context);
        }
    }
}
